package com.swiftgoal.app;

import com.swiftgoal.app.service.StorageService;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
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