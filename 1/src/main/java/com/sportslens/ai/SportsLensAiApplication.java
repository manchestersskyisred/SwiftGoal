package com.sportslens.ai;

import com.sportslens.ai.service.StorageService;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SportsLensAiApplication {

    @Resource
    StorageService storageService;

    public static void main(String[] args) {
        System.setProperty("java.net.useSystemProxies", "false");
        System.setProperty("http.proxyHost", "");
        System.setProperty("http.proxyPort", "80");
        System.setProperty("https.proxyHost", "");
        System.setProperty("https.proxyPort", "443");
        SpringApplication.run(SportsLensAiApplication.class, args);
    }

    @Bean
    CommandLineRunner init() {
        return (args) -> {
            storageService.init();
        };
    }
}