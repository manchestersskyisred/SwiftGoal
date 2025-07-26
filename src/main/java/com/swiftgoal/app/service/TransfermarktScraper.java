package com.swiftgoal.app.service;

import com.swiftgoal.app.repository.entity.football.Player;
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
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TransfermarktScraper {

    private static final String SEARCH_URL_TEMPLATE = "https://www.transfermarkt.com/schnellsuche/ergebnis/schnellsuche?query=%s";
    private static final String BASE_URL = "https://www.transfermarkt.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36";

    public Long scrapeMarketValue(Player player) {
        String playerName = player.getFullNameEn();
        log.info("Attempting to scrape market value for player: {}", playerName);
        try {
            String playerProfileUrl = findPlayerProfileUrl(playerName);
            if (playerProfileUrl == null) {
                log.warn("Could not find player profile URL for: {}", playerName);
                return null;
            }

            sleepRandomly();

            Document playerPage = Jsoup.connect(playerProfileUrl).userAgent(USER_AGENT).get();
            String marketValueStr = extractMarketValueFromPage(playerPage);
            if (marketValueStr == null) {
                log.warn("Could not extract market value for: {}", playerName);
                return null;
            }

            Long marketValue = parseMarketValue(marketValueStr);
            log.info("Successfully scraped market value for {}: €{}", playerName, marketValue);
            return marketValue;

        } catch (IOException e) {
            log.error("An I/O error occurred while scraping for player: {}", playerName, e);
            return null;
        } catch (InterruptedException e) {
            log.error("Thread was interrupted during scraping for player: {}", playerName, e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private String findPlayerProfileUrl(String playerName) throws IOException, InterruptedException {
        String encodedPlayerName = URLEncoder.encode(playerName, StandardCharsets.UTF_8);
        String searchUrl = String.format(SEARCH_URL_TEMPLATE, encodedPlayerName);

        Document searchPage = Jsoup.connect(searchUrl).userAgent(USER_AGENT).get();
        Elements playerLinks = searchPage.select("div.box:has(h2:contains(Players)) table.items > tbody > tr.odd > td:first-of-type > table > tbody > tr:first-of-type > td:nth-of-type(2) > a.spielprofil_tooltip");

        if (playerLinks.isEmpty()) {
            log.warn("No player links found on search page for '{}'", playerName);
            return null;
        }

        // Assume the first result is the most relevant one
        Element playerLink = playerLinks.first();
        return BASE_URL + playerLink.attr("href");
    }

    private String extractMarketValueFromPage(Document playerPage) {
        Element marketValueElement = playerPage.selectFirst("a.data-header__market-value-wrapper");
        if (marketValueElement == null) {
            return null;
        }
        return marketValueElement.text();
    }

    private Long parseMarketValue(String marketValueStr) {
        if (marketValueStr == null || marketValueStr.isEmpty()) {
            return null;
        }
        try {
            String valuePart = marketValueStr.replace("€", "").trim();
            double value;
            if (valuePart.endsWith("m")) {
                value = Double.parseDouble(valuePart.replace("m", "")) * 1_000_000;
            } else if (valuePart.endsWith("k")) {
                value = Double.parseDouble(valuePart.replace("k", "")) * 1_000;
            } else {
                value = Double.parseDouble(valuePart);
            }
            return (long) value;
        } catch (NumberFormatException e) {
            log.error("Failed to parse market value string: '{}'", marketValueStr, e);
            return null;
        }
    }

    private void sleepRandomly() throws InterruptedException {
        // Sleep for 1 to 3 seconds to be polite
        long sleepTime = TimeUnit.SECONDS.toMillis(1 + new Random().nextInt(2));
        Thread.sleep(sleepTime);
    }
} 