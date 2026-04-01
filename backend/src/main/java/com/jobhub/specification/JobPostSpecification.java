package com.jobhub.specification;

import com.jobhub.dto.jobpost.JobPostFilterRequest;
import com.jobhub.entity.JobPost;
import com.jobhub.enums.PostStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class JobPostSpecification {

    public static Specification<JobPost> withFilters(JobPostFilterRequest filter, boolean isAdmin) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always show only active posts on public listing and let admin filter by status
            if (isAdmin) {
                if (filter.getPostStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), filter.getPostStatus()));
                }
            } else {
                predicates.add(cb.equal(root.get("status"), PostStatus.ACTIVE));
            }

            // Keyword search on title
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        "%" + filter.getKeyword().toLowerCase() + "%"
                ));
            }

            // Filter by category
            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(
                        root.get("category").get("id"),
                        filter.getCategoryId()
                ));
            }

            // Filter by location
            if (filter.getLocationId() != null) {
                predicates.add(cb.equal(
                        root.get("location").get("id"),
                        filter.getLocationId()
                ));
            }

            // Filter by employment type
            if (filter.getEmploymentType() != null) {
                predicates.add(cb.equal(
                        root.get("employmentType"),
                        filter.getEmploymentType()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
