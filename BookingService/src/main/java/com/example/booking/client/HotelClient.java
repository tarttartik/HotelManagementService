package com.example.booking.client;

import com.example.booking.view.RoomView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class HotelClient {

    private final WebClient webClient;
    private final String hotelBaseUrl;
    private final int retries;
    private final Duration timeout;

    public HotelClient(
            @Value("${hotel.base-url}") String hotelBaseUrl,
            @Value("${hotel.timeout-ms}") int timeoutMs,
            @Value("${hotel.retries}") int retries,
            WebClient.Builder builder
    ){
        this.webClient = builder.baseUrl(hotelBaseUrl).build();
        this.hotelBaseUrl = hotelBaseUrl;
        this.retries = retries;
        this.timeout = Duration.ofMillis(timeoutMs);
    }

    public void sendHold(Long roomId, Map<String, String> payload, String correlationId) {
        sendPost( "/api/rooms/hold/" + roomId, payload, correlationId);
    }

    public void sendConfirm(Long roomId, Map<String, String> payload, String correlationId){
        sendPost( "/api/rooms/confirm/" + roomId, payload, correlationId);
    }

    public void sendRelease(Long roomId, Map<String, String> payload, String correlationId){
        sendPost( "/api/rooms/release/" + roomId, payload, correlationId);
    }

    private void sendPost(String path, Map<String, String> payload, String correlationId) {
        webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(timeout)
                .retryWhen(Retry.backoff(retries, Duration.ofMillis(300)).maxBackoff(Duration.ofSeconds(2)))
                .block();
    }

    public Long getTopAvailableRoomId() {
        return (Long) webClient.get()
                .uri(hotelBaseUrl + "/api/rooms/TopAvailable")
                .retrieve()
                .bodyToMono(RoomView.class)
                .map(RoomView::getId)
                .block();
    }

}
