package com.example.booking.service;

import com.example.booking.client.HotelClient;
import com.example.booking.entity.Booking;
import com.example.booking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;


@Service
public class BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private final HotelClient client;
    private final BookingRepository bookingRepository;

    public BookingService( BookingRepository bookingRepository, HotelClient client) {
        this.bookingRepository = bookingRepository;
        this.client = client;
    }

    @Transactional
    public Booking createBooking(Long userId, String requestId, Long roomId, LocalDate start, LocalDate end, Boolean autoSelect) {
        var existingBooking = bookingRepository.findByRequestId(requestId);
        if(existingBooking.isPresent()) return  existingBooking.get();

        if(autoSelect) {
            try{
                roomId = client.getTopAvailableRoomId();
            }
            catch (Exception ex){
                log.error("Unable to autoSelect a room");
                return null;
            }
        }

        var booking = new Booking.Builder()
                .requestId(requestId)
                .userId(userId)
                .roomId(roomId)
                .startDate(start)
                .endDate(end)
                .status(Booking.Status.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();

        bookingRepository.save(booking);

        String correlationId = UUID.randomUUID().toString();

        try{
            client.sendHold(roomId,
                    Map.of(
                    "requestId", requestId,
                    "startDate", start.toString(),
                    "endDate", end.toString()
            ), correlationId);

            client.sendConfirm(roomId,  Map.of("requestId", requestId), correlationId);
            booking.confirm();
            bookingRepository.save(booking);
            log.info("[{}] Booking CONFIRMED", correlationId);
        }
        catch(Exception e) {
            log.warn("[{}] Booking flow failed: {}", correlationId, e.toString());

            try {
                client.sendRelease(roomId,  Map.of("requestId", requestId), correlationId);
            }
            catch (Exception ex ) {}

            booking.cancel();
            bookingRepository.save(booking);
            log.info("[{}] Booking CANCELLED", correlationId);
        }

        return booking;
    }

}