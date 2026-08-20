package com.sportradar.fwcscoreboard;

import com.sportradar.fwcscoreboard.exception.IllegalScoreException;
import com.sportradar.fwcscoreboard.exception.MatchNotFoundException;
import com.sportradar.fwcscoreboard.exception.TeamAlreadyPlayingException;
import com.sportradar.fwcscoreboard.model.Match;
import com.sportradar.fwcscoreboard.model.MatchId;
import java.util.List;
import java.util.Optional;

/**
 * A scoreboard for football matches in progress.
 *
 * <p>Implementations track zero or more simultaneous matches, each identified by the {@link
 * MatchId} returned from {@link #startMatch}. Finished matches are removed from the board rather
 * than retained in some "finished" state; the board only ever represents live matches.
 */
public interface ScoreBoard {

    /**
     * Starts a new match between the two named teams with an initial score of 0-0.
     *
     * @param homeTeam the home team's name; must not be null or blank
     * @param awayTeam the away team's name; must not be null or blank, and must differ from
     *     {@code homeTeam}
     * @return the id of the newly started match
     * @throws NullPointerException if either team name is {@code null}
     * @throws IllegalArgumentException if either team name is blank, or the two names are equal
     * @throws TeamAlreadyPlayingException if either team is already in an in-progress match
     */
    MatchId startMatch(String homeTeam, String awayTeam);

    /**
     * Sets the absolute score of an in-progress match, replacing whatever score it currently has.
     * This is not an increment: pass the match's full current score, e.g. {@code updateScore(id,
     * 2, 1)} to record the match as 2-1, regardless of what it was before.
     *
     * @throws NullPointerException if {@code matchId} is {@code null}
     * @throws MatchNotFoundException if {@code matchId} does not refer to an in-progress match
     * @throws IllegalArgumentException if either score is negative
     * @throws IllegalScoreException if either score is lower than the match's current recorded
     *     score
     */
    void updateScore(MatchId matchId, int homeScore, int awayScore);

    /**
     * Ends a match, removing it from the board. It will no longer appear in {@link #getSummary()}
     * or be resolvable via its id.
     *
     * @throws NullPointerException if {@code matchId} is {@code null}
     * @throws MatchNotFoundException if {@code matchId} does not refer to an in-progress match
     */
    void finishMatch(MatchId matchId);

    /**
     * Returns all in-progress matches, ordered by total score descending; matches tied on total
     * score are ordered by most recently started first.
     */
    List<Match> getSummary();

    /**
     * Looks up a single in-progress match by id.
     *
     * <p>This is the "additional operation" required by the exercise brief. A scoreboard summary
     * is naturally followed by drilling into one match (a client polling for updates on a match
     * it's already displaying, for instance), and an explicit lookup lets a caller do that
     * without re-deriving it from {@link #getSummary()} and without throwing for a match that may
     * have already finished &mdash; unlike the other operations, "not found" is an expected,
     * non-exceptional outcome here.
     *
     * @return the match, or {@link Optional#empty()} if {@code matchId} is {@code null} or does
     *     not refer to an in-progress match &mdash; unlike the other operations, a null or unknown
     *     id here is a normal "nothing to show" outcome, not an error, so this method never throws
     *     for it
     */
    Optional<Match> getMatch(MatchId matchId);
}
