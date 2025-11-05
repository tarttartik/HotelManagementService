package com.example.booking.dto;

public class AuthRequest {
    public static class Register {
        public String username;
        public String password;
    }
    public static class Login {
        public String username;
        public String password;
    }
}