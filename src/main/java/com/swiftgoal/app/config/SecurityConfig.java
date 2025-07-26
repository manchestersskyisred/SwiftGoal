package com.swiftgoal.app.config;

import com.swiftgoal.app.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter;

import java.util.UUID;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .map(user -> User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities("ROLE_" + user.getRole()) // Use authorities for clarity and convention
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers
                        .addHeaderWriter(new ContentSecurityPolicyHeaderWriter(
                                "img-src 'self' data: https://tmssl.akamaized.net;"
                        ))
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/game/api/**")) // 只对游戏API关闭CSRF
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/",
                                "/news/**",
                                "/register",
                                "/user/**", // Allow access to all user-related pages
                                "/profile/**", // Allow access to profile actions like avatar upload
                                "/login",
                                "/perform_login", // Allow access to login processing URL
                                "/style.css",
                                "/script.js",
                                "/favicon.ico",
                                "/user-avatars/**",
                                "/game/**", // 允许访问游戏页面和API
                                "/game/play", // 游戏页面
                                "/game/api/**", // 游戏API
                                "/api/admin/**", // 数据导入API
                                "/test/**", // 测试API
                                "/css/**", // CSS文件
                                "/js/**", // JavaScript文件
                                "/images/**" // 图片文件
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login") // Specify custom login processing URL
                        .permitAll()
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .rememberMe(rememberMe -> rememberMe
                        .key(UUID.randomUUID().toString())
                        .tokenValiditySeconds(86400 * 14) // 14 days
                );
        return http.build();
    }
} 