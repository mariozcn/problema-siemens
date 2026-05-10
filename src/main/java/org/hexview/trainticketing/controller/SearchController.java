package org.hexview.trainticketing.controller;

import org.hexview.trainticketing.dto.JourneyDto;
import org.hexview.trainticketing.service.RouteSearchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final RouteSearchService searchService;

    public SearchController(RouteSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public List<JourneyDto> search(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime earliestDeparture) {
        return JourneyDto.from(searchService.findJourneys(from, to, earliestDeparture));
    }
}
