package org.hexview.trainticketing.dto;

import jakarta.validation.constraints.Min;

public record DelayRequest(
        @Min(1) long minutes
) {
}
