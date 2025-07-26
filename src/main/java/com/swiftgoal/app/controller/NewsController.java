package com.swiftgoal.app.controller;

import com.swiftgoal.app.dto.SearchResultDto;
import com.swiftgoal.app.repository.entity.BrowsingHistory;
import com.swiftgoal.app.repository.entity.NewsArticle;
import com.swiftgoal.app.repository.entity.User;
import com.swiftgoal.app.repository.BrowsingHistoryRepository;
import com.swiftgoal.app.repository.NewsArticleRepository;
import com.swiftgoal.app.repository.UserRepository;
import com.swiftgoal.app.service.NewsArticleService;
import com.swiftgoal.app.service.RecommendationService;
import com.swiftgoal.app.service.SearchService;
import com.swiftgoal.app.service.BrowsingHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import com.swiftgoal.app.dto.NewsArticleDto;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class NewsController {

    private static final List<String> ALL_CATEGORIES = Arrays.asList(
            "NBA", "英超", "西甲", "德甲", "法甲", "意甲", "沙特联", "女足", "MLB", "网球", "综合体育"
    );

    private final NewsArticleService newsArticleService;
    private final SearchService searchService;
    private final UserRepository userRepository;
    private final RecommendationService recommendationService;
    private final BrowsingHistoryService browsingHistoryService;

    public NewsController(NewsArticleService newsArticleService, SearchService searchService, UserRepository userRepository, RecommendationService recommendationService, BrowsingHistoryService browsingHistoryService) {
        this.newsArticleService = newsArticleService;
        this.searchService = searchService;
        this.userRepository = userRepository;
        this.recommendationService = recommendationService;
        this.browsingHistoryService = browsingHistoryService;
    }

    @GetMapping("/test-images")
    public String testImages() {
        return "test_images";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Model model) {
        // Prepare main content: the news articles
        List<NewsArticleDto> mixedNews = newsArticleService.findHomepageNews();
        Page<NewsArticleDto> articlesPage = new PageImpl<>(mixedNews);
        model.addAttribute("articlesPage", articlesPage);

        // Prepare sidebar data
        model.addAttribute("categories", ALL_CATEGORIES);
        model.addAttribute("currentCategory", "Home"); // To highlight the correct link in the sidebar

        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam("query") String query, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // 1. Search for articles
        List<NewsArticle> articles = newsArticleService.searchArticles(query);
        model.addAttribute("articles", articles);

        // 2. Search for players and teams
        List<SearchResultDto> searchResults = searchService.searchPlayersAndTeams(query);
        model.addAttribute("searchResults", searchResults);

        // 3. Add other necessary attributes for the template
        model.addAttribute("searchQuery", query);
        model.addAttribute("categories", ALL_CATEGORIES);
        model.addAttribute("currentCategory", "Search");

        if (userDetails != null) {
            userRepository.findByUsername(userDetails.getUsername()).ifPresent(user -> {
                model.addAttribute("userAvatarUrl", user.getAvatarUrl());
            });
        }

        return "search"; // Return the correct template name
    }

    @GetMapping("/recommendations")
    public String getRecommendations(Model model, @AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        if (userDetails != null) {
            userRepository.findByUsername(userDetails.getUsername()).ifPresent(user -> {
                Pageable pageable = PageRequest.of(page, size);
                Page<NewsArticle> recommendations = recommendationService.getRecommendationsForUserPaginated(user, pageable);
                model.addAttribute("recommendations", recommendations);
                model.addAttribute("user", user);
            });
        }
        return "recommendations";
    }

    @GetMapping("/news/category/{category}")
    public String newsByCategory(@PathVariable String category, @RequestParam(defaultValue = "0") int page, Model model) {
        int size = 10;
        Page<NewsArticle> newsPage = newsArticleService.findByCategoryPaginated(category, page, size);
        model.addAttribute("newsPage", newsPage);
        model.addAttribute("category", category);
        return "news_category";
    }

    @GetMapping("/article/{id}")
    @Transactional(readOnly = true)
    public String articleDetail(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<NewsArticle> articleOpt = newsArticleService.findById(id);

        if (articleOpt.isEmpty()) {
            return "redirect:/";
        }
        NewsArticle article = articleOpt.get();
        model.addAttribute("article", article);

        if (userDetails != null) {
            userRepository.findByUsername(userDetails.getUsername()).ifPresent(user -> {
                // Only record history for real articles that are in the database
                if (article.getId() < 99997L) {
                    browsingHistoryService.recordHistory(user, article);
                }
                model.addAttribute("userAvatarUrl", user.getAvatarUrl());
            });
        }
        model.addAttribute("categories", ALL_CATEGORIES);
        model.addAttribute("currentCategory", article.getCategoryAi() != null ? article.getCategoryAi() : "News");
        return "article_detail";
    }
}