package com.psawesome.basepackage.learningreactivefile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class LearningReactiveFileApplication {

    public static void main(String[] args) {
        System.out.println("args.length = " + args.length);
        try (ConfigurableApplicationContext run = SpringApplication.run(LearningReactiveFileApplication.class, args)){

        }
    }

}
