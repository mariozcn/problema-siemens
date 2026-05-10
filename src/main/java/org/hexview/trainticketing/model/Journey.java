package org.hexview.trainticketing.model;

import java.time.LocalDateTime;
import java.util.List;

/** A way to get from origin to destination, possibly with changeovers. */
public record Journey(List<JourneyLeg> legs) {

    public LocalDateTime departure() {
        return legs.get(0).departure();
    }

    public LocalDateTime arrival() {
        return legs.get(legs.size() - 1).arrival();
    }

    public int changeovers() {
        return legs.size() - 1;
    }
}
