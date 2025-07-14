package com.sportslens.ai.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class HtmlTranslator {

    private static final Logger logger = LoggerFactory.getLogger(HtmlTranslator.class);
    private final AIService aiService;
    private static final String SEPARATOR = "|||---|||";

    @Autowired
    public HtmlTranslator(AIService aiService) {
        this.aiService = aiService;
    }

    public String translateHtmlContent(String originalHtml) {
        if (originalHtml == null || originalHtml.isBlank()) {
            return originalHtml;
        }

        Document doc = Jsoup.parseBodyFragment(originalHtml);
        Element body = doc.body();

        List<String> textsToTranslate = new ArrayList<>();
        body.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    TextNode textNode = (TextNode) node;
                    String text = textNode.getWholeText().trim();
                    if (!text.isEmpty()) {
                        textsToTranslate.add(text);
                    }
                }
            }
            @Override
            public void tail(Node node, int depth) {}
        });

        if (textsToTranslate.isEmpty()) {
            return originalHtml;
        }

        String combinedText = String.join(SEPARATOR, textsToTranslate);
        String translatedCombinedText = aiService.translateBatch(combinedText, SEPARATOR);

        if (translatedCombinedText == null) {
            logger.error("Batch translation returned null. Aborting translation for this HTML.");
            return originalHtml;
        }

        String[] translatedTexts = translatedCombinedText.split("\\|\\|\\|---\\|\\|\\|");

        if (translatedTexts.length != textsToTranslate.size()) {
            logger.error("Translation result count mismatch! Expected: {}, Got: {}. Reverting to original.", textsToTranslate.size(), translatedTexts.length);
            return originalHtml;
        }

        AtomicInteger counter = new AtomicInteger(0);
        body.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    TextNode textNode = (TextNode) node;
                    String text = textNode.getWholeText().trim();
                    if (!text.isEmpty()) {
                        int index = counter.getAndIncrement();
                        if (index < translatedTexts.length) {
                            textNode.text(translatedTexts[index].trim());
                        }
                    }
                }
            }
            @Override
            public void tail(Node node, int depth) {}
        });

        return body.html();
    }
} 