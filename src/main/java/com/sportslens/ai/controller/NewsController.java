package com.sportslens.ai.controller;

import com.sportslens.ai.domain.BrowsingHistory;
import com.sportslens.ai.domain.NewsArticle;
import com.sportslens.ai.domain.User;
import com.sportslens.ai.repository.BrowsingHistoryRepository;
import com.sportslens.ai.repository.NewsArticleRepository;
import com.sportslens.ai.repository.UserRepository;
import com.sportslens.ai.service.NewsArticleService;
import com.sportslens.ai.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
public class NewsController {

    private static final List<String> ALL_CATEGORIES = Arrays.asList(
            "NBA", "英超", "西甲", "德甲", "法甲", "意甲", "沙特联", "女足", "MLB", "网球", "综合体育"
    );

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    @Autowired
    private NewsArticleService newsArticleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrowsingHistoryRepository browsingHistoryRepository;

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Page<NewsArticle> articlesPage = newsArticleService.findPaginated(0, 100);
        model.addAttribute("articlesPage", articlesPage);
        model.addAttribute("categories", ALL_CATEGORIES);
        model.addAttribute("currentCategory", "Home"); // Add this for active link handling

        if (userDetails != null) {
            userRepository.findByUsername(userDetails.getUsername()).ifPresent(user -> {
                List<NewsArticle> recommendedArticles = recommendationService.getRecommendationsForUser(user, 5);
                model.addAttribute("recommendedArticles", recommendedArticles);
                model.addAttribute("userAvatarUrl", user.getAvatarUrl());
            });
        }

        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam("query") String query, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<NewsArticle> articles = newsArticleService.searchArticles(query);
        model.addAttribute("articles", articles);
        model.addAttribute("searchQuery", query);
        model.addAttribute("categories", ALL_CATEGORIES);
        model.addAttribute("currentCategory", "Search"); // Add this for active link handling
        if (userDetails != null) {
            userRepository.findByUsername(userDetails.getUsername()).ifPresent(user -> {
                model.addAttribute("userAvatarUrl", user.getAvatarUrl());
            });
        }
        return "search";
    }

    @GetMapping("/recommendations")
    public String getRecommendations(Model model, @AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return "redirect:/"; // Should not happen
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsArticle> articlesPage = recommendationService.getRecommendationsForUserPaginated(userOpt.get(), pageable);

        model.addAttribute("articlesPage", articlesPage);
        model.addAttribute("categories", ALL_CATEGORIES);
        model.addAttribute("currentCategory", "为你推荐");
        model.addAttribute("userAvatarUrl", userOpt.get().getAvatarUrl());
        return "category";
    }

    @GetMapping("/category/{category}")
    public String getArticlesByCategory(@PathVariable String category, Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size, @AuthenticationPrincipal UserDetails userDetails) {
        Page<NewsArticle> articlesPage = newsArticleService.findByCategoryPaginated(category, page, size);
        model.addAttribute("articlesPage", articlesPage);
        model.addAttribute("categories", ALL_CATEGORIES);
        model.addAttribute("currentCategory", category);
        if (userDetails != null) {
            userRepository.findByUsername(userDetails.getUsername()).ifPresent(user -> {
                model.addAttribute("userAvatarUrl", user.getAvatarUrl());
            });
        }
        return "category";
    }

    @GetMapping("/article/{id}")
    public String viewArticle(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<NewsArticle> optionalArticle = newsArticleRepository.findById(id);

        if (optionalArticle.isEmpty()) {
            return "redirect:/"; // Redirect to home if article not found
        }

        NewsArticle article = optionalArticle.get();
        model.addAttribute("article", article);
        model.addAttribute("categories", ALL_CATEGORIES); // For header consistency

        if (userDetails != null) {
            userRepository.findByUsername(userDetails.getUsername()).ifPresent(user -> {
                BrowsingHistory history = new BrowsingHistory(user, article);
                browsingHistoryRepository.save(history);
                model.addAttribute("userAvatarUrl", user.getAvatarUrl());
            });
        }

        return "article_detail";
    }
}