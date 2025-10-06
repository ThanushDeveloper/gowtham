
package com.example.rentaride.service;

import com.example.rentaride.domain.MasterData;
import com.example.rentaride.domain.Vehicle;
import com.example.rentaride.repository.MasterDataRepository;
import com.example.rentaride.repository.VehicleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements ApplicationRunner {
    private final MasterDataRepository masterRepo;
    private final VehicleRepository vehicleRepo;

    public DataSeeder(MasterDataRepository masterRepo, VehicleRepository vehicleRepo) {
        this.masterRepo = masterRepo;
        this.vehicleRepo = vehicleRepo;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (masterRepo.count() == 0) {
            MasterData loc1 = new MasterData();
            loc1.setType("location");
            loc1.setDistrict("kochi");
            loc1.setLocation("mg road");
            masterRepo.save(loc1);

            MasterData loc2 = new MasterData();
            loc2.setType("location");
            loc2.setDistrict("kochi");
            loc2.setLocation("vyttila");
            masterRepo.save(loc2);

            MasterData car1 = new MasterData();
            car1.setType("car");
            car1.setModel("swift");
            car1.setBrand("maruti");
            masterRepo.save(car1);
        }

        if (vehicleRepo.count() == 0) {
            Vehicle v = new Vehicle();
            v.setRegistrationNumber("KL-07-AB-1234");
            v.setCompany("maruti");
            v.setName("Swift");
            v.setModel("swift");
            v.setPrice(2000);
            v.setYearMade(2020);
            v.setFuelType("petrol");
            v.setSeats(5);
            v.setTransmission("manual");
            v.setCarType("hatchback");
            v.setDistrict("kochi");
            v.setLocation("mg road");
            vehicleRepo.save(v);
        }
    }
}

