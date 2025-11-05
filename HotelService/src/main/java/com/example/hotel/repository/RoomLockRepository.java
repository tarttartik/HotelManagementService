package com.example.hotel.repository;

import com.example.hotel.entity.RoomLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface RoomLockRepository extends JpaRepository<RoomLock, Long> {

    @Query("""
    SELECT r FROM RoomLock r
    WHERE r.roomId = :roomId
      AND r.startDate <= :end
      AND r.endDate >= :start
      AND r.status IN :statuses""")
    List<RoomLock> findOverlapping(@Param("roomId") Long roomId, @Param("start") LocalDate start, @Param("end") LocalDate end,
                                   @Param("statuses") List<RoomLock.Status> statuses);

    Optional<RoomLock> findByRequestId(String requestId);
}