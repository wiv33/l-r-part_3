package com.psawesome.basepackage.learningreactivefile.service;

import com.psawesome.basepackage.learningreactivefile.dto.Image;
import com.psawesome.basepackage.learningreactivefile.repo.ImageRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * package: com.psawesome.basepackage.learningreactivefile.service
 * author: PS
 * DATE: 2020-01-04 토요일 20:17
 */
@ExtendWith(SpringExtension.class)
@DataMongoTest
class ImageServiceTest {

    final ImageRepository imageRepository;
    final ImageService imageService;
    final MongoOperations operations;

    @Autowired
    ImageServiceTest(ImageRepository imageRepository, ImageService imageService, MongoOperations operations) {
        this.imageRepository = imageRepository;
        this.imageService = imageService;
        this.operations = operations;
    }

    @BeforeEach
    public void setUp() {
        System.out.println("ImageServiceTest.setUp");
        operations.dropCollection(Image.class);

        operations.insert(new Image("1", "docker-logo.png"));
        operations.insert(new Image("2", "l-r-Flux.png"));
        operations.insert(new Image("3", "l-r-Mono.png"));
    }

    @AfterEach
    public void drop() {
        System.out.println("ImageServiceTest.drop");
        operations.dropCollection(Image.class);
    }

    @Test
    public void findAllImages() {

        Flux<Image> listFlux = imageService.findAllImages()
            .log();
        Flux<String> stringFlux = listFlux.flatMap(image -> Mono.just(image.getId() + "," + image.getName()));

        assertAll(
            () -> assertTrue(stringFlux.any(string -> string.equals("1,docker-logo.png")).block()),
            () -> assertTrue(stringFlux.any(string -> string.equals("2,l-r-Flux.png")).block()),
            () -> assertTrue(stringFlux.any(string -> string.equals("3,l-r-Mono.png")).block())
        );
    }

    @Test
    public void findOneImage() {
        Mono<Resource> imageResource = imageService.findOneImage("docker-logo.png");
        Resource block = imageResource.log().block();

        assertEquals("docker-logo.png", block.getFilename());
    }

    @Test
    public void deleteImage() throws IOException {

        // Given
        Mono<Resource> oneImage = imageService.findOneImage("l-r-Mono.png");
        assertEquals("l-r-Mono.png", oneImage.block().getFilename());

        // When
        imageService.deleteImage("l-r-Mono.png").block();

        // Then
        Mono<Resource> resultImage = imageService.findOneImage("l-r-Mono.png");
        System.out.println("resultImage = " + resultImage.block().getFile().getPath());
        assertFalse(resultImage.block().exists());
    }


    @Configuration
    @EnableReactiveMongoRepositories(basePackageClasses = ImageRepository.class)
    static class TestConfig {

        @Bean
        public ImageService imageService(ResourceLoader resourceLoader, ImageRepository imageRepository) {
            return new ImageService(resourceLoader, imageRepository);
        }
    }
}