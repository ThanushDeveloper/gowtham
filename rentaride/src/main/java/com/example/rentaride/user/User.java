package com.example.rentaride.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;

    private String adress;

    @Column(nullable = false)
    private String passwordHash;

    private String profilePicture;

    private boolean isUser;
    private boolean isAdmin;
    private boolean isVendor;

    private String refreshToken;
}
