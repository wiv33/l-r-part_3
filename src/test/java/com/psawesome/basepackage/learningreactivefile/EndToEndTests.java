package com.psawesome.basepackage.learningreactivefile;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.interactions.Actions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.chrome.ChromeDriverService.createDefaultService;

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


    @BeforeAll
    static void beforeAll() throws IOException {
        System.out.println("EndToEndTests.setUp");
        System.setProperty("webdriver.chrome.driver", "ext/chromedriver2");
        service = createDefaultService();
        driver = new ChromeDriver(service);

        Path testResults = Paths.get("build", "test-results");

        if (!Files.exists(testResults)) {
            Files.createDirectory(testResults);
        }
    }

    @AfterAll
    static void afterAll() {
        service.stop();
    }


    @Test
    void homePageShouldWork() throws IOException {
        driver.get("http://localhost:" + port);
        //스크린 샷
        takeScreenshot("homePageShouldWork-1");

        //페이지 제목 확인
        assertThat(driver.getTitle()).isEqualTo("Learning Reactive");

        String pageContent = driver.getPageSource();

        assertThat(pageContent).contains("<a href=\"/images/bazinga.png/raw\">");

        WebElement element = driver.findElement(By.cssSelector("a[href*=\"bazinga.png\"]"));

        Actions actions = new Actions(driver);
        actions.moveToElement(element).click().perform();

        takeScreenshot("homePageShouldWork-2");

        driver.navigate().back();
    }

    private void takeScreenshot(String name) throws IOException {
        FileCopyUtils.copy(
            driver.getScreenshotAs(OutputType.FILE),
            new File("/build/test-results/TEST-" + name)
        );
    }
}
