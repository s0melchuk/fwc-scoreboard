/**
 * Immutable domain types shared by the scoreboard API: {@link com.sportradar.fwcscoreboard.model.Team},
 * {@link com.sportradar.fwcscoreboard.model.Score}, {@link com.sportradar.fwcscoreboard.model.Match},
 * and the {@link com.sportradar.fwcscoreboard.model.MatchId} handle used to refer to a match.
 *
 * <p>None of these types hold any behaviour beyond validating their own invariants; all
 * scoreboard logic lives in {@link com.sportradar.fwcscoreboard.InMemoryScoreBoard}.
 */
package com.sportradar.fwcscoreboard.model;
