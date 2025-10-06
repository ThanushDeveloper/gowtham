package com.example.rentaride.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "master_data")
@Getter
@Setter
public class MasterData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String extId; // corresponds to JS "id"

    private String district;
    private String location;

    @Column(length = 20)
    private String type; // location | car

    private String model;
    private String variant;
    private String photoUrl;
    private String brand;
}
