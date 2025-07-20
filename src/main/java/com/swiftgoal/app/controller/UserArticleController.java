package com.swiftgoal.app.controller;

import com.swiftgoal.app.repository.entity.NewsArticle;
import com.swiftgoal.app.repository.entity.User;
import com.swiftgoal.app.dto.UserArticleDto;
import com.swiftgoal.app.repository.UserRepository;
import com.swiftgoal.app.service.NewsArticleService;
import com.swiftgoal.app.service.UserArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

@Controller
@RequestMapping("/user/articles")
public class UserArticleController {

    private static final List<String> ALL_CATEGORIES = Arrays.asList(
            "NBA", "英超", "西甲", "德甲", "法甲", "意甲", "沙特联", "女足", "MLB", "网球", "综合体育"
    );

    @Autowired
    private UserArticleService userArticleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NewsArticleService newsArticleService;

    @GetMapping
    public String listMyArticles(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<User> optionalUser = userRepository.findByUsername(userDetails.getUsername());
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = optionalUser.get();
        List<NewsArticle> myArticles = userArticleService.getArticlesByUser(user);
        
        model.addAttribute("articles", myArticles);
        model.addAttribute("user", user);
        
        return "user/my-articles";
    }

    @GetMapping("/create")
    public String createArticleForm(Model model) {
        model.addAttribute("article", new UserArticleDto());
        model.addAttribute("allCategories", ALL_CATEGORIES);
        return "user/create-article";
    }

    @PostMapping("/create")
    public String createArticle(@Valid @ModelAttribute("article") UserArticleDto articleDto,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes, Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("allCategories", ALL_CATEGORIES);
            return "user/create-article";
        }

        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<User> optionalUser = userRepository.findByUsername(userDetails.getUsername());
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = optionalUser.get();
        
        try {
            NewsArticle savedArticle = userArticleService.createUserArticle(articleDto, user);
            redirectAttributes.addFlashAttribute("successMessage", "文章创建成功！");
            return "redirect:/user/articles";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "文章创建失败：" + e.getMessage());
            model.addAttribute("allCategories", ALL_CATEGORIES);
            return "user/create-article";
        }
    }

    @GetMapping("/{id}")
    public String viewArticle(@PathVariable Long id, 
                             Model model, 
                             @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<User> optionalUser = userRepository.findByUsername(userDetails.getUsername());
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = optionalUser.get();
        Optional<NewsArticle> optionalArticle = userArticleService.getArticleByIdAndUser(id, user);
        
        if (optionalArticle.isEmpty()) {
            return "redirect:/user/articles?error=article_not_found";
        }

        model.addAttribute("article", optionalArticle.get());
        return "user/view-article";
    }

    @GetMapping("/{id}/edit")
    public String editArticleForm(@PathVariable Long id, 
                                 Model model, 
                                 @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<User> optionalUser = userRepository.findByUsername(userDetails.getUsername());
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = optionalUser.get();
        Optional<NewsArticle> optionalArticle = userArticleService.getArticleByIdAndUser(id, user);
        
        if (optionalArticle.isEmpty()) {
            return "redirect:/user/articles?error=article_not_found";
        }

        UserArticleDto articleDto = userArticleService.mapEntityToDto(optionalArticle.get());

        model.addAttribute("article", articleDto);
        model.addAttribute("articleId", id);
        model.addAttribute("allCategories", ALL_CATEGORIES);
        
        return "user/edit-article";
    }

    @PostMapping("/{id}/edit")
    public String updateArticle(@PathVariable Long id,
                               @Valid @ModelAttribute("article") UserArticleDto articleDto,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes, Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("articleId", id);
            model.addAttribute("allCategories", ALL_CATEGORIES);
            return "user/edit-article";
        }

        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<User> optionalUser = userRepository.findByUsername(userDetails.getUsername());
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = optionalUser.get();
        
        try {
            userArticleService.updateUserArticle(id, articleDto, user);
            redirectAttributes.addFlashAttribute("successMessage", "文章更新成功！");
            return "redirect:/user/articles";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "文章更新失败：" + e.getMessage());
            model.addAttribute("articleId", id);
            model.addAttribute("allCategories", ALL_CATEGORIES);
            return "user/edit-article";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteArticle(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        
        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<User> optionalUser = userRepository.findByUsername(userDetails.getUsername());
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = optionalUser.get();
        
        try {
            userArticleService.deleteUserArticle(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "文章删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "文章删除失败：" + e.getMessage());
        }
        
        return "redirect:/user/articles";
    }
} 