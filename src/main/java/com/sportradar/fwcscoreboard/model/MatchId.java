package com.sportradar.fwcscoreboard.model;

/**
 * Handle for a match, returned by {@link com.sportradar.fwcscoreboard.ScoreBoard#startMatch} and
 * used to refer back to that match in subsequent calls.
 *
 * <p>Callers should treat this as an opaque token: its internal representation (currently a
 * sequential counter) is not part of the public contract and should not be relied upon, parsed,
 * or used to construct arbitrary values. It is a public record rather than a fully encapsulated
 * type purely for simplicity &mdash; this is a convention, not something the API enforces.
 */
public record MatchId(long value) {}
