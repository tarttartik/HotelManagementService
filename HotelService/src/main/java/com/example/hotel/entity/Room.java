package com.example.hotel.entity;


import jakarta.persistence.*;

@Entity
public class Room {
    @Id
    @GeneratedValue
    private Long id;
    private String number;
    private boolean available = true;
    private int timesBooked = 0;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    public long getId() { return id; }
    public boolean isAvailable() {return available; }
    public int getTimesBooked() { return timesBooked;}
    public String getNumber() { return number; }
    public Long getHotelId() { return hotel.getId(); }
    public Hotel getHotel() { return hotel; }

    public  void increaseTimesBooked() { timesBooked++; }

    //JPA конструктор - не удалять!!
    protected Room() {}

    private Room(Builder builder) {
        this.id = builder.id;
        this.number = builder.number;
        this.available = builder.available;
        this.timesBooked = builder.timesBooked;
        this.hotel = builder.hotel;
    }

    public static class Builder {
        private Long id;
        private String number;
        private boolean available = true;
        private int timesBooked = 0;
        private Hotel hotel;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder number(String number) {
            this.number = number;
            return this;
        }

        public Builder available(boolean available) {
            this.available = available;
            return this;
        }

        public Builder timesBooked(int timesBooked) {
            this.timesBooked = timesBooked;
            return this;
        }

        public Builder hotel(Hotel hotel)
        {
            this.hotel = hotel;
            return this;
        }

        public Room build() {
            return new Room(this);
        }
    }

    public Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .number(this.number)
                .available(this.available)
                .timesBooked(this.timesBooked)
                .hotel(this.hotel);
    }
}