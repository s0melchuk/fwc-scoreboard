package com.sportradar.fwcscoreboard.model;

import java.util.Objects;

/**
 * A football team identified by name.
 *
 * <p>Equality and hashing are name-based. Names are compared as given (no case-folding or
 * trimming beyond rejecting blank input) since the scoreboard does not attempt to normalize
 * or canonicalize team names against any external reference data.
 *
 * @throws NullPointerException if {@code name} is {@code null}
 * @throws IllegalArgumentException if {@code name} is blank
 */
public record Team(String name) {

    public Team {
        Objects.requireNonNull(name, "Team name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Team name must not be blank");
        }
    }
}
