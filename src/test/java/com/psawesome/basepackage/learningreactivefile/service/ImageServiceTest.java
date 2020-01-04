package com.psawesome.basepackage.learningreactivefile.service;

import com.psawesome.basepackage.learningreactivefile.dto.Image;
import com.psawesome.basepackage.learningreactivefile.repo.ImageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import static com.psawesome.basepackage.learningreactivefile.service.ImageService.UPLOAD_ROOT;
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
    public void setUp() throws IOException {
        System.out.println("ImageServiceTest.setUp");
        operations.dropCollection(Image.class);

        String fileOne = "docker-logo.png";
        String fileTwo = "l-r-Flux.png";
        String fileTree = "l-r-Mono.png";

        operations.insert(new Image("1", fileOne));
        operations.insert(new Image("2", fileTwo));
        operations.insert(new Image("3", fileTree));

        FileSystemUtils.deleteRecursively(new File(UPLOAD_ROOT));

        Files.createDirectory(Paths.get(UPLOAD_ROOT));
        FileCopyUtils.copy("Test file", new FileWriter(UPLOAD_ROOT + "/" + fileOne));
        FileCopyUtils.copy("Test file2", new FileWriter(UPLOAD_ROOT + "/" + fileTwo));
        FileCopyUtils.copy("Test file3", new FileWriter(UPLOAD_ROOT + "/" + fileTree));
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
        Flux<String> stringFlux = listFlux.flatMap(image -> Mono.just(image.getName()));

        assertAll(
            () -> assertTrue(stringFlux.any(string -> string.equals("docker-logo.png")).block(Duration.ofSeconds(3))),
            () -> assertTrue(stringFlux.any(string -> string.equals("l-r-Flux.png")).block(Duration.ofSeconds(3))),
            () -> assertTrue(stringFlux.any(string -> string.equals("l-r-Mono.png")).block(Duration.ofSeconds(3)))
        );
    }

    @Test
    public void findOneImage() {
        Mono<Resource> imageResource = imageService.findOneImage("docker-logo.png");
        Resource block = imageResource.log().block(Duration.ofSeconds(3));

        assertEquals("docker-logo.png", block.getFilename());
    }

    @Test
    public void deleteImage() throws IOException {

        // Given
        Mono<Resource> oneImage = imageService.findOneImage("l-r-Mono.png");

        Resource block = oneImage.block(Duration.ofSeconds(2));
        assertAll(
            () -> assertEquals("l-r-Mono.png", block.getFilename()),
            () -> assertTrue(block.exists())
        );

        // When
        imageService.deleteImage("l-r-Mono.png");

        // Then
        Mono<Resource> resultImage = imageService.findOneImage("l-r-Mono.png");
        Resource block1 = resultImage.block(Duration.ofSeconds(7));
        System.out.println("resultImage = " + block1.getFile().getPath());
        assertFalse(block1.exists());
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