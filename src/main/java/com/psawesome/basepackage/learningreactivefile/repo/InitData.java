package com.psawesome.basepackage.learningreactivefile.repo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

/**
 * package: com.psawesome.basepackage.learningreactivefile.repo
 * author: PS
 * DATE: 2020-01-03 금요일 22:38
 */
@Component
public class InitData {

    @Bean
    CommandLineRunner init(MongoOperations operations) {
        return args -> {

        };
    }
}
