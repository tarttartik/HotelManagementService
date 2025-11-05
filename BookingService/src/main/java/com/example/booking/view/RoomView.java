package com.example.booking.view;

public record RoomView(Long id, String number, long timesBooked) {
    public Object getId() { return id; }
}
