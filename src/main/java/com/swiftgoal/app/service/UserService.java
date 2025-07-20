package com.swiftgoal.app.service;

import com.swiftgoal.app.repository.entity.User;
import com.swiftgoal.app.dto.UserRegistrationDto;
import com.swiftgoal.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public void registerNewUser(UserRegistrationDto userDto) {
        // Validation is now handled in the controller
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setAvatarUrl("/images/default-avatar.png"); // 设置默认头像
        user.setRole("USER"); // 确保新用户有角色
        user.setEnabled(true); // 确保新用户账户是激活状态
        
        userRepository.save(user);
    }

    @Transactional
    public void follow(String currentUsername, String usernameToFollow) {
        User currentUser = findByUsername(currentUsername);
        User userToFollow = findByUsername(usernameToFollow);

        currentUser.getFollowing().add(userToFollow);
        userToFollow.getFollowers().add(currentUser);

        userRepository.save(currentUser);
        userRepository.save(userToFollow);
    }

    @Transactional
    public void unfollow(String currentUsername, String usernameToUnfollow) {
        User currentUser = findByUsername(currentUsername);
        User userToUnfollow = findByUsername(usernameToUnfollow);

        currentUser.getFollowing().remove(userToUnfollow);
        userToUnfollow.getFollowers().remove(currentUser);

        userRepository.save(currentUser);
        userRepository.save(userToUnfollow);
    }

    @Transactional
    public void updateAvatar(String username, String avatarUrl) {
        User user = findByUsername(username);
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }
}