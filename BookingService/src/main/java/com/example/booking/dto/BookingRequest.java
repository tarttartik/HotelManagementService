package com.example.booking.dto;

import java.time.LocalDate;
import java.util.UUID;

public class BookingRequest {
    public static class CreateBooking {
        public Long roomId; // optional if autoSelect
        public boolean autoSelect;
        public LocalDate startDate;
        public LocalDate endDate;
        public UUID requestId; // optional client-provided
    }
}