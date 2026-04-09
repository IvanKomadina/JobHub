package com.jobhub.dto.application;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class AssessmentRequest {

    @DecimalMin(value = "0.0", message = "Match score cannot be less than 0")
    @DecimalMax(value = "100.0", message = "Match score cannot be greater than 100")
    private BigDecimal matchScore;

    private String employerNotes;
}
