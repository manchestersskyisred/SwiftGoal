package com.sportslens.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportslens.ai.dto.AiAnalysisResult;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.HTMLHighlighter;
import okhttp3.*;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions";

    @Value("${app.deepseek.api-key}")
    private String deepseekApiKey;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS) // Increase timeout for longer translation tasks
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String translateFullArticle(Document articleDoc) {
        if (isApiKeyInvalid()) {
            logger.warn("DeepSeek API key is not configured. Skipping translation.");
            return null;
        }

        String articleHtml;
        try {
            articleHtml = extractMainArticleHtml(articleDoc);
        } catch (BoilerpipeProcessingException | SAXException e) {
            logger.error("Failed to extract main article content using boilerpipe for URL: {}", articleDoc.location(), e);
            // Fallback to using the full body if extraction fails
            articleHtml = articleDoc.body().html();
        }

        if (articleHtml.isBlank()) {
            logger.warn("Extracted article HTML is blank for URL: {}. Skipping translation.", articleDoc.location());
            return null;
        }

        String systemPrompt = "You are a professional translator. Translate the following HTML content into Chinese. IMPORTANT: Do not translate the content inside `<code>` or `<pre>` tags. Preserve all original HTML tags, including their classes and structure.";
        String userPrompt = String.format("Translate this HTML to Chinese:\\n\\n%s", articleHtml);

        Map<String, Object> payload = Map.of(
                "model", "deepseek-chat",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "max_tokens", 4096 // Increased token limit for full articles
        );

        String jsonPayload = serializePayload(payload);

        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(DEEPSEEK_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + deepseekApiKey)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleApiError(response);
                return null;
            }
            JsonNode rootNode = objectMapper.readTree(response.body().string());
            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (IOException e) {
            logger.error("IOException while calling DeepSeek API for translation", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred during translation", e);
        }
        return null;
    }

    private String extractMainArticleHtml(Document articleDoc) throws BoilerpipeProcessingException, SAXException {
        InputSource is = new InputSource(new StringReader(articleDoc.html()));
        BoilerpipeSAXInput in = new BoilerpipeSAXInput(is);
        TextDocument doc = in.getTextDocument();
        ArticleExtractor.INSTANCE.process(doc);

        HTMLHighlighter highlighter = HTMLHighlighter.newExtractingInstance();
        return highlighter.process(doc, articleDoc.html());
    }

    public AiAnalysisResult getAiAnalysisForArticle(String title, String content) {
        if (isApiKeyInvalid()) {
            logger.warn("DeepSeek API key is not configured. Skipping AI analysis.");
            return null;
        }

        String truncatedContent = content.length() > 4000 ? content.substring(0, 4000) : content;

        String systemPrompt = "You are a professional sports news editor. Analyze the provided article and return a JSON object with four fields: 'translatedTitle' (Chinese translation of the title), 'chineseSummary' (a one-sentence summary in Chinese), 'keywords' (3-5 relevant English keywords, comma-separated), and 'partition' (classify the article into one of the following categories: NBA, 英超, 西甲, 德甲, 法甲, 意甲, 沙特联, 综合体育).";
        
        String userPrompt = String.format("Title: \"%s\"\\n\\nArticle Content: \"%s\"", title, truncatedContent);

        Map<String, Object> payload = Map.of(
            "model", "deepseek-chat",
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            ),
            "max_tokens", 1024,
            "response_format", Map.of("type", "json_object")
        );

        String jsonPayload = serializePayload(payload);
        if (jsonPayload == null) return null;

        Request request = buildRequest(jsonPayload);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleApiError(response);
                return null;
            }

            String responseBody = response.body().string();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            String jsonContentString = rootNode.path("choices").get(0).path("message").path("content").asText();
            
            logger.debug("Received JSON from AI: {}", jsonContentString);

            return objectMapper.readValue(jsonContentString, AiAnalysisResult.class);

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse JSON response from DeepSeek API: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("IOException while calling DeepSeek API", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred in AIService", e);
        }
        return null;
    }

    private boolean isApiKeyInvalid() {
        return deepseekApiKey == null || deepseekApiKey.startsWith("YOUR_") || deepseekApiKey.trim().isEmpty();
    }

    private String serializePayload(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize request payload for DeepSeek API", e);
            return null;
        }
    }

    private Request buildRequest(String jsonPayload) {
        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));
        return new Request.Builder()
                .url(DEEPSEEK_API_URL)
                .header("Authorization", "Bearer " + deepseekApiKey)
                .post(body)
                .build();
    }

    private void handleApiError(Response response) throws IOException {
        String errorBody = response.body() != null ? response.body().string() : "No response body";
        logger.error("Failed to call DeepSeek API: {} - {}", response.code(), errorBody);
    }
}