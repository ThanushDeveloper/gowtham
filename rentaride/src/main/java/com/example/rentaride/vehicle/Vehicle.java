
package com.example.rentaride.vehicle;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String registerationNumber;

    private String carTitle;
    private String carDescription;

    private Instant createdAt;
    private Instant updatedAt;

    private String remark;

    private String company;
    private String name;
    private String model;
    private Integer yearMade;

    private String fuelType; // petrol|diesel|electric|hybrid

    private String rentedBy;

    private Integer seats;

    private String transmition; // manual|automatic

    @ElementCollection
    @CollectionTable(name = "vehicle_images", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "url")
    private List<String> image = new ArrayList<>();

    private String description;
    private String title;
    private Integer price;
    private String basePackage;

    private Boolean withOrWithoutFuel;

    private LocalDate insuranceEnd;
    private LocalDate registerationEnd;
    private LocalDate pollutionEnd;

    private String carType; // sedan|suv|hatchback

    @Column(nullable = false)
    private String isDeleted = "false"; // matches frontend expecting string

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String district;

    private Boolean isBooked = false;
    private Boolean isAdminAdded = true;
    private String addedBy = "admin"; // user id or admin
    private Boolean isAdminApproved = true;
    private Boolean isRejected = false;
}

