package com.ues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpringBootApplication
public class UesProjectApplication {

    private static final Logger logger = LogManager.getLogger(UesProjectApplication.class);

    public static void main(String[] args) {
        logger.info("Starting UES Project Application");
        SpringApplication.run(UesProjectApplication.class, args);
        logger.info("UES Project Application started successfully");
    }
}
