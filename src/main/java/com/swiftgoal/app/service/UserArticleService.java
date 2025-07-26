package com.swiftgoal.app.service;

import com.swiftgoal.app.repository.entity.NewsArticle;
import com.swiftgoal.app.repository.entity.User;
import com.swiftgoal.app.dto.AiAnalysisResult;
import com.swiftgoal.app.dto.UserArticleDto;
import com.swiftgoal.app.repository.NewsArticleRepository;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UserArticleService {

    private static final Logger logger = LoggerFactory.getLogger(UserArticleService.class);

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    @Autowired
    private AIService aiService;

    @Autowired
    private HtmlTranslator htmlTranslator;

    @Autowired
    private StorageService storageService;

    @Transactional
    public NewsArticle createUserArticle(UserArticleDto articleDto, User user) {
        logger.info("Creating new user article: {} by user: {}", articleDto.getTitle(), user.getUsername());

        NewsArticle article = new NewsArticle();
        article.setTitle(articleDto.getTitle());
        article.setRawHtmlContent(articleDto.getContent()); // Save the original content
        article.setSource(articleDto.getSource() != null && !articleDto.getSource().isEmpty() ? articleDto.getSource() : "用户投稿");
        article.setPublishDate(LocalDateTime.now());
        article.setUser(user);
        article.setUserGenerated(true);
        article.setUploadTime(LocalDateTime.now());
        article.setCategoryAi(articleDto.getCategory());
        
        // --- Key Change: Set to PENDING and save immediately ---
        article.setModerationStatus(0); // 0 = Pending
        article.setTranslationStatus(0); // 0 = Not translated yet

        String userArticleUrl = "/user/articles/" + System.currentTimeMillis() + "-" + user.getId();
        article.setUrl(userArticleUrl);

        NewsArticle savedArticle = newsArticleRepository.save(article);
        logger.info("Successfully created user article with ID: {}, setting to PENDING.", savedArticle.getId());

        // --- Key Change: Trigger async processing ---
        processAndPublishArticle(savedArticle.getId());
        
        return savedArticle;
    }

    @Async
    @Transactional
    public void processAndPublishArticle(Long articleId) {
        try {
            // 1. Wait for 5 seconds
            TimeUnit.SECONDS.sleep(5);
            logger.info("Starting delayed processing for article ID: {}", articleId);

            Optional<NewsArticle> articleOpt = newsArticleRepository.findById(articleId);
            if (articleOpt.isEmpty()) {
                logger.error("Article with ID {} not found for async processing.", articleId);
                return;
            }

            NewsArticle article = articleOpt.get();
            String plainTextContent = Jsoup.parse(article.getRawHtmlContent()).text();
            
            if (plainTextContent.length() > 50) {
                logger.info("Starting AI processing for user article: {}", article.getTitle());

                // Step 1: Translate the raw HTML
                String translatedHtml = htmlTranslator.translateHtmlContent(article.getRawHtmlContent());

                // Step 2: Clean the translated HTML.
                String cleanedHtml = aiService.cleanTranslatedHtml(translatedHtml);

                if (cleanedHtml != null && !cleanedHtml.isBlank()) {
                    article.setTranslatedContent(translatedHtml); // Keep original translation
                    article.setRawHtmlContent(cleanedHtml); // Save cleaned HTML
                    article.setTranslationStatus(1);
                    logger.info("HTML cleaning/translation successful for user article: {}", article.getTitle());

                    // Step 3: Perform AI analysis on the *original plain text* for better accuracy
                    AiAnalysisResult analysisResult = aiService.getAiAnalysisForArticle(article.getTitle(), plainTextContent);
                    if (analysisResult != null && analysisResult.getTranslatedTitle() != null && !analysisResult.getTranslatedTitle().isBlank()) {
                        article.setTitleCn(analysisResult.getTranslatedTitle());
                        article.setSummaryAiCn(analysisResult.getChineseSummary());
                        article.setKeywordsAi(analysisResult.getKeywords());
                    } else {
                        logger.warn("AI analysis returned null or incomplete for user article: {}.", article.getTitle());
                    }
                } else {
                    logger.warn("AI HTML cleaning/translation failed for user article: {}.", article.getTitle());
                }
            } else {
                logger.warn("Content for '{}' is too short, skipping AI processing.", article.getTitle());
            }

            // --- Key Change: Always approve after delay ---
            article.setModerationStatus(1); // 1 = Approved
            newsArticleRepository.save(article);
            logger.info("Article ID: {} has been automatically published after a delay.", articleId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Article processing delay was interrupted for article ID: {}", articleId, e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred during async processing for article ID: {}", articleId, e);
            // Optionally, set the status to 'failed' to indicate a problem
            newsArticleRepository.findById(articleId).ifPresent(article -> {
                article.setModerationStatus(2); // 2 = Failed
                newsArticleRepository.save(article);
            });
        }
    }

    @Transactional
    public NewsArticle updateUserArticle(Long articleId, UserArticleDto articleDto, User user) {
        logger.info("Updating user article ID: {} by user: {}", articleId, user.getUsername());

        NewsArticle article = newsArticleRepository.findByIdAndUserId(articleId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("文章不存在或您没有权限编辑此文章"));

        article.setTitle(articleDto.getTitle());
        // article.setRawHtmlContent(articleDto.getContent());
        article.setSource(articleDto.getSource() != null && !articleDto.getSource().isEmpty() ? articleDto.getSource() : "用户投稿");
        article.setCategoryAi(articleDto.getCategory());
        
        String plainTextContent = Jsoup.parse(articleDto.getContent()).text();
        article.setRawContent(plainTextContent);

        // Reset status to pending for re-evaluation
        article.setModerationStatus(0);
        article.setTranslationStatus(0);

        try {
            if (plainTextContent.length() > 50) {
                logger.info("Re-running AI processing for updated user article: {}", article.getTitle());

                // Step 1: Re-translate content
                String translatedHtml = htmlTranslator.translateHtmlContent(articleDto.getContent());

                // Step 2: Clean the translated HTML
                String cleanedHtml = aiService.cleanTranslatedHtml(translatedHtml);
                
                if (cleanedHtml != null && !cleanedHtml.isBlank()) {
                    article.setTranslatedContent(translatedHtml); // Keep original translation
                    article.setRawHtmlContent(cleanedHtml); // Save cleaned HTML
                    article.setTranslationStatus(1);
                    logger.info("HTML cleaning/translation successful for updated user article: {}", article.getTitle());

                    // Step 3: Perform AI analysis
                    AiAnalysisResult analysisResult = aiService.getAiAnalysisForArticle(article.getTitle(), plainTextContent);
                    if (analysisResult != null && analysisResult.getTranslatedTitle() != null && !analysisResult.getTranslatedTitle().isBlank()) {
                        article.setTitleCn(analysisResult.getTranslatedTitle());
                        article.setSummaryAiCn(analysisResult.getChineseSummary());
                        article.setKeywordsAi(analysisResult.getKeywords());
                        
                        // AI analysis passed, approve the article
                        article.setModerationStatus(1);
                        logger.info("AI check passed. Updated article {} approved and published.", article.getTitle());
                    } else {
                        logger.warn("AI analysis returned null or incomplete for updated article: {}. It remains pending.", article.getTitle());
                    }
                } else {
                     logger.warn("AI HTML cleaning/translation failed for updated article: {}. It remains pending.", article.getTitle());
                }
            } else {
                 logger.warn("Content for updated article '{}' is too short, skipping AI processing. It remains pending.", article.getTitle());
            }
        } catch (Exception e) {
            logger.error("AI processing failed for updated user article: {}: {}", article.getTitle(), e.getMessage(), e);
        }

        NewsArticle savedArticle = newsArticleRepository.save(article);
        logger.info("Successfully updated user article with ID: {}", savedArticle.getId());
        
        return savedArticle;
    }

    @Transactional
    public void deleteUserArticle(Long articleId, User user) {
        logger.info("Deleting user article ID: {} by user: {}", articleId, user.getUsername());

        NewsArticle article = newsArticleRepository.findByIdAndUserId(articleId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("文章不存在或您没有权限删除此文章"));

        newsArticleRepository.delete(article);
        logger.info("Successfully deleted user article with ID: {}", articleId);
    }

    public List<NewsArticle> getArticlesByUser(User user) {
        return newsArticleRepository.findByUserIdOrderByPublishDateDesc(user.getId());
    }

    public Optional<NewsArticle> getArticleByIdAndUser(Long articleId, User user) {
        return newsArticleRepository.findByIdAndUserId(articleId, user.getId());
    }

    public UserArticleDto mapEntityToDto(NewsArticle article) {
        UserArticleDto articleDto = new UserArticleDto();
        articleDto.setTitle(article.getTitle());
        articleDto.setContent(article.getRawHtmlContent());
        articleDto.setSource(article.getSource());
        articleDto.setCategory(article.getCategoryAi());
        return articleDto;
    }

    private String extractTextFromHtml(String html) {
        if (html == null) {
            return "";
        }
        return Jsoup.parse(html).text();
    }
} 