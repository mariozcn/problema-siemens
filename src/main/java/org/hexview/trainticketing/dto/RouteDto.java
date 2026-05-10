package org.hexview.trainticketing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.hexview.trainticketing.model.Route;

import java.util.List;

public record RouteDto(
        @NotBlank String id,
        @NotBlank String name,
        @NotEmpty List<String> stationIds
) {
    public static RouteDto from(Route r) {
        return new RouteDto(r.getId(), r.getName(), r.getStationIds());
    }

    public Route toModel() {
        return new Route(id, name, stationIds);
    }
}
