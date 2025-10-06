package com.example.rentaride.dto;

public class AuthDtos {
    public record SignUpRequest(String username, String email, String password) {}
    public record SignInRequest(String email, String password) {}
    public record OAuthRequest(String name, String email, String photo) {}
    public record TokenResponse(String accessToken, String refreshToken, boolean isAdmin, boolean isUser, boolean isVendor, Long _id, String username, String email) {}
}
