package com.swiftgoal.app.service;

import com.swiftgoal.app.repository.LeagueRepository;
import com.swiftgoal.app.repository.PlayerContractRepository;
import com.swiftgoal.app.repository.PlayerRepository;
import com.swiftgoal.app.repository.TeamRepository;
import com.swiftgoal.app.repository.entity.League;
import com.swiftgoal.app.repository.entity.Player;
import com.swiftgoal.app.repository.entity.PlayerContract;
import com.swiftgoal.app.repository.entity.Team;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataInitializerService {

    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final PlayerContractRepository playerContractRepository;

    private static final String TRANSFERMARKT_BASE_URL = "https://www.transfermarkt.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36";

    @Transactional
    public void initializeData() {
        log.info("Starting database initialization for sports data...");

        if (leagueRepository.count() > 0) {
            log.info("Sports data already initialized. Skipping.");
            return;
        }

        Map<String, String> leaguesToScrape = Map.of(
                "GB1", "Premier League", "ES1", "La Liga", "L1", "Bundesliga", "IT1", "Serie A", "FR1", "Ligue 1"
        );
        Map<String, String> leagueCountries = Map.of(
                "GB1", "England", "ES1", "Spain", "L1", "Germany", "IT1", "Italy", "FR1", "France"
        );

        log.info("Scraping and saving {} leagues...", leaguesToScrape.size());
        leaguesToScrape.forEach((code, name) -> {
            String urlFriendlyName = name.toLowerCase().replace(" ", "-");
            String leagueUrlPath = "/" + urlFriendlyName + "/startseite/wettbewerb/" + code;
            try {
                League league = new League();
                league.setName(name);
                league.setCountry(leagueCountries.get(code));
                League savedLeague = leagueRepository.save(league);
                log.info("Saved league: {}", savedLeague.getName());

                scrapeTeamsForLeague(savedLeague, leagueUrlPath);
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                log.error("Failed to process league: {} (URL path: {})", name, leagueUrlPath, e);
            }
        });
        log.info("Database initialization for sports data completed.");
    }

    private void scrapeTeamsForLeague(League league, String leagueUrlPath) throws IOException, InterruptedException {
        String fullUrl = TRANSFERMARKT_BASE_URL + leagueUrlPath;
        log.info("Scraping teams for league: {} from URL: {}", league.getName(), fullUrl);

        Document doc = Jsoup.connect(fullUrl)
                .userAgent(USER_AGENT)
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Referer", TRANSFERMARKT_BASE_URL + "/")
                .get();

        for (Element row : doc.select("#yw1 .items > tbody > tr")) {
            Element teamLink = row.selectFirst("td.hauptlink a");
            if (teamLink == null) continue;

            String teamName = teamLink.text();
            String teamUrlPath = teamLink.attr("href");
            String logoUrl = row.selectFirst("td.zentriert a img") != null ? row.selectFirst("td.zentriert a img").attr("src") : null;

            Team team = new Team();
            team.setName(teamName);
            team.setCountry(league.getCountry());
            team.setLogoUrl(logoUrl);
            team.setLeague(league);
            Team savedTeam = teamRepository.save(team);
            log.info("Saved team: {} for league: {}", savedTeam.getName(), league.getName());

            scrapePlayersForTeam(savedTeam, teamUrlPath);
            TimeUnit.SECONDS.sleep(2);
        }
    }

    private void scrapePlayersForTeam(Team team, String teamUrlPath) throws IOException, InterruptedException {
        // Sanitize URL to remove any season-specific parts, making it more robust.
        int seasonIndex = teamUrlPath.indexOf("/saison_id/");
        if (seasonIndex != -1) {
            teamUrlPath = teamUrlPath.substring(0, seasonIndex);
        }

        String fullUrl = TRANSFERMARKT_BASE_URL + teamUrlPath;
        log.info("--> Scraping players for team: {} from URL: {}", team.getName(), fullUrl);
        Document doc = Jsoup.connect(fullUrl)
                .userAgent(USER_AGENT)
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Referer", fullUrl) // Use the team page as the referer for this request
                .get();

        for (Element row : doc.select("#yw1 .items > tbody > tr")) {
            try {
                String playerName = row.selectFirst("td.posrela .hauptlink a") != null ? row.selectFirst("td.posrela .hauptlink a").text() : null;
                if (playerName == null) continue;

                String photoUrl = row.selectFirst("td.zentriert img") != null ? row.selectFirst("td.zentriert img").attr("data-src") : null;
                String position = row.selectFirst("td.posrela .inline-table tr:last-child td") != null ? row.selectFirst("td.posrela .inline-table tr:last-child td").text() : null;
                String jerseyNumberStr = row.selectFirst("td.zentriert .tm-shirt-number") != null ? row.selectFirst("td.zentriert .tm-shirt-number").text() : null;
                String dobString = row.select("td.zentriert").get(1).text();
                String nationality = row.select("td.zentriert").get(2).selectFirst("img") != null ? row.select("td.zentriert").get(2).selectFirst("img").attr("title") : null;
                String marketValueString = row.selectFirst("td.rechts.hauptlink") != null ? row.selectFirst("td.rechts.hauptlink").text() : null;

                Player player = new Player();
                player.setFullName(playerName);
                player.setNationality(nationality);
                player.setPosition(position);
                player.setPhotoUrl(photoUrl);
                player.setDateOfBirth(parseDateOfBirth(dobString));
                Player savedPlayer = playerRepository.save(player);

                PlayerContract contract = new PlayerContract();
                contract.setPlayer(savedPlayer);
                contract.setTeam(team);
                if (jerseyNumberStr != null && jerseyNumberStr.matches("\\d+")) {
                    contract.setJerseyNumber(Integer.parseInt(jerseyNumberStr));
                }
                contract.setMarketValueEur(parseMarketValue(marketValueString));
                playerContractRepository.save(contract);

                log.info("    + Saved player: {} with contract for team: {}", savedPlayer.getFullName(), team.getName());
                TimeUnit.MILLISECONDS.sleep(200);

            } catch (Exception e) {
                log.error("Failed to parse or save player for team {}. Row HTML: {}", team.getName(), row.html(), e);
            }
        }
    }

    private LocalDate parseDateOfBirth(String dobString) {
        if (dobString == null || dobString.isEmpty()) return null;
        try {
            String datePart = dobString.split("\\(")[0].trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
            return LocalDate.parse(datePart, formatter);
        } catch (DateTimeParseException e) {
            log.warn("Could not parse date: {}", dobString);
            return null;
        }
    }

    private Long parseMarketValue(String valueString) {
        if (valueString == null || valueString.equals("-")) return null;
        try {
            Pattern pattern = Pattern.compile("€([\\d.]+)m");
            Matcher matcher = pattern.matcher(valueString);
            if (matcher.find()) {
                double value = Double.parseDouble(matcher.group(1));
                return (long) (value * 1_000_000);
            }
            pattern = Pattern.compile("€([\\d.]+)k");
            matcher = pattern.matcher(valueString);
            if (matcher.find()) {
                double value = Double.parseDouble(matcher.group(1));
                return (long) (value * 1_000);
            }
        } catch (NumberFormatException e) {
            log.warn("Could not parse market value: {}", valueString);
        }
        return null;
    }
}