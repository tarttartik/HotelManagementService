package com.example.booking.controller;

import com.example.booking.client.HotelClient;
import com.example.booking.entity.Booking;
import com.example.booking.repository.BookingRepository;
import com.example.booking.service.BookingService;
import com.example.booking.view.RoomView;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearer-jwt")
public class BookingController {

    private final BookingService bookingService;
    private final HotelClient client;
    private final BookingRepository bookingRepository;

    public BookingController(BookingService bookingService, HotelClient client, BookingRepository bookingRepository) {
        this.client = client;
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
    }

    @PostMapping
    public Booking createBooking(@AuthenticationPrincipal Jwt jwt, @RequestBody Map<String, String> req) {
        Long userId = Long.parseLong(jwt.getSubject());
        Boolean autoSelect = Boolean.valueOf(req.get("autoSelect"));
        Long roomId = Long.valueOf(req.get("roomId"));
        LocalDate start = LocalDate.parse(req.get("startDate"));
        LocalDate end = LocalDate.parse(req.get("endDate"));
        String requestId = req.get("requestId");
        return bookingService.createBooking(userId, requestId, roomId, start, end, autoSelect);
    }

    @GetMapping
    public List<Booking> getUserBookings(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        return bookingRepository.findByUserId(userId);
    }
}