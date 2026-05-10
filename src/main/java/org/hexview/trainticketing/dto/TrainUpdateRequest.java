package org.hexview.trainticketing.dto;

import jakarta.validation.Valid;

import java.util.List;

public record TrainUpdateRequest(
        String name,
        Integer capacity,
        @Valid List<StopTimeDto> stopTimes
) {
}
