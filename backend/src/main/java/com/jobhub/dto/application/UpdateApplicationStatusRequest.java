package com.jobhub.dto.application;

import com.jobhub.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateApplicationStatusRequest {

    @NotNull(message = "Status cannot be null")
    private ApplicationStatus status;
}