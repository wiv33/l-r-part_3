package com.psawesome.basepackage.learningreactivefile.sample;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

/**
 * package: com.psawesome.basepackage.learningreactivefile.sample
 * author: PS
 * DATE: 2020-01-01 수요일 23:43
 */
public class SampleTest {

    @Test
    public void sample() {
        Flux.just("alpah", "bravo", "charlie")
                .map(String::toUpperCase)
                .flatMap(s -> Flux.fromArray(s.split("")))
        .groupBy(String::toString)
        .sort(Comparator.comparing(GroupedFlux::key))
        .flatMap(group -> Mono.just(group.key()).and(group.count()))
                .map(keyAndCount -> keyAndCount.toString())
                .subscribe(System.out::println);
    }
}
