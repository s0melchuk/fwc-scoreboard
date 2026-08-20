/**
 * A small library for tracking football matches currently in progress: starting a match,
 * updating its score, finishing it, and reading an ordered summary or a single match's state.
 *
 * <p>The entry point is {@link com.sportradar.fwcscoreboard.ScoreBoard}, with {@link
 * com.sportradar.fwcscoreboard.InMemoryScoreBoard} as its only implementation. Domain types live
 * in {@link com.sportradar.fwcscoreboard.model}; failure modes are in {@link
 * com.sportradar.fwcscoreboard.exception}.
 */
package com.sportradar.fwcscoreboard;
