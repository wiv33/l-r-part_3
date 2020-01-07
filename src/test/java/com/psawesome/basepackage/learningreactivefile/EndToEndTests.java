package com.psawesome.basepackage.learningreactivefile;



import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.openqa.selenium.chrome.ChromeDriverService.*;

/**
 * package: com.psawesome.basepackage.learningreactivefile
 * author: PS
 * DATE: 2020-01-07 화요일 23:31
 */
//@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EndToEndTests {

    static ChromeDriverService service;

    static ChromeDriver driver;

    @LocalServerPort
    int port;

    @BeforeTestClass
    public static void setUp () throws IOException {
        System.setProperty("webdriver.chrome.driver", "ext/chromedriver");
        service = createDefaultService();
        driver = new ChromeDriver();

        Path testResults = Paths.get("build", "test-results");

        if (!Files.exists(testResults)) {
            Files.createDirectory(testResults);
        }
    }

    @AfterTestClass
    public static void tearDown() {
        service.stop();
    }

}
