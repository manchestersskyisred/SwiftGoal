package com.sportslens.ai.service;

import com.sportslens.ai.domain.User;
import com.sportslens.ai.dto.UserRegistrationDto;
import com.sportslens.ai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("用户 " + username + " 不存在"));
    }

    @Transactional
    public void registerNewUser(UserRegistrationDto userDto) {
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new IllegalStateException("该用户名已被注册");
        }
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new IllegalStateException("该邮箱已被注册");
        }
        
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setAvatarUrl("/images/default-avatar.png"); // 设置默认头像
        
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