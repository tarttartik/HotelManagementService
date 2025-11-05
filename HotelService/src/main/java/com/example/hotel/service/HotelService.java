package com.example.hotel.service;

import com.example.hotel.repository.HotelRepository;
import com.example.hotel.repository.RoomRepository;
import com.example.hotel.repository.RoomLockRepository;
import com.example.hotel.entity.Hotel;
import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomLock;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class HotelService {
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomLockRepository roomLockRepository;

    public HotelService(HotelRepository hotelRep, RoomRepository roomRep, RoomLockRepository roomLockRep) {
        hotelRepository = hotelRep;
        roomRepository = roomRep;
        roomLockRepository = roomLockRep;
    }

    public List<Hotel> getHotels() { return hotelRepository.findAll(); }
    public Optional<Hotel> getHotelById(Long id) {return hotelRepository.findById(id);}
    public Hotel saveHotel(Hotel hotel) {return hotelRepository.save(hotel);}
    public void deleteHotel(Long id) {hotelRepository.deleteById(id);}
    public List<Hotel> getHotelsStats() { return hotelRepository.findAllOrderByTotalBookingsDesc();}

    public Optional<Room> getRoomById(Long id) {return roomRepository.findById(id);}
    public Room saveRoom(Room room) {return roomRepository.save(room);}
    public void deleteRoom(Long id) {roomRepository.deleteById(id);}
    public List<Room> getAvailableRoomsByHotelId(Long hotelId) { return roomRepository.findByHotel_IdAndAvailableTrue(hotelId);}

    public List<Room> getRoomsStats() { return roomRepository.findAll().stream()
            .sorted(Comparator.comparingLong(Room::getTimesBooked).reversed())
            .toList();}

    public List<Room> getAllAvailableRooms() {return roomRepository.findByAvailableTrue(); }

    @Transactional
    public RoomLock holdRoomLock(String requestId, Long roomId, LocalDate start, LocalDate end) {
        //обеспечение идемпотентности по requestId
        Optional<RoomLock> existingLock = roomLockRepository.findByRequestId(requestId);
        if(existingLock.isPresent()) return existingLock.get();

        List<RoomLock> overlappingLocks =
                roomLockRepository.findOverlapping(roomId, start, end,  Arrays.asList(RoomLock.Status.HELD, RoomLock.Status.CONFIRMED));

        //проверка на пересекающиеся брони
        if(!overlappingLocks.isEmpty())
            throw new IllegalStateException("Room unavailable during the specified period");

        var lock = new RoomLock.Builder()
                .roomId(roomId)
                .requestId(requestId)
                .startDate(start)
                .endDate(end)
                .status(RoomLock.Status.HELD)
                .build();

        return roomLockRepository.save(lock);
    }

    @Transactional
    public RoomLock confirmRoomLock(String requestId) {

        Optional<RoomLock> existingLock= roomLockRepository.findByRequestId(requestId);
        if(existingLock.isEmpty()) throw new IllegalStateException("Lock not found");

        var lock = existingLock.get();

        if(lock.isConfirmed()) return lock;
        if(lock.isReleased()) throw new IllegalStateException("Lock already released");

        lock.confirm();
        roomRepository.findById(lock.getRoomId()).ifPresent(room ->
                {
                    room.increaseTimesBooked();
                    roomRepository.save(room);
                }
        );

        return roomLockRepository.save(lock);
    }

    @Transactional
    public RoomLock releaseRoomLock(String requestId){

        Optional<RoomLock> existingLock= roomLockRepository.findByRequestId(requestId);
        if(existingLock.isEmpty()) throw new IllegalStateException("Lock not found");

        var lock = existingLock.get();

        if(lock.isConfirmed() || lock.isReleased()) return lock;

        lock.release();
        return roomLockRepository.save(lock);
    }
}
