package com.example.hotel.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_lock_request", columnNames = {"requestId"})
})
public class RoomLock {

    public enum Status
    {
        CONFIRMED,
        HELD,
        RELEASED
    }

    @Id @GeneratedValue private Long id;
    private Long roomId;
    private String requestId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt = Instant.now();
    @Enumerated(EnumType.STRING) private Status status;

    //JPA конструктор - не удалять!!
    protected RoomLock() { };

    private RoomLock(Builder builder) {
        this.id = builder.id;
        this.roomId = builder.roomId;
        this.requestId = builder.requestId;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.createdAt = builder.createdAt != null ? builder.createdAt : Instant.now();
        this.status = builder.status;
    }

    public Instant getCreatedAt() { return createdAt; }
    public Status getStatus() { return status; }
    public LocalDate getEndDate() { return endDate; }
    public LocalDate getStartDate() { return startDate; }
    public String getRequestId() { return requestId; }
    public Long getRoomId() { return roomId; }
    public Long getId() { return id; }
    public Status getReservationStatus() { return status; }
    public boolean isReleased() { return status == Status.RELEASED; }
    public boolean isConfirmed() { return status == Status.CONFIRMED; }

    public void confirm() {status = Status.CONFIRMED;}
    public void release() {status = Status.RELEASED;}

    public static class Builder {
        private Long id;
        private Long roomId;
        private String requestId;
        private LocalDate startDate;
        private LocalDate endDate;
        private Instant createdAt;
        private Status status;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder roomId(Long roomId) {
            this.roomId = roomId;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
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

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public RoomLock build() {
            return new RoomLock(this);
        }
    }

    public Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .roomId(this.roomId)
                .requestId(this.requestId)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .createdAt(this.createdAt)
                .status(this.status);
    }
}