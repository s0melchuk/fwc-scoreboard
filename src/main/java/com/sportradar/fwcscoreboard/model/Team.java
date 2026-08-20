package com.sportradar.fwcscoreboard.model;

/**
 * A football team identified by name.
 *
 * <p>Equality and hashing are name-based. Names are compared as given (no case-folding or
 * trimming beyond rejecting blank input) since the scoreboard does not attempt to normalize
 * or canonicalize team names against any external reference data.
 */
public record Team(String name) {

    public Team {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Team name must not be blank");
        }
    }
}
