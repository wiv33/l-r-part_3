package com.psawesome.basepackage.learningreactivefile.repo;

import com.psawesome.basepackage.learningreactivefile.dto.Image;
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
            operations.dropCollection(Image.class);

            operations.insert(new Image("1", "docker-logo.png"));
            operations.insert(new Image("2", "l-r-Flux"));
            operations.insert(new Image("3", "l-r-Mono"));
        };
    }
}
