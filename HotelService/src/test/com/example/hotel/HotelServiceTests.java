package com.example.hotel;

import com.example.hotel.entity.Hotel;
import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomLock;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.service.HotelService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
public class HotelServiceTests {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private HotelService hotelService;

    @Test
    @Transactional
    public void holdConfirmRelease_idempotentFlow() {
        Hotel h = new Hotel.Builder()
                .name("TestHotel")
                .address("Moscow, Tverskaya, 50")
                .build();

        h = hotelRepository.save(h);

        Room r = new Room.Builder()
                .number("110")
                .timesBooked(0)
                .available(true)
                .hotel(h)
                .build();

        r = hotelService.saveRoom(r);

        String req = "req-1";
        LocalDate s = LocalDate.now();
        LocalDate e = s.plusDays(2);

        RoomLock l1 = hotelService.holdRoomLock(req, r.getId(), s, e);
        RoomLock l2 = hotelService.holdRoomLock(req, r.getId(), s, e);
        Assertions.assertEquals(l1.getId(), l2.getId());

        hotelService.confirmRoomLock(req);
        hotelService.confirmRoomLock(req);

        RoomLock afterConfirm = hotelService.confirmRoomLock(req);
        Assertions.assertEquals(RoomLock.Status.CONFIRMED, afterConfirm.getStatus());

        RoomLock afterRelease = hotelService.releaseRoomLock(req);
        Assertions.assertEquals(RoomLock.Status.CONFIRMED, afterRelease.getStatus());
    }

    @Test
    @Transactional
   public void dateConflictThrowsException() {

        Hotel h = new Hotel.Builder()
                .name("TestHotel")
                .address("Moscow, Tverskaya, 50")
                .build();

        h = hotelRepository.save(h);

        Room r = new Room.Builder()
                .number("110")
                .timesBooked(0)
                .available(true)
                .hotel(h)
                .build();

        r = hotelService.saveRoom(r);

        LocalDate s1 = LocalDate.now();
        LocalDate e1 = s1.plusDays(2);
        hotelService.holdRoomLock("req-a", r.getId(), s1, e1);

        Room finalR = r;
        Assertions.assertThrows(IllegalStateException.class, () ->
                hotelService.holdRoomLock("req-b", finalR.getId(), s1.plusDays(1), e1.plusDays(1))
        );
    }

    @Test
    @Transactional
    public void availableFlagDoesNotAffectDateOccupancy() {

        Hotel h = new Hotel.Builder()
                .name("TestHotel")
                .address("Moscow, Tverskaya, 50")
                .build();

        h = hotelRepository.save(h);

        Room r = new Room.Builder()
                .number("110")
                .timesBooked(0)
                .hotel(h)
                .available(true)
                .build();

        r = hotelService.saveRoom(r);

        LocalDate s1 = LocalDate.now();
        LocalDate e1 = s1.plusDays(1);
        RoomLock lock = hotelService.holdRoomLock("req-c", r.getId(), s1, e1);
        Assertions.assertEquals(RoomLock.Status.HELD, lock.getStatus());
    }


}
