package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;

@SpringBootApplication
@EnableIntegration
public class FileProcessingApp {
    public static void main(String[] args) {
        SpringApplication.run(FileProcessingApp.class, args);
    }
}