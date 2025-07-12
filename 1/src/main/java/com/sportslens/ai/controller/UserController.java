package com.sportslens.ai.controller;

import com.sportslens.ai.domain.BrowsingHistory;
import com.sportslens.ai.domain.User;
import com.sportslens.ai.dto.UserRegistrationDto;
import com.sportslens.ai.repository.BrowsingHistoryRepository;
import com.sportslens.ai.repository.UserRepository;
import com.sportslens.ai.service.StorageService;
import com.sportslens.ai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final BrowsingHistoryRepository browsingHistoryRepository;
    private final StorageService storageService;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository,
                          BrowsingHistoryRepository browsingHistoryRepository, StorageService storageService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.browsingHistoryRepository = browsingHistoryRepository;
        this.storageService = storageService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserRegistrationDto userDto, BindingResult bindingResult, Model model) {
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            model.addAttribute("errorMessage", "两次输入的密码不匹配");
            return "register";
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.registerNewUser(userDto);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
        return "redirect:/login?registration_success";
    }

    @GetMapping("/profile")
    public String profile(Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        return "redirect:/profile/" + principal.getName();
    }

    @GetMapping("/profile/{username}")
    public String userProfile(@PathVariable("username") String username, Model model, Principal principal) {
        User user = userService.findByUsername(username);
        model.addAttribute("user", user);

        if (principal != null) {
            User currentUser = userService.findByUsername(principal.getName());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isFollowing", currentUser.getFollowing().contains(user));
        }

        List<BrowsingHistory> history = browsingHistoryRepository.findByUserOrderByViewedAtDesc(user);
        model.addAttribute("history", history);

        return "profile";
    }

    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile file,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        String avatarUrl = storageService.store(file);
        userService.updateAvatar(principal.getName(), avatarUrl);
        redirectAttributes.addFlashAttribute("message", "头像上传成功!");
        return "redirect:/profile";
    }

    @PostMapping("/follow/{username}")
    public String followUser(@PathVariable("username") String username, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        userService.follow(principal.getName(), username);
        return "redirect:/profile/" + username;
    }

    @PostMapping("/unfollow/{username}")
    public String unfollowUser(@PathVariable("username") String username, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        userService.unfollow(principal.getName(), username);
        return "redirect:/profile/" + username;
    }
}