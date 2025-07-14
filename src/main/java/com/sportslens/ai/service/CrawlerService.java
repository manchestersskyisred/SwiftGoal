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
import org.springframework.beans.factory.annotation.Value;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.HTMLHighlighter;
import de.l3s.boilerpipe.document.TextDocument;
import java.io.IOException;
import java.io.StringReader;
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

    @Autowired
    private HtmlTranslator htmlTranslator;

    @Value("${app.crawler.rss-feeds}")
    private List<String> rssFeeds;

    // Run 10 seconds after startup, then every 15 minutes
    @Scheduled(initialDelay = 10000, fixedRate = 900000)
    public void crawlAndSaveArticles() {
        logger.info("Starting scheduled crawl...");
        // This is a consolidated list. The specific crawl methods below will be removed.
        for (String feedUrl : rssFeeds) {
            crawlRssFeed(feedUrl);
        }
        logger.info("Scheduled crawl finished.");
    }

    private void crawlRssFeed(String rssUrl) {
        String sourceName = extractSourceFromUrl(rssUrl);
        logger.info("Crawling {} from RSS feed: {}", sourceName, rssUrl);
        try {
            Document doc = getXmlWithRetries(rssUrl, rssUrl);
            Elements items = doc.select("item");
            logger.info("{} RSS: Found {} items.", sourceName, items.size());
            if (items.isEmpty()) {
                logger.warn("{} RSS: No items found. Feed structure might have changed.", sourceName);
                return;
            }

            for (Element item : items) {
                String title = item.select("title").text();
                String link = item.select("link").text();

                if (link.isEmpty() || newsArticleRepository.findByUrl(link).isPresent()) {
                    continue;
                }
                if (title.isEmpty()) continue;

                logger.debug("{} RSS: Processing new article '{}'", sourceName, title);

                try {
                    Document articleDoc = getDocumentWithRetries(link, rssUrl);
                    if (articleDoc == null) {
                        logger.warn("Fetched document for {} is null, skipping.", link);
                        continue;
                    }

                    NewsArticle article = new NewsArticle();
                    article.setTitle(title);
                    article.setUrl(link);
                    article.setSource(sourceName);
                    article.setPublishDate(LocalDateTime.now());
                    
                    analyzeAndSaveArticle(article, articleDoc);

                } catch (Exception e) {
                    logger.error("Failed to process article from link {}: {}", link, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Error crawling {} RSS feed: {}", sourceName, e.getMessage(), e);
        }
    }

    private void analyzeAndSaveArticle(NewsArticle article, Document articleDoc) {
        String mainContentHtml = extractMainHtml(articleDoc);
        if (mainContentHtml.isBlank()) {
            logger.warn("Extracted empty main content for URL: {}. Skipping article.", article.getUrl());
            return;
        }
        
        article.setRawHtmlContent(mainContentHtml);

        // Step 1: Translate the raw HTML
        String translatedHtml = htmlTranslator.translateHtmlContent(mainContentHtml);

        // Step 2: Clean the translated HTML. The AI service will return null on failure.
        String cleanedHtml = aiService.cleanTranslatedHtml(translatedHtml);

        // Step 3: Check if the cleaning was successful.
        if (cleanedHtml == null || cleanedHtml.isBlank()) {
            article.setTranslatedContent(null);
            article.setTranslationStatus(0);
            logger.warn("AI-based HTML cleaning for {} failed. Article will not be published.", article.getTitle());
            newsArticleRepository.save(article);
            return;
        }

        // The process was successful.
        String cleanedContentText = Jsoup.parse(cleanedHtml).text();

        article.setRawContent(cleanedContentText); // Use cleaned text for raw content
        article.setTranslatedContent(cleanedHtml);
        article.setTranslationStatus(1);

        if (cleanedContentText.length() < 100) {
            logger.warn("Content for '{}' is too short after cleaning (<100 chars), skipping AI analysis.", article.getTitle());
            newsArticleRepository.save(article);
            return;
        }

        AiAnalysisResult analysis = aiService.getAiAnalysisForArticle(article.getTitle(), cleanedContentText);
        if (analysis != null) {
            article.setTitleCn(analysis.getTranslatedTitle());
            article.setSummaryAiCn(analysis.getChineseSummary());
            article.setKeywordsAi(analysis.getKeywords());
            article.setCategoryAi(analysis.getPartition());
        } else {
            logger.warn("AI analysis (summary/category) failed for '{}' after cleaning.", article.getTitle());
        }

        newsArticleRepository.save(article);
        logger.info("Successfully processed and saved article from URL: {}", article.getUrl());
    }
    
    private String extractSourceFromUrl(String rssUrl) {
        if (rssUrl.contains("espn.com")) return "ESPN";
        if (rssUrl.contains("transfermarkt")) return "Transfermarkt";
        if (rssUrl.contains("sportsengine.com")) return "NBC Sports";
        if (rssUrl.contains("skysports.com")) return "Sky Sports";
        if (rssUrl.contains("foxsports.com")) return "Fox Sports";
        return "Unknown";
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

    private String extractMainHtml(Document doc) {
        try {
            // Use Boilerpipe's ArticleExtractor to get the core article HTML, preserving structure.
            // This is the most robust method, as suggested.
            InputSource is = new InputSource(new StringReader(doc.html()));
            BoilerpipeSAXInput in = new BoilerpipeSAXInput(is);
            TextDocument textDoc = in.getTextDocument();
            ArticleExtractor.INSTANCE.process(textDoc);
            String mainContentHtml = HTMLHighlighter.newExtractingInstance().process(textDoc, doc.html());

            if (mainContentHtml == null || mainContentHtml.isBlank()) {
                logger.warn("Boilerpipe returned empty content for {}. Falling back to full body.", doc.location());
                return doc.body().html(); // Fallback to full body if boilerpipe fails
            }
            
            logger.info("Boilerpipe extraction successful for {}.", doc.location());
            return mainContentHtml;

        } catch (Exception e) {
            logger.error("Boilerpipe extraction failed for URL: {}. Returning empty string.", doc.location(), e);
            // Return empty string to prevent downstream processing of potentially junk-filled body
            return "";
        }
    }
}