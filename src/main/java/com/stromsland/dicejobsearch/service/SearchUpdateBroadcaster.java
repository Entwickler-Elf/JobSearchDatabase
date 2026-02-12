package com.stromsland.dicejobsearch.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SearchUpdateBroadcaster {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter register() {
        SseEmitter emitter = new SseEmitter(0L); // no timeout (or set e.g. 30_000L)
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        return emitter;
    }

    public void completed(String query, int resultCount, long elapsedMs) {
        send("search-completed", new Payload("COMPLETED", query, resultCount, elapsedMs, null));
    }

    public void failed(String query, String errorMessage, long elapsedMs) {
        send("search-failed", new Payload("FAILED", query, 0, elapsedMs, errorMessage));
    }

    private void send(String eventName, Payload payload) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(payload));
            } catch (IOException ex) {
                emitters.remove(emitter);
            }
        }
    }

    public record Payload(
            String status,
            String query,
            int resultCount,
            long elapsedMs,
            String error,
            String timestamp
    ) {
        public Payload(String status, String query, int resultCount, long elapsedMs, String error) {
            this(status, query, resultCount, elapsedMs, error, Instant.now().toString());
        }
    }
}