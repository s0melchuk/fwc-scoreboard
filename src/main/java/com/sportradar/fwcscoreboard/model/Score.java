package com.sportradar.fwcscoreboard.model;

/**
 * An immutable pair of goal counts for a match, home and away.
 *
 * <p>Scores are absolute goal counts, not deltas: {@link
 * com.sportradar.fwcscoreboard.ScoreBoard#updateScore} always replaces the score outright rather
 * than incrementing it. Both values must be non-negative.
 */
public record Score(int home, int away) {

    public static final Score INITIAL = new Score(0, 0);

    public Score {
        if (home < 0 || away < 0) {
            throw new IllegalArgumentException(
                    "Score values must not be negative: home=%d, away=%d".formatted(home, away));
        }
    }

    public int total() {
        return home + away;
    }
}
