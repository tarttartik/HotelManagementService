package com.example.booking.dto;

import java.time.LocalDate;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        Long roomId,
        LocalDate start,
        LocalDate end,
        String status
) {}