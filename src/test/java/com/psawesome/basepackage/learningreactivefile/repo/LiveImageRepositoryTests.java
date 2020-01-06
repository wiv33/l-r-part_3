package com.psawesome.basepackage.learningreactivefile.repo;

import com.psawesome.basepackage.learningreactivefile.dto.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * package: com.psawesome.basepackage.learningreactivefile.repo
 * author: PS
 * DATE: 2020-01-06 월요일 23:07
 */
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
public class LiveImageRepositoryTests {
    @Autowired
    ImageRepository repository;

    @Autowired
    MongoOperations operations;

    @BeforeEach
    public void setUp() {
        operations.dropCollection(Image.class);

        operations.insert(new Image("1", "learning-spring-boot-cover.jpg"));
        operations.insert(new Image("2", "learning-spring-boot-2nd-edition-cover.jpg"));
        operations.insert(new Image("3", "bazinga.png"));

        operations.findAll(Image.class).forEach(System.out::println);
    }

    @Test
    void findAllShouldWork() {
        Flux<Image> images = repository.findAll();
        StepVerifier.create(images)
            .recordWith(ArrayList::new)
            .expectNextCount(3)
            .consumeRecordedWith(results -> {
                assertAll(
                    () -> assertThat(results).hasSize(3),
                    () -> assertThat(results).extracting(Image::getName)
                        .contains("learning-spring-boot-cover.jpg",
                            "learning-spring-boot-2nd-edition-cover.jpg",
                            "bazinga.png")
                );
            })
            .expectComplete()
            .verify();
    }

    @Test
    void findByNameShouldWork() {
        Mono<Image> image = repository.findByName("bazinga.png");

        StepVerifier.withVirtualTime(() -> image, 1)
            .expectNextMatches(results -> {
                assertAll(
                    () -> assertEquals("bazinga.png", results.getName()),
                    () -> assertEquals("3" ,results.getId())
                );
                return true;
            })
            .expectComplete()
            .verify();
    }
}
