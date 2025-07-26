package com.swiftgoal.app.service;

import com.swiftgoal.app.dto.TranslationResultDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Service
@Slf4j
public class DqdTranslatorScraper {

    private static final String SEARCH_URL_TEMPLATE = "https://www.dongqiudi.com/search?q=%s";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    public TranslationResultDto translate(String englishName) {
        try {
            // 1. 构建URL
            String encodedName = URLEncoder.encode(englishName.toLowerCase(), StandardCharsets.UTF_8);
            String searchUrl = String.format(SEARCH_URL_TEMPLATE, encodedName);
            log.info("懂球帝搜索URL: {}", searchUrl);

            // 2. 请求搜索页
            Document searchResultPage = Jsoup.connect(searchUrl)
                    .userAgent(USER_AGENT)
                    .get();

            // 礼貌性延迟
            sleepRandomly();

            // 3. 找到球员详情页链接
            Elements playerLinks = searchResultPage.select("a.search-result-player");
            Element bestMatchLink = findBestMatch(playerLinks, englishName);

            if (bestMatchLink == null) {
                log.warn("在懂球帝上未找到球员: {}", englishName);
                return null;
            }

            String playerDetailUrl = bestMatchLink.absUrl("href");
            log.info("找到球员详情页: {}", playerDetailUrl);

            // 4. 访问详情页
            Document detailPage = Jsoup.connect(playerDetailUrl)
                    .userAgent(USER_AGENT)
                    .get();
            
            // 5. 提取信息
            String playerNameCn = detailPage.select("h1.player-name").text();
            String teamNameCn = detailPage.select("a.club-name").text();

            log.info("提取到中文名: {}, 球队: {}", playerNameCn, teamNameCn);

            TranslationResultDto result = new TranslationResultDto();
            result.setPlayerNameCn(playerNameCn);
            result.setTeamNameCn(teamNameCn);

            return result;

        } catch (IOException e) {
            log.error("爬取懂球帝时发生IO异常 for name '{}'", englishName, e);
            return null;
        } catch (Exception e) {
            log.error("爬取或解析懂球帝时发生未知错误 for name '{}'", englishName, e);
            return null;
        }
    }

    private Element findBestMatch(Elements links, String englishName) {
        if (links.isEmpty()) {
            return null;
        }
        for (Element link : links) {
            // 选择器直接定位到英文名所在的元素
            String foundEnglishName = link.select("p.en-name").text();
            if (englishName.equalsIgnoreCase(foundEnglishName)) {
                return link;
            }
        }
        // 如果没有完全匹配的，返回第一个作为备选
        return links.first();
    }

    private void sleepRandomly() {
        try {
            Thread.sleep(1000 + new Random().nextInt(2000)); // 随机延迟1-3秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("爬虫延迟被中断");
        }
    }
} 