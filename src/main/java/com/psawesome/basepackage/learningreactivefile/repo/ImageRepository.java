package com.psawesome.basepackage.learningreactivefile.repo;

import com.psawesome.basepackage.learningreactivefile.dto.Image;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * package: com.psawesome.basepackage.learningreactivefile.repo
 * author: PS
 * DATE: 2020-01-04 토요일 20:31
 */
public interface ImageRepository extends ReactiveCrudRepository<Image, String> {

    Mono<Image> findByName(String name);
}
