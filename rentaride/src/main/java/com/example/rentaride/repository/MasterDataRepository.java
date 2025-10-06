
package com.example.rentaride.repository;

import com.example.rentaride.domain.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterDataRepository extends JpaRepository<MasterData, Long> {
    List<MasterData> findByType(String type);
}

