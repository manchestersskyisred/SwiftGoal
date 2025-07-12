package com.sportslens.ai.service;

import com.sportslens.ai.domain.NewsArticle;
import com.sportslens.ai.dto.AiAnalysisResult;
import com.sportslens.ai.repository.NewsArticleRepository;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class CrawlerService {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);
    private static final List<String> USER_AGENTS = List.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:107.0) Gecko/20100101 Firefox/107.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.1 Safari/605.1.15"
    );
    private static final Random RANDOM = new Random();

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    @Autowired
    private AIService aiService;

    // Run 10 seconds after startup, then every hour
    @Scheduled(initialDelay = 10000, fixedRate = 3600000)
    public void crawlAndSaveArticles() {
        logger.info("Starting scheduled crawl...");
        crawlEspnNews();
        crawlTransfermarktNews();
        crawlNbcSportsNews();
        crawlSkySportsNews();
        crawlFoxSportsNews();
        logger.info("Scheduled crawl finished.");
    }

    public void crawlEspnNews() {
        String espnRssUrl = "https://espn.com/espn/rss/news";
        logger.info("Crawling ESPN sports news from RSS feed: {}", espnRssUrl);
        try {
            Document doc = getXmlWithRetries(espnRssUrl, espnRssUrl);
            Elements items = doc.select("item");
            logger.info("ESPN RSS: Found {} items.", items.size());
            if (items.isEmpty()) {
                logger.warn("ESPN RSS: No items found in feed. Feed might be empty or structure changed. XML dump:");
                logger.warn(doc.html());
            }

            for (Element item : items) {
                String title = item.select("title").text();
                String absoluteUrl = item.select("link").text();
                
                if (absoluteUrl.isEmpty() || newsArticleRepository.findByUrl(absoluteUrl).isPresent()) {
                    continue;
                }
                
                if (title.isEmpty()) continue;
                logger.debug("ESPN RSS: Processing new article '{}' at URL {}", title, absoluteUrl);

                NewsArticle article = new NewsArticle();
                article.setTitle(title);
                article.setUrl(absoluteUrl);
                article.setSource("ESPN");
                article.setPublishDate(LocalDateTime.now());

                try {
                    Document articleDoc = getDocumentWithRetries(absoluteUrl, espnRssUrl);
                    if (articleDoc == null || articleDoc.body() == null || articleDoc.body().html().isEmpty()) {
                        logger.warn("Fetched document for {} is empty, skipping article.", absoluteUrl);
                        continue;
                    }
                    article.setRawHtmlContent(articleDoc.body().html()); // Save raw HTML
                    String content = ArticleExtractor.INSTANCE.getText(articleDoc.html()); // Extract clean text
                    article.setRawContent(content);

                    // AI Analysis
                    if (content.length() > 50) { // Only analyze if content is substantial
                        logger.info("Requesting AI analysis for article: {}", article.getTitle());
                        // 1. Full content translation
                        String translatedHtml = aiService.translateFullArticle(articleDoc);
                        if (translatedHtml != null && !translatedHtml.isBlank()) {
                            article.setTranslatedContent(translatedHtml);
                            logger.info("Full content translation successful for: {}", article.getTitle());
                        }

                        // 2. Summary, keywords, and category analysis
                        AiAnalysisResult analysisResult = aiService.getAiAnalysisForArticle(article.getTitle(), content);
                        if (analysisResult != null) {
                            article.setTitleCn(analysisResult.getTranslatedTitle());
                            article.setSummaryAiCn(analysisResult.getChineseSummary());
                            article.setKeywordsAi(analysisResult.getKeywords());
                            article.setCategoryAi(analysisResult.getPartition());
                            logger.info("AI analysis successful for: {}", article.getTitle());
                        } else {
                            logger.warn("AI analysis returned null for: {}", article.getTitle());
                        }
                    } else {
                        logger.warn("Content for '{}' is too short or empty, skipping AI analysis and translation.", article.getTitle());
                        article.setTranslatedContent(null);
                    }

                } catch (IOException e) {
                    logger.error("Could not fetch full content for {}: {}", title, e.getMessage());
                }

                newsArticleRepository.save(article);
                logger.info("Saved new ESPN article: {}", title);
            }
        } catch (Exception e) {
            logger.error("Error crawling ESPN news from RSS feed: {}", e.getMessage(), e);
        }
    }

    public void crawlTransfermarktNews() {
        String tmRssUrl = "https://www.transfermarkt.co.uk/rss/news";
        logger.info("Crawling Transfermarkt news from RSS feed: {}", tmRssUrl);
        try {
            Document doc = getXmlWithRetries(tmRssUrl, tmRssUrl);
            Elements items = doc.select("item");
            logger.info("Transfermarkt RSS: Found {} items.", items.size());
            if (items.isEmpty()) {
                logger.warn("Transfermarkt RSS: No items found in feed. Feed might be empty or structure changed. XML dump:");
                logger.warn(doc.html());
            }

            for (Element item : items) {
                String title = item.select("title").text();
                String absoluteUrl = item.select("link").text();
                
                if (absoluteUrl.isEmpty() || newsArticleRepository.findByUrl(absoluteUrl).isPresent()) {
                    continue;
                }
                
                if (title.isEmpty()) continue;
                logger.debug("Transfermarkt RSS: Processing new article '{}' at URL {}", title, absoluteUrl);

                NewsArticle article = new NewsArticle();
                article.setTitle(title);
                article.setUrl(absoluteUrl);
                article.setSource("Transfermarkt");
                article.setPublishDate(LocalDateTime.now());

                try {
                    Document articleDoc = getDocumentWithRetries(absoluteUrl, tmRssUrl);
                     if (articleDoc == null || articleDoc.body() == null || articleDoc.body().html().isEmpty()) {
                        logger.warn("Fetched document for {} is empty, skipping article.", absoluteUrl);
                        continue;
                    }
                    article.setRawHtmlContent(articleDoc.body().html()); // Save raw HTML
                    String content = ArticleExtractor.INSTANCE.getText(articleDoc.html()); // Extract clean text
                    article.setRawContent(content);
                    
                    // AI Analysis
                    if (content.length() > 50) {
                        logger.info("Requesting AI analysis for article: {}", article.getTitle());
                        // 1. Full content translation
                        String translatedHtml = aiService.translateFullArticle(articleDoc);
                        if (translatedHtml != null && !translatedHtml.isBlank()) {
                            article.setTranslatedContent(translatedHtml);
                            logger.info("Full content translation successful for: {}", article.getTitle());
                        }

                        // 2. Summary, keywords, and category analysis
                        AiAnalysisResult analysisResult = aiService.getAiAnalysisForArticle(article.getTitle(), content);
                        if (analysisResult != null) {
                            article.setTitleCn(analysisResult.getTranslatedTitle());
                            article.setSummaryAiCn(analysisResult.getChineseSummary());
                            article.setKeywordsAi(analysisResult.getKeywords());
                            article.setCategoryAi(analysisResult.getPartition());
                            logger.info("AI analysis successful for: {}", article.getTitle());
                        } else {
                            logger.warn("AI analysis returned null for: {}", article.getTitle());
                        }
                    } else {
                        logger.warn("Content for '{}' is too short or empty, skipping AI analysis and translation.", article.getTitle());
                        article.setTranslatedContent(null);
                    }

                } catch (IOException e) {
                    logger.error("Could not fetch full content for {}: {}", title, e.getMessage());
                }
                
                newsArticleRepository.save(article);
                logger.info("Saved new Transfermarkt article: {}", title);
            }
        } catch (Exception e) {
            logger.error("Error crawling Transfermarkt news from RSS feed: {}", e.getMessage(), e);
        }
    }

    public void crawlNbcSportsNews() {
        String nbcRssUrl = "https://www.sportsengine.com/soccer-rss-feed";
        logger.info("Crawling NBC Sports news from RSS feed: {}", nbcRssUrl);
        try {
            Document doc = getXmlWithRetries(nbcRssUrl, nbcRssUrl);
            Elements items = doc.select("item");
            logger.info("NBC Sports RSS: Found {} items.", items.size());
            if (items.isEmpty()) {
                logger.warn("NBC Sports RSS: No items found in feed. Feed might be empty or structure changed. XML dump:");
                logger.warn(doc.html());
            }

            for (Element item : items) {
                String title = item.select("title").text();
                String absoluteUrl = item.select("link").text();

                if (absoluteUrl.isEmpty() || newsArticleRepository.findByUrl(absoluteUrl).isPresent()) {
                    continue;
                }

                if (title.isEmpty()) continue;
                logger.debug("NBC Sports RSS: Processing new article '{}' at URL {}", title, absoluteUrl);

                NewsArticle article = new NewsArticle();
                article.setTitle(title);
                article.setUrl(absoluteUrl);
                article.setSource("NBC Sports");
                article.setPublishDate(LocalDateTime.now());

                try {
                    Document articleDoc = getDocumentWithRetries(absoluteUrl, absoluteUrl);
                    if (articleDoc == null || articleDoc.body() == null || articleDoc.body().html().isEmpty()) {
                        logger.warn("Fetched document for {} is empty, skipping article.", absoluteUrl);
                        continue;
                    }
                    article.setRawHtmlContent(articleDoc.body().html());
                    String content = ArticleExtractor.INSTANCE.getText(articleDoc.html());
                    article.setRawContent(content);

                    if (content.length() > 50) {
                        logger.info("Requesting AI analysis for article: {}", article.getTitle());
                        // 1. Full content translation
                        String translatedHtml = aiService.translateFullArticle(articleDoc);
                        if (translatedHtml != null && !translatedHtml.isBlank()) {
                            article.setTranslatedContent(translatedHtml);
                            logger.info("Full content translation successful for: {}", article.getTitle());
                        }

                        // 2. Summary, keywords, and category analysis
                        AiAnalysisResult analysisResult = aiService.getAiAnalysisForArticle(article.getTitle(), content);
                        if (analysisResult != null) {
                            article.setTitleCn(analysisResult.getTranslatedTitle());
                            article.setSummaryAiCn(analysisResult.getChineseSummary());
                            article.setKeywordsAi(analysisResult.getKeywords());
                            article.setCategoryAi(analysisResult.getPartition());
                            logger.info("AI analysis successful for: {}", article.getTitle());
                        } else {
                            logger.warn("AI analysis returned null for: {}", article.getTitle());
                        }
                    } else {
                        logger.warn("Content for '{}' is too short or empty, skipping AI analysis and translation.", article.getTitle());
                        article.setTranslatedContent(null);
                    }

                } catch (IOException e) {
                    logger.error("Could not fetch full content for {}: {}", title, e.getMessage());
                }

                newsArticleRepository.save(article);
                logger.info("Saved new NBC Sports article: {}", title);
            }
        } catch (Exception e) {
            logger.error("Error crawling NBC Sports news from RSS feed: {}", e.getMessage(), e);
        }
    }

    public void crawlSkySportsNews() {
        String skyRssUrl = "https://www.skysports.com/rss/11095";
        logger.info("Crawling Sky Sports news from RSS feed: {}", skyRssUrl);
        try {
            Document doc = getXmlWithRetries(skyRssUrl, skyRssUrl);
            Elements items = doc.select("item");
            logger.info("Sky Sports RSS: Found {} items.", items.size());
            if (items.isEmpty()) {
                logger.warn("Sky Sports RSS: No items found in feed. Feed might be empty or structure changed. XML dump:");
                logger.warn(doc.html());
            }

            for (Element item : items) {
                String title = item.select("title").text();
                String absoluteUrl = item.select("link").text();
                
                if (absoluteUrl.isEmpty() || newsArticleRepository.findByUrl(absoluteUrl).isPresent()) {
                    continue;
                }
                
                if (title.isEmpty()) continue;
                logger.debug("Sky Sports RSS: Processing new article '{}' at URL {}", title, absoluteUrl);

                NewsArticle article = new NewsArticle();
                article.setTitle(title);
                article.setUrl(absoluteUrl);
                article.setSource("Sky Sports");
                article.setPublishDate(LocalDateTime.now());

                try {
                    Document articleDoc = getDocumentWithRetries(absoluteUrl, skyRssUrl);
                    if (articleDoc == null || articleDoc.body() == null || articleDoc.body().html().isEmpty()) {
                        logger.warn("Fetched document for {} is empty, skipping article.", absoluteUrl);
                        continue;
                    }
                    article.setRawHtmlContent(articleDoc.body().html());
                    String content = ArticleExtractor.INSTANCE.getText(articleDoc.html());
                    article.setRawContent(content);

                    // AI Analysis
                    if (content.length() > 50) {
                        logger.info("Requesting AI analysis for article: {}", article.getTitle());
                        // 1. Full content translation
                        String translatedHtml = aiService.translateFullArticle(articleDoc);
                        if (translatedHtml != null && !translatedHtml.isBlank()) {
                            article.setTranslatedContent(translatedHtml);
                            logger.info("Full content translation successful for: {}", article.getTitle());
                        }

                        // 2. Summary, keywords, and category analysis
                        AiAnalysisResult analysisResult = aiService.getAiAnalysisForArticle(article.getTitle(), content);
                        if (analysisResult != null) {
                            article.setTitleCn(analysisResult.getTranslatedTitle());
                            article.setSummaryAiCn(analysisResult.getChineseSummary());
                            article.setKeywordsAi(analysisResult.getKeywords());
                            article.setCategoryAi(analysisResult.getPartition());
                            logger.info("AI analysis successful for: {}", article.getTitle());
                        } else {
                            logger.warn("AI analysis returned null for: {}", article.getTitle());
                        }
                    } else {
                        logger.warn("Content for '{}' is too short or empty, skipping AI analysis and translation.", article.getTitle());
                        article.setTranslatedContent(null);
                    }

                } catch (IOException e) {
                    logger.error("Could not fetch full content for {}: {}", title, e.getMessage());
                }

                newsArticleRepository.save(article);
                logger.info("Saved new Sky Sports article: {}", title);
            }
        } catch (Exception e) {
            logger.error("Error crawling Sky Sports news from RSS feed: {}", e.getMessage(), e);
        }
    }

    public void crawlFoxSportsNews() {
        String foxRssUrl = "https://api.foxsports.com/v2/content/optimized-rss?partnerKey=MB0Wehpmuj2lUhuRhQaafhBjAJqaPU244mlTDK1i&size=50";
        logger.info("Crawling Fox Sports news from RSS feed: {}", foxRssUrl);
        try {
            Document doc = getXmlWithRetries(foxRssUrl, foxRssUrl);
            Elements items = doc.select("item");
            logger.info("Fox Sports RSS: Found {} items.", items.size());
            if (items.isEmpty()) {
                logger.warn("Fox Sports RSS: No items found in feed. Feed might be empty or structure changed. XML dump:");
                logger.warn(doc.html());
            }

            for (Element item : items) {
                String title = item.select("title").text();
                String absoluteUrl = item.select("link").text();

                if (absoluteUrl.isEmpty() || newsArticleRepository.findByUrl(absoluteUrl).isPresent()) {
                    continue;
                }

                if (title.isEmpty()) continue;
                logger.debug("Fox Sports RSS: Processing new article '{}' at URL {}", title, absoluteUrl);


                NewsArticle article = new NewsArticle();
                article.setTitle(title);
                article.setUrl(absoluteUrl);
                article.setSource("Fox Sports");
                article.setPublishDate(LocalDateTime.now());

                try {
                    Document articleDoc = getDocumentWithRetries(absoluteUrl, foxRssUrl);
                    if (articleDoc == null || articleDoc.body() == null || articleDoc.body().html().isEmpty()) {
                        logger.warn("Fetched document for {} is empty, skipping article.", absoluteUrl);
                        continue;
                    }
                    article.setRawHtmlContent(articleDoc.body().html());
                    String content = ArticleExtractor.INSTANCE.getText(articleDoc.html());
                    article.setRawContent(content);

                    // AI Analysis
                    if (content.length() > 50) {
                        logger.info("Requesting AI analysis for article: {}", article.getTitle());
                        // 1. Full content translation
                        String translatedHtml = aiService.translateFullArticle(articleDoc);
                        if (translatedHtml != null && !translatedHtml.isBlank()) {
                            article.setTranslatedContent(translatedHtml);
                            logger.info("Full content translation successful for: {}", article.getTitle());
                        }

                        // 2. Summary, keywords, and category analysis
                        AiAnalysisResult analysisResult = aiService.getAiAnalysisForArticle(article.getTitle(), content);
                        if (analysisResult != null) {
                            article.setTitleCn(analysisResult.getTranslatedTitle());
                            article.setSummaryAiCn(analysisResult.getChineseSummary());
                            article.setKeywordsAi(analysisResult.getKeywords());
                            article.setCategoryAi(analysisResult.getPartition());
                            logger.info("AI analysis successful for: {}", article.getTitle());
                        } else {
                            logger.warn("AI analysis returned null for: {}", article.getTitle());
                        }
                    } else {
                        logger.warn("Content for '{}' is too short or empty, skipping AI analysis and translation.", article.getTitle());
                        article.setTranslatedContent(null);
                    }

                } catch (IOException e) {
                    logger.error("Could not fetch full content for {}: {}", title, e.getMessage());
                }

                newsArticleRepository.save(article);
                logger.info("Saved new Fox Sports article: {}", title);
            }
        } catch (Exception e) {
            logger.error("Error crawling Fox Sports news from RSS feed: {}", e.getMessage(), e);
        }
    }

    private String getRandomUserAgent() {
        return USER_AGENTS.get(RANDOM.nextInt(USER_AGENTS.size()));
    }

    private Document getDocumentWithRetries(String url, String referer) throws IOException {
        int retries = 3;
        IOException lastException = null;
        for (int i = 0; i < retries; i++) {
            try {
                // Add a small random delay to be less predictable
                TimeUnit.MILLISECONDS.sleep(500 + RANDOM.nextInt(1000));
                return Jsoup.connect(url)
                        .userAgent(getRandomUserAgent())
                        .referrer(referer)
                        .get();
            } catch (IOException e) {
                lastException = e;
                logger.warn("Attempt {} to fetch {} failed. Retrying...", i + 1, url);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Crawling interrupted", e);
                break;
            }
        }
        if (lastException != null) {
            logger.error("Failed to fetch document from " + url + " after " + retries + " retries.", lastException);
        }
        return null;
    }

    private Document getXmlWithRetries(String url, String referer) throws IOException {
        int retries = 3;
        while (retries > 0) {
            try {
                String xmlString = Jsoup.connect(url)
                        .userAgent(getRandomUserAgent())
                        .referrer(referer)
                        .timeout(30000)
                        .execute().body();
                return Jsoup.parse(xmlString, "", org.jsoup.parser.Parser.xmlParser());
            } catch (IOException e) {
                retries--;
                if (retries == 0) {
                    throw e;
                }
                logger.warn("Retrying connection to {} ({} retries left)", url, retries);
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new IOException("Failed to connect to " + url + " after multiple retries");
    }
}