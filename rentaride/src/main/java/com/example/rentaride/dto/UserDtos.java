package com.example.rentaride.dto;

import java.time.Instant;
import java.util.List;

public class UserDtos {
    public record VehicleDetail(Long _id, String registeration_number, String car_title, String car_description,
                                String company, String name, String model, Integer year_made, String fuel_type,
                                Integer seats, String transmition, List<String> image, String description,
                                String title, Integer price, String base_package, String car_type,
                                String location, String district, String ratting) {}

    public record BookingRequest(String user_id, String vehicle_id, Integer totalPrice, String pickupDate, String dropoffDate,
                                 String pickup_location, String dropoff_location, String pickup_district,
                                 String razorpayPaymentId, String razorpayOrderId) {}

    public record ProfileEditRequest(String username, String email, String phoneNumber, String adress) {}

    public record FilterOption(String type, Boolean suv, Boolean sedan, Boolean hatchback, Boolean automatic, Boolean manual) {}

    public record LocationsLov(String type, String model, String brand, String district, String location) {}

    public record RazorpayOrderRequest(Integer totalPrice, String pickUpLocation, String pickup_district, String pickup_location, String dropoff_location) {}
}
