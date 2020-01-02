package com.psawesome.basepackage.learningreactivefile.sample;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

import java.time.Duration;
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
                .flatMap(group -> Flux.zip(Mono.just(group.key()), group.count()))
                .map(keyAndCount -> keyAndCount.toString())
                .subscribe(System.out::println);
    }

    @Test
    public void generateAndDuration() {
        Flux<String> generate = Flux.generate(() -> 1L, (id, sink) -> {
            sink.next("value " + id);
            return id + 1;
        });
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(1)).take(10);

        /*return */Flux.zip(generate, interval).subscribe();
    }
}
