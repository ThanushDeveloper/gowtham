package com.example.rentaride.dev;

import com.example.rentaride.master.MasterData;
import com.example.rentaride.master.MasterDataRepository;
import com.example.rentaride.user.User;
import com.example.rentaride.user.UserRepository;
import com.example.rentaride.vehicle.Vehicle;
import com.example.rentaride.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Profile({"default"})
public class DevDataLoader implements CommandLineRunner {

    private final MasterDataRepository masterRepo;
    private final VehicleRepository vehicleRepo;
    private final UserRepository userRepo;

    @Override
    public void run(String... args) {
        if (masterRepo.count() == 0) {
            MasterData m1 = new MasterData();
            m1.setType("location"); m1.setDistrict("ernakulam"); m1.setLocation("ekm-city");
            MasterData m2 = new MasterData();
            m2.setType("car"); m2.setBrand("toyota"); m2.setModel("glanza");
            masterRepo.saveAll(List.of(m1, m2));
        }

        if (vehicleRepo.count() == 0) {
            Vehicle v = new Vehicle();
            v.setRegisterationNumber("KL-07-ABC-1234");
            v.setCompany("toyota");
            v.setName("Glanza");
            v.setModel("glanza");
            v.setYearMade(2022);
            v.setFuelType("petrol");
            v.setSeats(5);
            v.setTransmition("manual");
            v.setPrice(1500);
            v.setCarType("hatchback");
            v.setLocation("ekm-city");
            v.setDistrict("ernakulam");
            vehicleRepo.save(v);
        }

        if (userRepo.count() == 0) {
            User u = new User();
            u.setUsername("demo");
            u.setEmail("demo@example.com");
            u.setPasswordHash("demo");
            u.setUser(true);
            userRepo.save(u);
        }
    }
}
