package com.swiftgoal.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftgoal.app.dto.AiAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    @Value("${app.deepseek.api-key}")
    private String deepSeekApiKey;

    private final String deepSeekApiUrl = "https://api.deepseek.com/chat/completions";

    public AiAnalysisResult getAiAnalysisForArticle(String title, String content) {
        String prompt = "我将为您提供一个体育新闻的标题和正文。请分析它们并以JSON格式返回以下信息： " +
                "1. `translatedTitle`: 将标题翻译成简体中文。 " +
                "2. `chineseSummary`: 生成一个80-120字的简体中文摘要。 " +
                "3. `keywords`: 提取3-5个相关的简体中文关键词，以逗号分隔。 " +
                "4. `partition`: 将文章分类到以下之一的简体中文分区：NBA, 英超, 西甲, 德甲, 法甲, 意甲, 沙特联, 女足, MLB, 网球, 综合体育。 " +
                "标题是：\"" + title + "\"。正文是：\"" + content.substring(0, Math.min(content.length(), 3500)) + "\"。";

        try {
            String jsonResponse = makeDeepSeekRequest(prompt);
            return parseJsonResponse(jsonResponse);
        } catch (Exception e) {
            logger.error("Error during AI analysis for title '{}': {}", title, e.getMessage(), e);
            return null;
        }
    }
    
    public String cleanTranslatedHtml(String htmlContent) {
        if (htmlContent == null || htmlContent.isBlank()) {
            return null;
        }

        String prompt = "Here is an HTML content snippet that has been machine-translated. " +
                      "Please clean it up by fixing any obvious translation errors, correcting broken HTML tags, " +
                      "and removing any non-content elements like ads, scripts, or navigation bars. " +
                      "Ensure the output is well-formed HTML, preserving the main article's structure. " +
                      "Do not add any explanations, just return the cleaned HTML code. The content is: \n\n" +
                      htmlContent;
        try {
            // Directly return the extracted content from the response
            return makeDeepSeekRequest(prompt);
        } catch (Exception e) {
            logger.error("Error cleaning HTML content via AI: {}", e.getMessage(), e);
            return htmlContent; // Return original content on failure
        }
    }

    public String translateBatch(String text, String separator) {
        String prompt = "我将为您提供一段由 '" + separator + "' 分隔的文本。请将每个部分翻译成简体中文，并使用相同的分隔符 '" + separator + "' 将翻译后的文本连接起来。请直接返回翻译后的文本，不要添加任何额外的解释。要翻译的文本是：\n" + text;
        try {
            // Directly return the extracted content from the response
            return makeDeepSeekRequest(prompt);
        } catch (Exception e) {
            logger.error("Error during batch translation: {}", e.getMessage(), e);
            return text; // Return original text on failure
        }
    }

    private String makeDeepSeekRequest(String prompt) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + deepSeekApiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("model", "deepseek-chat");
        
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        
        body.put("messages", new Map[]{message});

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                logger.info("Making DeepSeek API request (attempt {})...", retries + 1);
                ResponseEntity<String> response = restTemplate.exchange(deepSeekApiUrl, HttpMethod.POST, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("DeepSeek API request successful.");
                    // Extract the content directly from the response
                    return extractContentFromResponse(response.getBody());
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429) {
                    retries++;
                    logger.warn("Rate limit exceeded. Retrying in {} ms (attempt {}/{})", RETRY_DELAY_MS * retries, retries, MAX_RETRIES);
                    try {
                        TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS * retries);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry sleep", ie);
                    }
                } else {
                    logger.error("HTTP error from DeepSeek: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
                    throw e; // Re-throw the original exception
                }
            }
        }
        throw new IOException("Failed to get a successful response from DeepSeek after " + MAX_RETRIES + " retries.");
    }

    private String extractContentFromResponse(String jsonResponse) throws IOException {
        if (jsonResponse == null) {
            throw new IOException("Received null response from API.");
        }
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode choices = rootNode.path("choices");
        if (choices.isArray() && !choices.isEmpty()) {
            return choices.get(0).path("message").path("content").asText();
        }
        throw new IOException("Invalid response format: 'choices' array is missing or empty.");
    }
    
    private AiAnalysisResult parseJsonResponse(String jsonString) {
        try {
            logger.debug("Parsing AI JSON response: {}", jsonString);

            // The string is now the direct content, which should be the JSON block
            String jsonBlock = jsonString; 
            
            // Minor cleanup in case the AI wraps the JSON in markdown
            if (jsonBlock.startsWith("```json")) {
                jsonBlock = jsonBlock.substring(7, jsonBlock.length() - 3).trim();
            } else {
                 Matcher matcher = Pattern.compile("\\{.*\\}", Pattern.DOTALL).matcher(jsonBlock);
                 if (matcher.find()) {
                    jsonBlock = matcher.group();
                 } else {
                    logger.warn("No JSON object found in the content: {}", jsonString);
                    return null;
                 }
            }

            logger.debug("Extracted JSON block for parsing: {}", jsonBlock);
            AiAnalysisResult result = objectMapper.readValue(jsonBlock, AiAnalysisResult.class);
            
            // Basic validation
            if (result.getTranslatedTitle() == null || result.getChineseSummary() == null) {
                 logger.warn("Parsed JSON, but key fields are null. Original content: {}", jsonString);
                 return null;
            }
            
            logger.info("Successfully parsed AI analysis result.");
            return result;

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse JSON from AI response: {}", jsonString, e);
            return null;
        }
    }
}