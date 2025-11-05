package com.example.booking.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue private Long id;
    @Column(unique = true, nullable = false) private String username;
    @Column(nullable = false) private String passwordHashed;
    @Column(nullable = false) private String role;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return passwordHashed; }
    public String getRole() { return role; }

    protected User() {}

    private User(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.passwordHashed = builder.passwordHashed;
        this.role = builder.role;
    }

    public static class Builder {
        private Long id;
        private String username;
        private String passwordHashed;
        private String role;

        public Builder() {}

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder passwordHashed(String passwordHashed) {
            this.passwordHashed = passwordHashed;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    public Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .username(this.username)
                .passwordHashed(this.passwordHashed)
                .role(this.role);
    }

}