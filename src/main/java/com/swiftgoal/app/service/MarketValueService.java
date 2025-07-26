package com.swiftgoal.app.service;

import com.swiftgoal.app.repository.entity.football.Player;
import com.swiftgoal.app.repository.entity.football.PlayerContract;
import com.swiftgoal.app.repository.football.PlayerContractRepository;
import com.swiftgoal.app.repository.football.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketValueService {

    private final PlayerRepository playerRepository;
    private final PlayerContractRepository playerContractRepository;
    private final TransfermarktScraper transfermarktScraper;

    @Transactional
    public void scrapeAndSaveMarketValue(Player player) {
        Long marketValue = transfermarktScraper.scrapeMarketValue(player);
        if (marketValue != null) {
            List<PlayerContract> contracts = playerContractRepository.findByPlayerIdOrderByIdDesc(player.getId());
            if (!contracts.isEmpty()) {
                PlayerContract latestContract = contracts.get(0);
                latestContract.setMarketValueEur(marketValue);
                playerContractRepository.save(latestContract);
                log.info("Successfully updated market value for player {} ({}) to {}", player.getFullNameEn(), player.getId(), marketValue);
            } else {
                log.warn("No contract found for player {} ({}), cannot save market value.", player.getFullNameEn(), player.getId());
            }
        }
    }

    public void scrapeMarketValueForAllPlayers() {
        log.info("Starting to scrape market values for all players without it.");
        List<PlayerContract> contractsWithoutValue = playerContractRepository.findByMarketValueEurIsNull();
        log.info("Found {} players needing market value update.", contractsWithoutValue.size());

        for (PlayerContract contract : contractsWithoutValue) {
            if (contract.getPlayer() != null) {
                playerRepository.findById(contract.getPlayer().getId()).ifPresent(this::scrapeAndSaveMarketValue);
            }
        }

        log.info("Finished scraping market values for all players.");
    }
} 