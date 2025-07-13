package com.sportslens.ai.service;

import com.sportslens.ai.domain.NewsArticle;
import com.sportslens.ai.domain.User;
import com.sportslens.ai.dto.AiAnalysisResult;
import com.sportslens.ai.dto.UserArticleDto;
import com.sportslens.ai.repository.NewsArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserArticleService {

    private static final Logger logger = LoggerFactory.getLogger(UserArticleService.class);

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    @Autowired
    private AIService aiService;

    @Transactional
    public NewsArticle createUserArticle(UserArticleDto articleDto, User user) {
        logger.info("Creating new user article: {} by user: {}", articleDto.getTitle(), user.getUsername());

        NewsArticle article = new NewsArticle();
        article.setTitle(articleDto.getTitle());
        article.setRawContent(articleDto.getContent());
        article.setSource(articleDto.getSource() != null ? articleDto.getSource() : "用户投稿");
        article.setPublishDate(LocalDateTime.now());
        article.setUserId(user.getId());
        article.setUserGenerated(true);
        article.setUploadTime(LocalDateTime.now());

        // Generate a unique URL for user articles
        String userArticleUrl = "/user/articles/" + System.currentTimeMillis() + "-" + user.getId();
        article.setUrl(userArticleUrl);

        // Convert content to HTML format for consistency
        String htmlContent = convertPlainTextToHtml(articleDto.getContent());
        article.setRawHtmlContent(htmlContent);

        // AI Analysis for user-generated content
        try {
            if (articleDto.getContent().length() > 50) {
                logger.info("Requesting AI analysis for user article: {}", article.getTitle());
                
                // Full content translation
                String translatedHtml = aiService.translateFullArticle(htmlContent);
                if (translatedHtml != null && !translatedHtml.isBlank()) {
                    article.setTranslatedContent(translatedHtml);
                    logger.info("Full content translation successful for user article: {}", article.getTitle());
                }

                // Summary, keywords, and category analysis
                AiAnalysisResult analysisResult = aiService.getAiAnalysisForArticle(article.getTitle(), articleDto.getContent());
                if (analysisResult != null) {
                    article.setTitleCn(analysisResult.getTranslatedTitle());
                    article.setSummaryAiCn(analysisResult.getChineseSummary());
                    article.setKeywordsAi(analysisResult.getKeywords());
                    article.setCategoryAi(analysisResult.getPartition());
                    logger.info("AI analysis successful for user article: {}", article.getTitle());
                } else {
                    logger.warn("AI analysis returned null for user article: {}", article.getTitle());
                }
            }
        } catch (Exception e) {
            logger.error("AI analysis failed for user article: {}, error: {}", article.getTitle(), e.getMessage());
            // Continue without AI analysis if it fails
        }

        NewsArticle savedArticle = newsArticleRepository.save(article);
        logger.info("Successfully created user article with ID: {}", savedArticle.getId());
        
        return savedArticle;
    }

    @Transactional
    public NewsArticle updateUserArticle(Long articleId, UserArticleDto articleDto, User user) {
        logger.info("Updating user article ID: {} by user: {}", articleId, user.getUsername());

        Optional<NewsArticle> optionalArticle = newsArticleRepository.findByIdAndUserId(articleId, user.getId());
        if (optionalArticle.isEmpty()) {
            throw new IllegalArgumentException("文章不存在或您没有权限编辑此文章");
        }

        NewsArticle article = optionalArticle.get();
        article.setTitle(articleDto.getTitle());
        article.setRawContent(articleDto.getContent());
        article.setSource(articleDto.getSource() != null ? articleDto.getSource() : "用户投稿");
        
        // Update HTML content
        String htmlContent = convertPlainTextToHtml(articleDto.getContent());
        article.setRawHtmlContent(htmlContent);

        // Re-run AI analysis if content has changed significantly
        try {
            if (articleDto.getContent().length() > 50) {
                logger.info("Re-running AI analysis for updated user article: {}", article.getTitle());
                
                // Full content translation
                String translatedHtml = aiService.translateFullArticle(htmlContent);
                if (translatedHtml != null && !translatedHtml.isBlank()) {
                    article.setTranslatedContent(translatedHtml);
                }

                // Summary, keywords, and category analysis
                AiAnalysisResult analysisResult = aiService.getAiAnalysisForArticle(article.getTitle(), articleDto.getContent());
                if (analysisResult != null) {
                    article.setTitleCn(analysisResult.getTranslatedTitle());
                    article.setSummaryAiCn(analysisResult.getChineseSummary());
                    article.setKeywordsAi(analysisResult.getKeywords());
                    article.setCategoryAi(analysisResult.getPartition());
                    logger.info("AI analysis successful for updated user article: {}", article.getTitle());
                }
            }
        } catch (Exception e) {
            logger.error("AI analysis failed for updated user article: {}, error: {}", article.getTitle(), e.getMessage());
        }

        NewsArticle savedArticle = newsArticleRepository.save(article);
        logger.info("Successfully updated user article with ID: {}", savedArticle.getId());
        
        return savedArticle;
    }

    @Transactional
    public void deleteUserArticle(Long articleId, User user) {
        logger.info("Deleting user article ID: {} by user: {}", articleId, user.getUsername());

        Optional<NewsArticle> optionalArticle = newsArticleRepository.findByIdAndUserId(articleId, user.getId());
        if (optionalArticle.isEmpty()) {
            throw new IllegalArgumentException("文章不存在或您没有权限删除此文章");
        }

        newsArticleRepository.delete(optionalArticle.get());
        logger.info("Successfully deleted user article with ID: {}", articleId);
    }

    public List<NewsArticle> getArticlesByUser(User user) {
        return newsArticleRepository.findByUserIdOrderByPublishDateDesc(user.getId());
    }

    public Optional<NewsArticle> getArticleByIdAndUser(Long articleId, User user) {
        return newsArticleRepository.findByIdAndUserId(articleId, user.getId());
    }

    /**
     * Convert plain text to basic HTML format
     */
    private String convertPlainTextToHtml(String plainText) {
        if (plainText == null || plainText.trim().isEmpty()) {
            return "";
        }

        // Basic HTML conversion: convert line breaks to paragraphs
        String[] paragraphs = plainText.split("\\n\\s*\\n");
        StringBuilder htmlBuilder = new StringBuilder();
        
        for (String paragraph : paragraphs) {
            if (!paragraph.trim().isEmpty()) {
                htmlBuilder.append("<p>")
                          .append(paragraph.trim().replaceAll("\\n", "<br>"))
                          .append("</p>\n");
            }
        }
        
        return htmlBuilder.toString();
    }
}