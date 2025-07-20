package com.swiftgoal.app.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HtmlTranslator {

    private static final Logger logger = LoggerFactory.getLogger(HtmlTranslator.class);
    private static final String SEPARATOR = "|||";

    @Autowired
    private AIService aiService;

    public String translateHtmlContent(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        Document doc = Jsoup.parseBodyFragment(html);
        List<String> originalTexts = new ArrayList<>();

        doc.body().traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    TextNode textNode = (TextNode) node;
                    String text = textNode.getWholeText().trim();
                    if (!text.isEmpty()) {
                        originalTexts.add(text);
                    }
                }
            }

            @Override
            public void tail(Node node, int depth) {}
        });

        if (originalTexts.isEmpty()) {
            return html;
        }

        String combinedText = String.join(SEPARATOR, originalTexts);
        String translatedCombinedText = aiService.translateBatch(combinedText, SEPARATOR);

        if (translatedCombinedText == null || translatedCombinedText.isEmpty()) {
            logger.error("Batch translation returned null or empty string.");
            return html; // Return original HTML on translation failure
        }
        
        List<String> translatedTexts = Arrays.asList(translatedCombinedText.split("\\|\\|\\|"));

        if (originalTexts.size() != translatedTexts.size()) {
            logger.error("Mismatch between original text count ({}) and translated text count ({}).", originalTexts.size(), translatedTexts.size());
            return html; // Return original HTML on mismatch
        }

        final int[] index = {0};
        doc.body().traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    TextNode textNode = (TextNode) node;
                    String text = textNode.getWholeText().trim();
                    if (!text.isEmpty()) {
                        if (index[0] < translatedTexts.size()) {
                            textNode.text(translatedTexts.get(index[0]));
                            index[0]++;
                        }
                    }
                }
            }
            @Override
            public void tail(Node node, int depth) {}
        });

        return doc.body().html();
    }
} 