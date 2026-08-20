package com.sportradar.fwcscoreboard.exception;

import com.sportradar.fwcscoreboard.model.Score;

/**
 * Thrown when {@link com.sportradar.fwcscoreboard.ScoreBoard#updateScore} is called with a score
 * that regresses a team's goal count. Football scores only go up during a match, so a lower goal
 * count than what is already recorded almost always indicates a caller bug (e.g. stale data or
 * swapped arguments) rather than a legitimate correction.
 */
public class IllegalScoreException extends RuntimeException {

    public IllegalScoreException(Score current, Score attempted) {
        super("Score cannot decrease: current=%s, attempted=%s".formatted(current, attempted));
    }
}
