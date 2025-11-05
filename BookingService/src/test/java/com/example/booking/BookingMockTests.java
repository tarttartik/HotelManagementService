package com.example.booking;

import com.example.booking.client.HotelClient;
import com.example.booking.entity.Booking;
import com.example.booking.repository.BookingRepository;
import com.example.booking.service.BookingService;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class BookingMockTests {

    static WireMockServer wireMockServer = new WireMockServer(0);

    @BeforeAll
    static void startWireMock() {
        wireMockServer.start();
        System.out.println("WireMock started on port: " + wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer.isRunning()) {
            wireMockServer.stop();
            System.out.println("WireMock stopped.");
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("hotel.base-url", () -> "http://localhost:" + wireMockServer.port());
        registry.add("hotel.timeout-ms", () -> "1000");
        registry.add("hotel.retries", () -> "1");
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    // --- JWT helper ---
    private String tokenUser() {
        byte[] key = "dev-secret-please-change".getBytes(StandardCharsets.UTF_8);
        if (key.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(key, 0, padded, 0, key.length);
            key = padded;
        }

        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject("100")
                .addClaims(Map.of("scope", "USER", "username", "it-user"))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(key))
                .compact();
    }

    // --- Tests ---
    @Test
    void createBooking_Http_Success() {
        wireMockServer.stubFor(post(urlPathMatching("/api/rooms/hold/\\d+"))
                .willReturn(okJson("{}")));
        wireMockServer.stubFor(post(urlPathMatching("/api/rooms/confirm/\\d+"))
                .willReturn(okJson("{}")));

        webTestClient.post().uri("/bookings")
                .header("Authorization", "Bearer " + tokenUser())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{"
                        + "\"roomId\":1,"
                        + "\"startDate\":\"2025-10-20\","
                        + "\"endDate\":\"2025-10-22\","
                        + "\"requestId\":\"" + UUID.randomUUID() + "\"}")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().jsonPath("$.status").isEqualTo("CONFIRMED");
    }

    @Test
    void successFlow_confirmed() {
        wireMockServer.stubFor(post(urlPathMatching("/api/rooms/hold/\\d+"))
                .willReturn(okJson("{}")));
        wireMockServer.stubFor(post(urlPathMatching("/api/rooms/confirm/\\d+"))
                .willReturn(okJson("{}")));

        Booking booking = bookingService.createBooking(
                1L, "r1", 10L, LocalDate.now(), LocalDate.now().plusDays(1), false);

        assertEquals(Booking.Status.CONFIRMED, booking.getStatus());
    }

    @MockBean
    private HotelClient hotelClient;

    @Test
    void successFlowAutoSelectTrue_confirmed() {
        when(hotelClient.getTopAvailableRoomId()).thenReturn(42L);

        wireMockServer.stubFor(post(urlPathMatching("/api/rooms/hold/\\d+"))
                .willReturn(okJson("{}")));
        wireMockServer.stubFor(post(urlPathMatching("/api/rooms/confirm/\\d+"))
                .willReturn(okJson("{}")));

        Booking booking = bookingService.createBooking(
                1L, "r1", null, LocalDate.now(), LocalDate.now().plusDays(1), true);

        assertEquals(Booking.Status.CONFIRMED, booking.getStatus());

        assertEquals(42L, booking.getRoomId());
    }

    @Test
    void failureFlow_cancelledWithCompensation() {
        wireMockServer.stubFor(post(urlPathMatching("/api/rooms/hold\\d+"))
                .willReturn(serverError()));
        wireMockServer.stubFor(post(urlPathMatching("/api/rooms/release\\d+"))
                .willReturn(okJson("{}")));

        Booking booking = bookingService.createBooking(
                2L, "r2", 11L, LocalDate.now(), LocalDate.now().plusDays(1), false);

        assertEquals(Booking.Status.CANCELLED, booking.getStatus());
    }

    @Test
    void timeoutFlow_cancelled() {
        wireMockServer.stubFor(post(urlPathMatching("/api/rooms/hold/\\d+"))
                .willReturn(aResponse().withFixedDelay(2000).withStatus(200)));
        wireMockServer.stubFor(post(urlPathMatching("/api/rooms/release/\\d+"))
                .willReturn(okJson("{}")));

        Booking booking = bookingService.createBooking(
                3L, "r3", 12L, LocalDate.now(), LocalDate.now().plusDays(1), false);

        assertEquals(Booking.Status.CANCELLED, booking.getStatus());
    }

    @Test
    void idempotency_noDuplicate() {
        wireMockServer.stubFor(post(urlPathMatching("/api/rooms/hold/\\d+"))
                .willReturn(okJson("{}")));
        wireMockServer.stubFor(post(urlPathMatching("/api/rooms/confirm/\\d+"))
                .willReturn(okJson("{}")));

        Booking b1 = bookingService.createBooking(
                4L, "r4", 13L, LocalDate.now(), LocalDate.now().plusDays(1), false);
        Booking b2 = bookingService.createBooking(
                4L, "r4", 13L, LocalDate.now(), LocalDate.now().plusDays(1), false);

        assertEquals(b1.getId(), b2.getId());
    }
}