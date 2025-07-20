package com.swiftgoal.app.controller;

import com.swiftgoal.app.repository.BrowsingHistoryRepository;
import com.swiftgoal.app.repository.entity.BrowsingHistory;
import com.swiftgoal.app.repository.entity.User;
import com.swiftgoal.app.dto.UserRegistrationDto;
import com.swiftgoal.app.service.UserService;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserRegistrationDto registrationDto,
                                      BindingResult result,
                                      Model model) {
        User existingUsername = userService.findByUsername(registrationDto.getUsername());
        if (existingUsername != null) {
            result.rejectValue("username", null, "该用户名已被注册");
        }

        User existingEmail = userService.findByEmail(registrationDto.getEmail());
        if (existingEmail != null) {
            result.rejectValue("email", null, "该邮箱已被注册");
        }

        if (result.hasErrors()) {
            return "register";
        }

        userService.registerNewUser(registrationDto);
        return "redirect:/login?registration_success";
    }
    
    @GetMapping("/{username}")
    public String userProfile(@PathVariable String username, Model model) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return "error/404";
        }

        // Sort the browsing history directly from the user entity
        List<BrowsingHistory> history = user.getBrowsingHistory().stream()
                .sorted(Comparator.comparing(BrowsingHistory::getViewedAt).reversed())
                .collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("history", history);
        return "profile";
    }
}