package com.example.rentaride.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "master_data")
public class MasterData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String externalId; // compatibility with old 'id' field

    private String district;
    private String location;

    @Column(nullable = false)
    private String type; // location | car

    private String model;
    private String variant;
    private String photoUrl;
    private String brand;
}
