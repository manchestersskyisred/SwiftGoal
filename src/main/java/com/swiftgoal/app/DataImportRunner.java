package com.swiftgoal.app;

import com.swiftgoal.app.repository.football.PlayerRepository;
import com.swiftgoal.app.repository.entity.football.Player;
import com.swiftgoal.app.service.DataImportService;
import com.swiftgoal.app.service.PlayerDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Profile("data-import")
@Component
@Slf4j
@RequiredArgsConstructor
public class DataImportRunner implements CommandLineRunner {

    private final DataImportService dataImportService;
    private final PlayerDataService playerDataService;
    private final PlayerRepository playerRepository;
    private final ConfigurableApplicationContext context;

    @Override
    public void run(String... args) {
        log.info("Starting data import process...");

        try {
            importLeaguesAndTeams();
            importPlayerStats();
            log.info("Data import process finished successfully.");
        } catch (Exception e) {
            log.error("A critical error occurred during data import.", e);
        } finally {
            log.info("Closing application.");
            SpringApplication.exit(context, () -> 0);
        }
    }

    private void importLeaguesAndTeams() {
        log.info("Starting leagues and teams import...");
        try {
            dataImportService.importLeaguesTeamsAndPlayers();
            log.info("Leagues and teams import completed successfully.");
        } catch (Exception e) {
            log.error("Failed to import leagues and teams", e);
            // Decide if we should continue if this part fails. For now, we'll stop.
            throw new RuntimeException("Stopping due to failure in leagues and teams import.", e);
        }
    }

    private void importPlayerStats() {
        log.info("Starting player stats import...");
        List<Player> players = playerRepository.findAll();
        int totalPlayers = players.size();
        log.info("Found {} players to import stats for.", totalPlayers);

        // Use a fixed-size thread pool to control concurrency
        ExecutorService executor = Executors.newFixedThreadPool(10); // Adjust pool size as needed

        for (int i = 0; i < totalPlayers; i++) {
            Player player = players.get(i);
            int playerIndex = i + 1;
            executor.submit(() -> {
                try {
                    log.info("Importing stats for player {}/{} (ID: {})", playerIndex, totalPlayers, player.getId());
                    playerDataService.fetchAndSavePlayerMatchStats(player.getId(), 2025);
                } catch (Exception e) {
                    log.error("Failed to import stats for player ID: {}", player.getId(), e);
                }
            });
        }

        // Shut down the executor and wait for all tasks to complete
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.MINUTES)) {
                log.warn("Player stats import timed out after 60 minutes. Some players may not have been processed.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Player stats import was interrupted.", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("Player stats import completed.");
    }
} 