package org.hexview.trainticketing.model;

import java.time.LocalDateTime;

/** One segment of a journey on a single train. */
public record JourneyLeg(
        String trainId,
        String trainName,
        String fromStationId,
        String toStationId,
        LocalDateTime departure,
        LocalDateTime arrival
) {
}
