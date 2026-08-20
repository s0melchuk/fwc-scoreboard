package com.sportradar.fwcscoreboard.exception;

import com.sportradar.fwcscoreboard.model.Team;

/**
 * Thrown by {@link com.sportradar.fwcscoreboard.ScoreBoard#startMatch} when either team named is
 * already involved in another in-progress match.
 *
 * <p>A real-world team cannot physically play two matches at once, so allowing this would let the
 * scoreboard drift into a state that can never correspond to reality. This is a deliberate design
 * choice beyond what the brief states explicitly &mdash; see the README for the reasoning.
 */
public class TeamAlreadyPlayingException extends RuntimeException {

    public TeamAlreadyPlayingException(Team team) {
        super("Team is already playing in another in-progress match: " + team.name());
    }
}
