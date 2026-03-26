package com.jobhub.repository;

import com.jobhub.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByCityAndCountry(String city, String country);
    boolean existsByCityAndCountry(String city, String country);
}