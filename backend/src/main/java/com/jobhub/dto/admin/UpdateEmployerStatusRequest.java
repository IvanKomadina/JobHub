package com.jobhub.dto.admin;

import com.jobhub.enums.EmployerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateEmployerStatusRequest {

    @NotNull(message = "Status cannot be null")
    private EmployerStatus status;
}