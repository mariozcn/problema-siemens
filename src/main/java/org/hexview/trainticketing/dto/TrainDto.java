package org.hexview.trainticketing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.hexview.trainticketing.model.Train;

import java.util.List;

public record TrainDto(
        @NotBlank String id,
        @NotBlank String name,
        @NotBlank String routeId,
        @Min(1) int capacity,
        @NotEmpty @Valid List<StopTimeDto> stopTimes,
        long delayMinutes
) {
    public static TrainDto from(Train t) {
        return new TrainDto(
                t.getId(), t.getName(), t.getRouteId(), t.getCapacity(),
                t.getStopTimes().stream().map(StopTimeDto::from).toList(),
                t.getDelay().toMinutes());
    }

    public Train toModel() {
        return new Train(id, name, routeId, capacity,
                stopTimes.stream().map(StopTimeDto::toModel).toList());
    }
}
