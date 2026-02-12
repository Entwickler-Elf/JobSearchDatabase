package com.stromsland.dicejobsearch.controller;

import com.stromsland.dicejobsearch.service.SearchUpdateBroadcaster;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SearchUpdatesController {

    private final SearchUpdateBroadcaster broadcaster;

    public SearchUpdatesController(SearchUpdateBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @GetMapping("/api/search/updates")
    public SseEmitter updates() {
        return broadcaster.register();
    }
}