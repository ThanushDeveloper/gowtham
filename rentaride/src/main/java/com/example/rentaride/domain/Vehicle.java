package com.example.rentaride.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("_id")
    private Long id;

    @Column(nullable = false, unique = true)
    @JsonProperty("registeration_number")
    private String registrationNumber;

    @JsonProperty("car_title")
    private String carTitle;
    @Column(length = 2000)
    @JsonProperty("car_description")
    private String carDescription;

    private Instant createdAt;
    private Instant updatedAt;
    private String remark;

    private String company;
    private String name;
    private String model;
    @JsonProperty("year_made")
    private Integer yearMade;
    @JsonProperty("fuel_type")
    private String fuelType; // petrol, diesel, electric, hybrid
    @JsonProperty("ratting")
    private String rating; // "1".."5" for compatibility
    private Integer seats;
    @JsonProperty("transmition")
    private String transmission; // manual, automatic

    @ElementCollection
    @CollectionTable(name = "vehicle_images", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "url")
    private List<String> image = new ArrayList<>();

    private String description;
    private String title;
    private Integer price;
    @JsonProperty("base_package")
    private String basePackage;
    private Boolean withOrWithoutFuel;

    @JsonProperty("insurance_end")
    private Instant insuranceEnd;
    @JsonProperty("registeration_end")
    private Instant registrationEnd;
    @JsonProperty("pollution_end")
    private Instant pollutionEnd;

    private String carType;
    @Column(nullable = false)
    private String isDeleted = "false"; // keep string for compatibility

    @Column(nullable = false)
    private String location;
    @Column(nullable = false)
    private String district;

    private Boolean isBooked = false;
    private Boolean isAdminAdded = true;
    private String addedBy = "admin";
    private Boolean isAdminApproved = true;
    private Boolean isRejected = false;
}
