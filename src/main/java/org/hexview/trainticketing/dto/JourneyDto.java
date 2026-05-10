package org.hexview.trainticketing.dto;

import org.hexview.trainticketing.model.Journey;
import org.hexview.trainticketing.model.JourneyLeg;

import java.time.LocalDateTime;
import java.util.List;

public record JourneyDto(
        LocalDateTime departure,
        LocalDateTime arrival,
        int changeovers,
        List<JourneyLeg> legs
) {
    public static JourneyDto from(Journey j) {
        return new JourneyDto(j.departure(), j.arrival(), j.changeovers(), j.legs());
    }

    public static List<JourneyDto> from(List<Journey> journeys) {
        return journeys.stream().map(JourneyDto::from).toList();
    }
}
