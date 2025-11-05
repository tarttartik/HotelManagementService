package com.example.booking.entity;


import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Table(name = "bookings")
public class Booking {

    public enum Status
    {
        CONFIRMED,
        PENDING,
        CANCELLED
    }

    @Id @GeneratedValue private Long id;
    private Long userId;
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)  private Status status;
    private OffsetDateTime createdAt;
    @Column(unique = true)  private String requestId;

    //JPA конструктор - НЕ УДАЛЯТЬ!
    protected Booking() {};

    private Booking(Builder builder)
    {
        this.id = builder.id;
        this.requestId = builder.requestId;
        this.userId = builder.userId;
        this.roomId = builder.roomId;
        this.status = builder.status;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.createdAt = builder.createdAt;
    }


    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getRoomId() { return roomId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Status getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public String getRequestId() { return requestId; }

    public void confirm() { status = Status.CONFIRMED; }
    public void cancel() { status = Status.CANCELLED; }

    public static class Builder {
        private Long id;
        private Long userId;
        private Long roomId;
        private LocalDate startDate;
        private LocalDate endDate;
        private Status status;
        private OffsetDateTime createdAt;
        private String requestId;

        public Builder() {}

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder roomId(Long roomId) {
            this.roomId = roomId;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Booking build() {
            return new Booking(this);
        }
    }
}