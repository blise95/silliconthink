package com.silliconthink.oauth;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class OAuthTicketStore {

    private static final long STATE_TTL_SECONDS = 600;
    private static final long CODE_TTL_SECONDS = 120;

    private final Map<String, Instant> states = new ConcurrentHashMap<>();
    private final Map<String, CodeEntry> codes = new ConcurrentHashMap<>();

    public String createState() {
        purge();
        String state = UUID.randomUUID().toString().replace("-", "");
        states.put(state, Instant.now().plusSeconds(STATE_TTL_SECONDS));
        return state;
    }

    public boolean consumeState(String state) {
        purge();
        Instant exp = states.remove(state);
        return exp != null && Instant.now().isBefore(exp);
    }

    public String createExchangeCode(Long userId) {
        purge();
        String code = UUID.randomUUID().toString().replace("-", "");
        codes.put(code, new CodeEntry(userId, Instant.now().plusSeconds(CODE_TTL_SECONDS)));
        return code;
    }

    public Optional<Long> consumeExchangeCode(String code) {
        purge();
        CodeEntry entry = codes.remove(code);
        if (entry == null || Instant.now().isAfter(entry.expireAt())) {
            return Optional.empty();
        }
        return Optional.of(entry.userId());
    }

    private void purge() {
        Instant now = Instant.now();
        states.entrySet().removeIf(e -> now.isAfter(e.getValue()));
        codes.entrySet().removeIf(e -> now.isAfter(e.getValue().expireAt()));
    }

    private record CodeEntry(Long userId, Instant expireAt) {
    }
}
