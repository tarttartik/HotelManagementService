package com.example.hotel.repository;

import com.example.hotel.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    @Query("""
        SELECT h
        FROM Hotel h
        LEFT JOIN h.rooms r
        GROUP BY h
        ORDER BY SUM(r.timesBooked) DESC
    """)
    List<Hotel> findAllOrderByTotalBookingsDesc();
}