package com.example.hotel.controller;

import com.example.hotel.entity.Hotel;
import com.example.hotel.entity.Room;
import com.example.hotel.service.HotelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearer-jwt")
public class HotelController {
    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    //ADMIN ENDPOINTS

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public Hotel create(@RequestBody Hotel h) { return hotelService.saveHotel(h); }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Hotel> update(@PathVariable Long id, @RequestBody Hotel r) {
        return hotelService.getHotelById(id)
                .map(room -> {
                    var updated = r.toBuilder().id(id).build();
                    return ResponseEntity.ok(hotelService.saveHotel(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    //USER ENDPOINTS
    @GetMapping("/{id}")
    public ResponseEntity<Hotel> get(@PathVariable Long id) {
        return hotelService.getHotelById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public List<Hotel> getStats() {
        return hotelService.getHotelsStats();
    }

    @GetMapping
    public List<Hotel> getAll() { return hotelService.getHotels(); }

    @GetMapping("/availableRooms/{id}")
    public List<Room> getHotelAvailableRooms(@PathVariable Long id) { return hotelService.getAvailableRoomsByHotelId(id);}
}
