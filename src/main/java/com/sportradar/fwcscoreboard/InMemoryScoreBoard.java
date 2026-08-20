package com.sportradar.fwcscoreboard;

import com.sportradar.fwcscoreboard.exception.IllegalScoreException;
import com.sportradar.fwcscoreboard.exception.MatchNotFoundException;
import com.sportradar.fwcscoreboard.exception.TeamAlreadyPlayingException;
import com.sportradar.fwcscoreboard.model.Match;
import com.sportradar.fwcscoreboard.model.MatchId;
import com.sportradar.fwcscoreboard.model.Score;
import com.sportradar.fwcscoreboard.model.Team;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * An in-memory {@link ScoreBoard} backed by a {@link LinkedHashMap}.
 *
 * <p>
 * Not built for high-throughput concurrent access: every public method is synchronized on
 * {@code this}, giving simple, easy-to-verify correctness for the modest number of simultaneous
 * matches a real scoreboard ever has (a handful, not thousands). A lock-free or finer-grained
 * design would only pay for itself at a scale this library isn't intended for.
 */
public final class InMemoryScoreBoard implements ScoreBoard {

    private final Map<MatchId, Match> matches = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public synchronized MatchId startMatch(String homeTeam, String awayTeam) {
        Team home = new Team(homeTeam);
        Team away = new Team(awayTeam);

        matches.values().stream()
                .flatMap(m -> Stream.of(m.homeTeam(), m.awayTeam()))
                .filter(t -> t.equals(home) || t.equals(away))
                .findAny()
                .ifPresent(t -> {
                    throw new TeamAlreadyPlayingException(t);
                });

        MatchId id = new MatchId(sequence.incrementAndGet());
        Match match = new Match(id, home, away, Score.INITIAL, id.value());
        matches.put(id, match);
        return id;
    }

    @Override
    public synchronized void updateScore(MatchId matchId, int homeScore, int awayScore) {
        Match match = requireMatch(matchId);
        Score newScore = new Score(homeScore, awayScore);
        Score current = match.score();
        if (newScore.home() < current.home() || newScore.away() < current.away()) {
            throw new IllegalScoreException(current, newScore);
        }
        matches.put(matchId, match.withScore(newScore));
    }

    @Override
    public synchronized void finishMatch(MatchId matchId) {
        requireMatch(matchId);
        matches.remove(matchId);
    }

    @Override
    public synchronized List<Match> getSummary() {
        List<Match> summary = new ArrayList<>(matches.values());
        summary.sort(
                Comparator.comparingLong(Match::totalScore)
                        .reversed()
                        .thenComparing(Comparator.comparingLong(Match::startOrder).reversed()));
        return summary;
    }

    @Override
    public synchronized Optional<Match> getMatch(MatchId matchId) {
        return Optional.ofNullable(matches.get(matchId));
    }

    private Match requireMatch(MatchId matchId) {
        Objects.requireNonNull(matchId, "matchId must not be null");
        Match match = matches.get(matchId);
        if (match == null) {
            throw new MatchNotFoundException(matchId);
        }
        return match;
    }
}
