
package com.example.rentaride.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Instant pickupDate;

    @Column(nullable = false)
    private Instant dropOffDate;

    @Column(nullable = false)
    private String pickUpLocation;

    @Column(nullable = false)
    private String dropOffLocation;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(nullable = false)
    private String razorpayOrderId;

    @Column(nullable = false)
    private String razorpayPaymentId;

    @Column(nullable = false)
    private String status = "notBooked";

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}

