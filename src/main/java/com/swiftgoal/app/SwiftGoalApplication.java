package com.swiftgoal.app;

import com.swiftgoal.app.service.StorageService;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EntityScan("com.swiftgoal.app.repository.entity")
@EnableJpaRepositories("com.swiftgoal.app.repository")
@EnableAsync
public class SwiftGoalApplication {

    @Resource
    StorageService storageService;

    public static void main(String[] args) {
        SpringApplication.run(SwiftGoalApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    CommandLineRunner init() {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }
}