package com.jobhub.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Builder(access = AccessLevel.PRIVATE)
    private Category(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public static Category create(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Category name cannot be empty");

        return Category.builder()
                .name(name)
                .slug(generateSlug(name))
                .build();
    }

    public void updateName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Category name cannot be empty");

        this.name = name;
        this.slug = generateSlug(name);
    }

    private static String generateSlug(String name) {
        return name.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }
}
