package com.swiftgoal.app.command;

import com.swiftgoal.app.service.MarketValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("market-value-import")
@Slf4j
@RequiredArgsConstructor
public class MarketValueImportCommand implements CommandLineRunner {

    private final MarketValueService marketValueService;
    private final ConfigurableApplicationContext context;

    @Override
    public void run(String... args) {
        log.info("Starting Market Value Import process...");

        try {
            marketValueService.scrapeMarketValueForAllPlayers();
            log.info("Market Value Import process finished successfully.");
        } catch (Exception e) {
            log.error("An error occurred during the market value import process.", e);
        } finally {
            log.info("Closing application.");
            SpringApplication.exit(context);
        }
    }
} 