package com.sportslens.ai.controller;

import com.sportslens.ai.domain.NewsArticle;
import com.sportslens.ai.domain.User;
import com.sportslens.ai.dto.UserArticleDto;
import com.sportslens.ai.repository.UserRepository;
import com.sportslens.ai.service.UserArticleService;
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

@Controller
@RequestMapping("/user/articles")
public class UserArticleController {

    @Autowired
    private UserArticleService userArticleService;

    @Autowired
    private UserRepository userRepository;

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
        return "user/create-article";
    }

    @PostMapping("/create")
    public String createArticle(@Valid @ModelAttribute("article") UserArticleDto articleDto,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
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
            return "redirect:/user/articles/" + savedArticle.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "文章创建失败：" + e.getMessage());
            return "redirect:/user/articles/create";
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

        NewsArticle article = optionalArticle.get();
        UserArticleDto articleDto = new UserArticleDto();
        articleDto.setTitle(article.getTitle());
        articleDto.setContent(article.getRawContent());
        articleDto.setSource(article.getSource());

        model.addAttribute("article", articleDto);
        model.addAttribute("articleId", id);
        
        return "user/edit-article";
    }

    @PostMapping("/{id}/edit")
    public String updateArticle(@PathVariable Long id,
                               @Valid @ModelAttribute("article") UserArticleDto articleDto,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
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
            return "redirect:/user/articles/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "文章更新失败：" + e.getMessage());
            return "redirect:/user/articles/" + id + "/edit";
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