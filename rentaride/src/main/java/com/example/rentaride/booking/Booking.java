package com.example.rentaride.booking;

import com.example.rentaride.user.User;
import com.example.rentaride.vehicle.Vehicle;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private LocalDateTime pickupDate;
    private LocalDateTime dropOffDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String pickUpLocation;
    private String dropOffLocation;

    private Integer totalPrice;

    private String razorpayOrderId;
    private String razorpayPaymentId;

    @Enumerated(EnumType.STRING)
    private Status status = Status.notBooked;

    private Instant createdAt = Instant.now();

    public enum Status {
        notBooked, booked, onTrip, notPicked, canceled, overDue, tripCompleted
    }
}

