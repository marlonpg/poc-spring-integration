package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.support.FileExistsMode;

import java.io.File;

@Configuration
public class FileIntegrationConfig {

    @Bean
    public IntegrationFlow fileProcessingFlow() {
        return IntegrationFlow
            .from(Files.inboundAdapter(new File("input"))
                .patternFilter("*.csv")
                .autoCreateDirectory(true),
                e -> e.poller(Pollers.fixedDelay(5000)))
            .transform(Files.toStringTransformer())
            .split(s -> s.delimiters("\n"))
            .filter("payload.length() > 0")
            .transform(String.class, line -> processLine(line))
            .aggregate()
            .transform(list -> String.join("\n", (java.util.List<String>) list))
            .handle(Files.outboundAdapter(new File("output"))
                .fileExistsMode(FileExistsMode.APPEND)
                .fileNameGenerator(m -> "processed_" + System.currentTimeMillis() + ".txt"))
            .get();
    }

    private String processLine(String line) {
        // Simple processing: convert to uppercase and add timestamp
        return "[" + System.currentTimeMillis() + "] " + line.toUpperCase();
    }
}