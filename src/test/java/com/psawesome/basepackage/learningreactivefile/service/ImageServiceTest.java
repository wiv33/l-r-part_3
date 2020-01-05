package com.psawesome.basepackage.learningreactivefile.service;

import com.psawesome.basepackage.learningreactivefile.dto.Image;
import com.psawesome.basepackage.learningreactivefile.repo.ImageRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.http.codec.multipart.FilePart;
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
import java.util.List;

import static com.psawesome.basepackage.learningreactivefile.service.ImageService.UPLOAD_ROOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * package: com.psawesome.basepackage.learningreactivefile.service
 * author: PS
 * DATE: 2020-01-04 토요일 20:17
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

        String fileOne = "docker-logo.jpeg";
        String fileTwo = "l-r-Flux.jpg";
        String fileTree = "l-r-Mono.jpeg";

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
    @Order(1)
    public void findAllImages() {

        Flux<Image> listFlux = imageService.findAllImages()
            .log();
        Flux<String> stringFlux = listFlux.flatMap(image -> Mono.just(image.getName()));

        assertAll(
            () -> assertThat(listFlux.toIterable()).hasSize(3),
            () -> assertTrue(stringFlux.any(string -> string.equals("docker-logo.jpeg")).block(Duration.ofSeconds(10))),
            () -> assertTrue(stringFlux.any(string -> string.equals("l-r-Flux.jpg")).block(Duration.ofSeconds(10))),
            () -> assertTrue(stringFlux.any(string -> string.equals("l-r-Mono.jpeg")).block(Duration.ofSeconds(10)))
        );
    }

    @Test
    @Order(2)
    public void findOneImage() {
        Mono<Resource> imageResource = imageService.findOneImage("docker-logo.jpeg");
        Resource block = imageResource.log().block(Duration.ofSeconds(3));

        assertEquals("docker-logo.jpeg", block.getFilename());
    }

    @Test
    @Order(3)
    public void deleteImage() throws IOException {

        // Given
        Mono<Resource> oneImage = imageService.findOneImage("l-r-Mono.jpeg");

        Resource block = oneImage.block(Duration.ofSeconds(2));
        assertAll(
            () -> assertEquals("l-r-Mono.jpeg", block.getFilename()),
            () -> assertTrue(block.exists())
        );

        // When
        imageService.deleteImage("l-r-Mono.jpeg").block(Duration.ofSeconds(10));

        // Then
        Mono<Resource> resultImage = imageService.findOneImage("l-r-Mono.jpeg");
        Resource block1 = resultImage.block(Duration.ofSeconds(7));
        System.out.println("resultImage = " + block1.getFile().getPath());
        assertFalse(block1.exists());
    }

    @Test
    void createImages() {
        Image alphaImage = new Image("1", "alpha.jpg");
        Image bravoImage = new Image("2", "bravo.jpg");
        FilePart file1 = mock(FilePart.class);
        given(file1.filename()).willReturn(alphaImage.getName());
        given(file1.transferTo((File) any())).willReturn(Mono.empty());
        FilePart file2 = mock(FilePart.class);
        given(file2.filename()).willReturn(bravoImage.getName());
        given(file2.transferTo((File) any())).willReturn(Mono.empty());

        List<Image> images = imageService
            .createImage(Flux.just(file1, file2))
            .then(imageService.findAllImages().collectList())
            .block(Duration.ofSeconds(30));

        assertThat(images).hasSize(5);
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
