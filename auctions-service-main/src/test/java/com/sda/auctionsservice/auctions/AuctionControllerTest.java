package com.sda.auctionsservice.auctions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuctionControllerTest {

    @Test
    void shouldFindAuctionByQuery(@Autowired WebTestClient testClient) {
        testClient
                .get()
                .uri("/auctions/search?query=opla%20sprzedam")
                .headers(headersConsumer -> headersConsumer.setBasicAuth("user", "password"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldAddAuctionToDb(@Autowired WebTestClient testClient) {
        testClient
                .post()
                .uri("/auctions")
                .bodyValue(new Auction("test auction", BigDecimal.ONE, BigDecimal.ONE, "test description", LocalDateTime.now(), new Category("Moto")))
                .headers(headersConsumer -> headersConsumer.setBasicAuth("user", "password"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldReturn400ForNotExistingCategory(@Autowired WebTestClient testClient) {
        testClient
                .post()
                .uri("/auctions")
                .bodyValue(new Auction("test auction", BigDecimal.ONE, BigDecimal.ONE, "test description", LocalDateTime.now(), new Category("Non Existing Category")))
                .headers(headersConsumer -> headersConsumer.setBasicAuth("user", "password"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().json("""
                        {"type":"about:blank","title":"Bad Request","status":400,"detail":"Category Non Existing Category not exist","instance":"/auctions"}""");
    }

    @Test
    void shouldCheckIfValidationIsWorking(@Autowired WebTestClient testClient) {
        testClient
                .post()
                .uri("/auctions")
                .bodyValue(new Auction("test auction", BigDecimal.valueOf(-1.0), BigDecimal.ONE, "test description", LocalDateTime.now(),new Category("Moto")))
                .headers(headersConsumer -> headersConsumer.setBasicAuth("user", "password"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ProblemDetail.class);
//                .json("""
//                        {"type":"about:blank","title":"Bad Request","status":400,"detail":"initialPrice must be greater than or equal to 0.01","instance":"/auctions"}
//                        """);
    }

    @Test
    void shouldDeleteAuction(@Autowired WebTestClient testClient) {
        testClient
                .delete()
                .uri("/auctions/1")
                .headers(headersConsumer -> headersConsumer.setBasicAuth("user", "password"))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldUpdateAuction(@Autowired WebTestClient testClient) {
        Auction updatedAuction = testClient
                .put()
                .uri("/auctions/1")
                .bodyValue(new Auction("Updated", BigDecimal.TEN, BigDecimal.ONE, "test description", LocalDateTime.now(),new Category("Moto")))
                .headers(headersConsumer -> headersConsumer.setBasicAuth("user", "password"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Auction.class).returnResult().getResponseBody();

        assertEquals("Updated", updatedAuction.getName());
    }

    @Test
    void shouldReturn400whenUpdateAuctionWithWrongId(@Autowired WebTestClient testClient) {
        testClient
                .put()
                .uri("/auctions/999")
                .bodyValue(new Auction("Updated", BigDecimal.TEN, BigDecimal.ONE, "test description", LocalDateTime.now(),new Category("Moto")))
                .headers(headersConsumer -> headersConsumer.setBasicAuth("user", "password"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnListOfAuctionsForExistingCategory(@Autowired WebTestClient testClient) {
        List responseBody = testClient
                .get()
                .uri("/auctions/searchByCategory?category=moto")
                .headers(headersConsumer -> headersConsumer.setBasicAuth("user", "password"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class).returnResult().getResponseBody();

        assertEquals(2, responseBody.size());
    }

    @Test
    void shouldReturnEmptyListForCategoryWithoutAuctions(@Autowired WebTestClient testClient) {
        List responseBody = testClient
                .get()
                .uri("/auctions/searchByCategory?category=tools")
                .headers(headersConsumer -> headersConsumer.setBasicAuth("user", "password"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class).returnResult().getResponseBody();

        assertEquals(0, responseBody.size());
    }

    @Test
    void shouldReturn400ForNonExistingCategory(@Autowired WebTestClient testClient) {
        testClient
                .get()
                .uri("/auctions/searchByCategory?category=wrongCategory")
                .headers(headersConsumer -> headersConsumer.setBasicAuth("user", "password"))
                .exchange()
                .expectStatus().isBadRequest();
    }

}