package com.psawesome.basepackage.learningreactivefile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.reactive.HiddenHttpMethodFilter;

@SpringBootApplication
public class LearningReactiveFileApplication {

    public static void main(String[] args) {
        /*
        log-level: debug 시 오류 발생
        logging:
            level:
                io:
                    netty: debug

        noUnsafe true로 Accessible 은 해결되나 Unsafe는 해결되지 않음.
        java.lang.UnsupportedOperationException: Reflective setAccessible(true) disabled
        java.lang.UnsupportedOperationException: sun.misc.Unsafe unavailable
        */
//        System.setProperty("io.netty.noUnsafe", "true");
        SpringApplication.run(LearningReactiveFileApplication.class, args);
    }

    @Bean
    HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
}
