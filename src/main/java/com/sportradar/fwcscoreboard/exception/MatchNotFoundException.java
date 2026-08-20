package com.sportradar.fwcscoreboard.exception;

import com.sportradar.fwcscoreboard.model.MatchId;

/**
 * Thrown when an operation references a {@link MatchId} that does not correspond to a match
 * currently tracked by the scoreboard (either it was never started, or it has already finished).
 */
public class MatchNotFoundException extends RuntimeException {

    public MatchNotFoundException(MatchId matchId) {
        super("No in-progress match found for id: " + matchId);
    }
}
