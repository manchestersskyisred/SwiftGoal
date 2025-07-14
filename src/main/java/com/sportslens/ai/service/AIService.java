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

    public String translateBatch(String combinedText, String separator) {
        if (isApiKeyInvalid()) {
            logger.warn("DeepSeek API key is not configured. Skipping batch translation.");
            return null;
        }
        if (combinedText == null || combinedText.isBlank()) {
            logger.warn("Input for batch translation is blank. Skipping.");
            return null;
        }

        String systemPrompt = String.format(
            "You are a professional translator. Your task is to translate a batch of text segments provided by the user. " +
            "The segments are separated by a special marker: `%s`. You MUST translate each segment individually and " +
            "return the translated segments, also separated by the exact same marker `%s`, maintaining the original order and segment count. " +
            "Example Input: Hello world%sThis is a test. " +
            "Example Output: 你好世界%s这是一个测试。",
            separator, separator, separator, separator
        );
        String userPrompt = String.format("Translate the following text segments:\n%s", combinedText);

        Map<String, Object> payload = Map.of(
            "model", "deepseek-chat",
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            ),
            "max_tokens", 4096
        );

        String jsonPayload = serializePayload(payload);
        Request request = buildRequest(jsonPayload);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleApiError(response);
                return null;
            }
            String responseBody = response.body().string();
            logger.debug("DeepSeek API Response Body (translateBatch): {}", responseBody);
            JsonNode rootNode = objectMapper.readTree(responseBody);
            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (IOException e) {
            logger.error("IOException while calling DeepSeek API for batch translation", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred during batch translation", e);
        }
        return null;
    }

    public AiAnalysisResult getAiAnalysisForArticle(String title, String content) {
        if (isApiKeyInvalid()) {
            logger.warn("DeepSeek API key is not configured. Skipping AI analysis.");
            return null;
        }

        String truncatedContent = content.length() > 4000 ? content.substring(0, 4000) : content;

        String systemPrompt = "You are a professional sports news editor. Analyze the provided article and return a JSON object with four fields: 'translatedTitle' (Chinese translation of the title), 'chineseSummary' (a one-sentence summary in Chinese), 'keywords' (3-5 relevant English keywords, comma-separated), and 'partition'. For the 'partition' field, you MUST classify the article into ONLY ONE of the following categories: NBA, 英超, 西甲, 德甲, 法甲, 意甲, 沙特联, 女足, MLB, 网球, 综合体育. The value for 'partition' must exactly match one of the items from this list.";
        
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
            logger.debug("DeepSeek API Response Body (getAiAnalysisForArticle): {}", responseBody);
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

    public String cleanTranslatedHtml(String translatedHtml) {
        if (isApiKeyInvalid()) {
            logger.warn("DeepSeek API key is not configured. Skipping HTML cleaning.");
            return null; // Return null to indicate failure/skip
        }

        String systemPrompt = "You are an expert HTML content cleaner. Your task is to analyze the provided HTML snippet, which contains a news article translated into Chinese. " +
                            "Identify and remove all non-essential elements (navigation, ads, footers, etc.) and user interaction prompts ('注册', '登录'). " +
                            "Return a JSON object with two fields: 'status' and 'html'. " +
                            "If you successfully cleaned the article and only the core content remains, set 'status' to 'success' and put the cleaned HTML in the 'html' field. " +
                            "If the input is junk, empty, or cannot be cleaned, set 'status' to 'failure'.";

        String userPrompt = String.format("Clean the following HTML content:\n%s", translatedHtml);

        Map<String, Object> payload = Map.of(
            "model", "deepseek-chat",
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            ),
            "max_tokens", 4096,
            "response_format", Map.of("type", "json_object")
        );

        String jsonPayload = serializePayload(payload);
        Request request = buildRequest(jsonPayload);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleApiError(response);
                return null;
            }
            String responseBody = response.body().string();
            logger.debug("DeepSeek API Response Body (cleanTranslatedHtml): {}", responseBody);
            
            JsonNode rootNode = objectMapper.readTree(responseBody);
            String jsonContentString = rootNode.path("choices").get(0).path("message").path("content").asText();
            JsonNode contentJson = objectMapper.readTree(jsonContentString);

            if (contentJson.has("status") && "success".equals(contentJson.get("status").asText())) {
                logger.info("AI HTML cleaning reported SUCCESS.");
                return contentJson.get("html").asText();
            } else {
                logger.warn("AI HTML cleaning reported FAILURE or malformed JSON.");
                return null;
            }

        } catch (IOException e) {
            logger.error("IOException while calling DeepSeek API for HTML cleaning", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred during HTML cleaning", e);
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