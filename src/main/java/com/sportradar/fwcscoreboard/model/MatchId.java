package com.sportradar.fwcscoreboard.model;

/**
 * Opaque handle for a match, returned by {@link
 * com.sportradar.fwcscoreboard.ScoreBoard#startMatch} and used to refer back to that match in
 * subsequent calls.
 *
 * <p>Callers must treat this as an opaque token: its internal representation (currently a
 * sequential counter) is not part of the public contract and must not be relied upon, parsed, or
 * constructed by client code.
 */
public record MatchId(long value) {}
