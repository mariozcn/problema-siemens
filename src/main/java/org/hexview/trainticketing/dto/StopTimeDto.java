package org.hexview.trainticketing.dto;

import jakarta.validation.constraints.NotBlank;
import org.hexview.trainticketing.model.StopTime;

import java.time.LocalDateTime;

public record StopTimeDto(
        @NotBlank String stationId,
        LocalDateTime arrival,
        LocalDateTime departure
) {
    public static StopTimeDto from(StopTime s) {
        return new StopTimeDto(s.getStationId(), s.getArrival(), s.getDeparture());
    }

    public StopTime toModel() {
        return new StopTime(stationId, arrival, departure);
    }
}
