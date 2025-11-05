package com.example.hotel.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Hotel {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String address;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();

    //JPA конструктор - не удалять!!
    protected Hotel() {}

    private Hotel(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.address = builder.address;
        this.rooms = builder.rooms != null ? builder.rooms : new ArrayList<>();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public List<Room> getRooms() { return rooms; }

    public static class Builder {
        private Long id;
        private String name;
        private String address;
        private List<Room> rooms = new ArrayList<>();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder rooms(List<Room> rooms) {
            this.rooms = rooms;
            return this;
        }

        public Builder addRoom(Room room) {
            this.rooms.add(room);
            return this;
        }

        public Hotel build() {
            return new Hotel(this);
        }
    }

    public Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .name(this.name)
                .address(this.address)
                .rooms(new ArrayList<>(this.rooms));
    }
}