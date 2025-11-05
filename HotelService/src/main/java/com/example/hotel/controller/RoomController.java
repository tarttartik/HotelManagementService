package com.example.hotel.controller;

import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomLock;
import com.example.hotel.service.HotelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/rooms")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearer-jwt")
public class RoomController {
    private final HotelService hotelService;

    public RoomController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    //ADMIN ENDPOINTS

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping
    public Room create(@RequestBody Room r) { return hotelService.saveRoom(r); }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Room> update(@PathVariable Long id, @RequestBody Room r) {
        return hotelService.getRoomById(id)
                .map(room -> {
                    var updated = r.toBuilder().id(id).build();
                    return ResponseEntity.ok(hotelService.saveRoom(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    //USER ENDPOINTS

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return hotelService.getRoomById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public List<Room> getRoomsStats() {
       return hotelService.getRoomsStats();
    }

    @GetMapping("/topAvailable")
    public Room getTopAvailableRoom() {
        List<Room> availableRooms = hotelService.getAllAvailableRooms();
        return availableRooms.isEmpty() ? null : availableRooms.get(0);
    }

    @PostMapping("/hold/{id}")
    public ResponseEntity<RoomLock> holdRoomLock(@PathVariable Long id, @RequestBody Map<String, String> request) {
        var requestId = request.get("requestId");
        var start = LocalDate.parse(request.get("startDate"));
        var end = LocalDate.parse(request.get("endDate"));

        try {
            var lock =  hotelService.holdRoomLock(requestId, id, start, end);
            return ResponseEntity.ok(lock);
        }
        catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PostMapping("/confirm/{id}")
    public ResponseEntity<RoomLock> confirmRoomLock(@PathVariable Long id, @RequestBody Map<String, String> request)
    {
        var requestId = request.get("requestId");
        try {
            var lock =  hotelService.confirmRoomLock(requestId);
            return ResponseEntity.ok(lock);
        }
        catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PostMapping("/release/{id}")
    public ResponseEntity<RoomLock> releaseRoomLock(@PathVariable Long id, @RequestBody Map<String, String> request)
    {
        var requestId = request.get("requestId");
        try {
            var lock =  hotelService.releaseRoomLock(requestId);
            return ResponseEntity.ok(lock);
        }
        catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        }
    }
}