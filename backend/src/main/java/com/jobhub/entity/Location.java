package com.jobhub.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locations", uniqueConstraints = @UniqueConstraint(columnNames = {"city", "country"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String country;

    @Builder(access = AccessLevel.PRIVATE)
    private Location(String city, String country) {
        this.city = city;
        this.country = country;
    }

    public static Location create(String city, String country) {
        if (city == null || city.isBlank()) throw new IllegalArgumentException("City cannot be empty");
        if (country == null || country.isBlank()) throw new IllegalArgumentException("Country cannot be empty");

        return Location.builder()
                .city(city)
                .country(country)
                .build();
    }
}
