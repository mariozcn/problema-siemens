package org.hexview.trainticketing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BookingRequest(
        @NotBlank String trainId,
        @NotBlank String fromStationId,
        @NotBlank String toStationId,
        @NotBlank String passengerName,
        @NotBlank @Email String email,
        @Min(1) int seats
) {
}
