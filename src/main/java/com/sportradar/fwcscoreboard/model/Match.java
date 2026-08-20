package com.sportradar.fwcscoreboard.model;

import java.util.Objects;

/**
 * An immutable snapshot of a match in progress: its identity, the two teams involved, the
 * current score, and the order in which it was started relative to other matches.
 *
 * <p>{@code startOrder} is a monotonically increasing sequence number assigned when the match is
 * started, not a wall-clock timestamp. Using a logical clock rather than {@link
 * java.time.Instant} avoids flaky tie-breaking when two matches start within the same clock tick,
 * and keeps ordering deterministic and easy to test.
 */
public record Match(MatchId id, Team homeTeam, Team awayTeam, Score score, long startOrder) {

    public Match {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(homeTeam, "homeTeam must not be null");
        Objects.requireNonNull(awayTeam, "awayTeam must not be null");
        Objects.requireNonNull(score, "score must not be null");
        if (homeTeam.equals(awayTeam)) {
            throw new IllegalArgumentException("A team cannot play itself: " + homeTeam);
        }
    }

    /** Returns a copy of this match with its score replaced; identity and start order are preserved. */
    public Match withScore(Score newScore) {
        return new Match(id, homeTeam, awayTeam, newScore, startOrder);
    }

    public long totalScore() {
        return score.total();
    }
}
