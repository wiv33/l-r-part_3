package com.psawesome.basepackage.learningreactivefile.controller;

import com.psawesome.basepackage.learningreactivefile.dto.Image;
import com.psawesome.basepackage.learningreactivefile.service.ImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * package: com.psawesome.basepackage.learningreactivefile.controller
 * author: PS
 * DATE: 2020-01-06 월요일 23:11
 */
//@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = HomeController.class)
//@Import(value = {ThymeleafAutoConfiguration.class})
class HomeControllerTest {

    @Autowired
    WebTestClient client;

    @MockBean
    ImageService service;

    @Test
    void name() {
        assertNotNull(client);
    }

    @Test
    @DisplayName("전체 이미지 리스트 출력")
    void baseRouteShouldListAllImages() {
        // Given
        Image alphaImage = new Image("1", "alpha.png");
        Image bravoImage = new Image("2", "bravo.png");
        given(service.findAllImages())
            .willReturn(Flux.just(alphaImage, bravoImage));

        // When
        EntityExchangeResult<String> result = client.get().uri("/")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .returnResult();

        // Then
        verify(service).findAllImages();
        verifyNoMoreInteractions(service); // 추가 요구가 없다는 증명
        assertThat(result.getResponseBody())
            .contains(
                "<title>Learning Reactive</title>")
            .contains("<a href=\"/images/alpha.png/raw\">")
            .contains("<a href=\"/images/bravo.png/raw\">");
    }

    @DisplayName("성공적으로 파일을 가져옴")
    @Test
    void fetchingImageShouldWork() {
        // Given
        given(service.findOneImage(any()))
            .willReturn(Mono.just(new ByteArrayResource("data".getBytes())));

        client.get().uri("/images/alpha.png/raw")
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class).isEqualTo("data");

        verify(service).findOneImage("alpha.png");

        verifyNoMoreInteractions(service);
    }

    @Test
    void fetchingNullImageShouldFail() throws IOException {
        Resource resource = mock(Resource.class);

        given(resource.getInputStream())
            .willThrow(new IOException("Bad file"));

        given(service.findOneImage(any()))
            .willReturn(Mono.just(resource));

        client.get().uri("/images/alpha.png/raw")
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody(String.class)
        .isEqualTo("Couldn't find alpha.png => Bad file");

        verify(service).findOneImage("alpha.png");
        verifyNoMoreInteractions(service);
    }


}