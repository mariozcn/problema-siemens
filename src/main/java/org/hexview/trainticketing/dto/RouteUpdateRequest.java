package org.hexview.trainticketing.dto;

import java.util.List;

public record RouteUpdateRequest(
        String name,
        List<String> stationIds
) {
}
