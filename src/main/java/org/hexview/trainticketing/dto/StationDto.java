package org.hexview.trainticketing.dto;

import jakarta.validation.constraints.NotBlank;
import org.hexview.trainticketing.model.Station;

public record StationDto(
        @NotBlank String id,
        @NotBlank String name
) {
    public static StationDto from(Station s) {
        return new StationDto(s.getId(), s.getName());
    }

    public Station toModel() {
        return new Station(id, name);
    }
}
