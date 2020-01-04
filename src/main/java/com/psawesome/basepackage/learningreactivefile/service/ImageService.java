package com.psawesome.basepackage.learningreactivefile.service;

import com.psawesome.basepackage.learningreactivefile.dto.Image;
import com.psawesome.basepackage.learningreactivefile.repo.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * package: com.psawesome.basepackage.learningreactivefile.service
 * author: PS
 * DATE: 2020-01-02 목요일 23:47
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ImageService {
    static String UPLOAD_ROOT = "upload-dir";

    private final ResourceLoader resourceLoader;
    private final ImageRepository imageRepository;

    public Flux<Image> findAllImages() {
        return imageRepository.findAll();
    }

    public Mono<Resource> findOneImage(String filename) {
        return Mono.fromSupplier(() -> resourceLoader.getResource("file:" + UPLOAD_ROOT + "/" + filename));
    }

    public Mono<Void> createImage(Flux<FilePart> files) {
        return files.flatMap(file -> {
            Mono<Image> save = imageRepository.save(new Image(UUID.randomUUID().toString(), file.filename()));

            Mono<Void> copyFile = Mono.just(
                Paths.get(UPLOAD_ROOT, file.filename()).toFile())
                .log("createImage-pickTarget")
                .map(destFile -> {
                    try {
                        destFile.createNewFile();
                        return destFile;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).log("createImage-newFile")
                .flatMap(file::transferTo)
                .log("createImage-copy");

            return Mono.when(save, copyFile);
        }).then();

    }

    public Mono<Void> deleteImage(String filename) {
        Mono<Void> deleteDBImage = imageRepository
            .findByName(filename)
            .flatMap(imageRepository::delete)
            .log("delete Image");

        Mono<Void> deleteFile = Mono.fromRunnable(() -> {
            try {
                Files.deleteIfExists(Paths.get(UPLOAD_ROOT, filename));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return Mono.when(deleteDBImage, deleteFile).then();
    }

    @Bean
    CommandLineRunner setUp() {
        return args -> {
            FileSystemUtils.deleteRecursively(new File(UPLOAD_ROOT));

            Files.createDirectory(Paths.get(UPLOAD_ROOT));

            FileCopyUtils.copy("Test file", new FileWriter(UPLOAD_ROOT + "/lsbc.jpg"));

            FileCopyUtils.copy("Test file2", new FileWriter(UPLOAD_ROOT + "/lsb2ec.jpg"));
            FileCopyUtils.copy("Test file3", new FileWriter(UPLOAD_ROOT + "/background.jpg"));
        };
    }
}
