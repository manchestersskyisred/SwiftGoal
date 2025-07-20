package com.swiftgoal.app;

import com.swiftgoal.app.service.DataInitializerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DataImportRunner {

    public static void main(String[] args) {
        // We run this as a Spring Boot application to get the fully configured context
        ConfigurableApplicationContext context = SpringApplication.run(DataImportRunner.class, args);
        
        // Get the DataInitializerService bean from the context
        DataInitializerService dataInitializerService = context.getBean(DataInitializerService.class);
        
        System.out.println("Starting the data import process...");
        
        // Call the data initialization method directly
        try {
            dataInitializerService.initializeData();
            System.out.println("Data import process completed successfully.");
        } catch (Exception e) {
            System.err.println("An error occurred during data import: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the context to shut down the application
            context.close();
            System.exit(0);
        }
    }
} 