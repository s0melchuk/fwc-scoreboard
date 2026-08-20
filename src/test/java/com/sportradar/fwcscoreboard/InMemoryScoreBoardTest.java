package com.sportradar.fwcscoreboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sportradar.fwcscoreboard.exception.IllegalScoreException;
import com.sportradar.fwcscoreboard.exception.MatchNotFoundException;
import com.sportradar.fwcscoreboard.exception.TeamAlreadyPlayingException;
import com.sportradar.fwcscoreboard.model.Match;
import com.sportradar.fwcscoreboard.model.MatchId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InMemoryScoreBoardTest {

    private InMemoryScoreBoard board;

    @BeforeEach
    void setUp() {
        board = new InMemoryScoreBoard();
    }

    @Nested
    class StartMatch {

        @Test
        void startsWithZeroZeroScore() {
            MatchId id = board.startMatch("Mexico", "Canada");

            List<Match> summary = board.getSummary();
            assertThat(summary).hasSize(1);
            Match match = summary.getFirst();
            assertThat(match.id()).isEqualTo(id);
            assertThat(match.homeTeam().name()).isEqualTo("Mexico");
            assertThat(match.awayTeam().name()).isEqualTo("Canada");
            assertThat(match.score().home()).isZero();
            assertThat(match.score().away()).isZero();
        }

        @Test
        void rejectsBlankTeamNames() {
            assertThatThrownBy(() -> board.startMatch(" ", "Canada"))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> board.startMatch("Mexico", ""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsNullTeamNames() {
            assertThatThrownBy(() -> board.startMatch(null, "Canada"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> board.startMatch("Mexico", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void rejectsATeamPlayingItself() {
            assertThatThrownBy(() -> board.startMatch("Mexico", "Mexico"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsATeamAlreadyInAnotherInProgressMatch() {
            board.startMatch("Mexico", "Canada");

            assertThatThrownBy(() -> board.startMatch("Mexico", "Spain"))
                    .isInstanceOf(TeamAlreadyPlayingException.class);
            assertThatThrownBy(() -> board.startMatch("Spain", "Canada"))
                    .isInstanceOf(TeamAlreadyPlayingException.class);
        }

        @Test
        void allowsATeamToStartAgainAfterItsMatchFinishes() {
            MatchId id = board.startMatch("Mexico", "Canada");
            board.finishMatch(id);

            assertThat(board.startMatch("Mexico", "Canada")).isNotNull();
        }
    }

    @Nested
    class UpdateScore {

        @Test
        void replacesTheScoreAbsolutely() {
            MatchId id = board.startMatch("Mexico", "Canada");

            board.updateScore(id, 0, 5);

            Match match = board.getSummary().getFirst();
            assertThat(match.score().home()).isZero();
            assertThat(match.score().away()).isEqualTo(5);
        }

        @Test
        void rejectsUnknownMatch() {
            MatchId finishedId = board.startMatch("Mexico", "Canada");
            board.finishMatch(finishedId);

            assertThatThrownBy(() -> board.updateScore(finishedId, 1, 0))
                    .isInstanceOf(MatchNotFoundException.class);
        }

        @Test
        void rejectsNullMatchId() {
            assertThatThrownBy(() -> board.updateScore(null, 1, 0))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void rejectsNegativeScores() {
            MatchId id = board.startMatch("Mexico", "Canada");

            assertThatThrownBy(() -> board.updateScore(id, -1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsAScoreThatDecreases() {
            MatchId id = board.startMatch("Mexico", "Canada");
            board.updateScore(id, 2, 1);

            assertThatThrownBy(() -> board.updateScore(id, 1, 1))
                    .isInstanceOf(IllegalScoreException.class);
        }

        @Test
        void acceptsLargeScoresAtTheIntegerBoundary() {
            MatchId id = board.startMatch("Mexico", "Canada");

            board.updateScore(id, Integer.MAX_VALUE, Integer.MAX_VALUE);

            Match match = board.getMatch(id).orElseThrow();
            assertThat(match.score().home()).isEqualTo(Integer.MAX_VALUE);
            assertThat(match.score().away()).isEqualTo(Integer.MAX_VALUE);
        }
    }

    @Nested
    class FinishMatch {

        @Test
        void removesTheMatchFromTheSummary() {
            MatchId id = board.startMatch("Mexico", "Canada");

            board.finishMatch(id);

            assertThat(board.getSummary()).isEmpty();
        }

        @Test
        void rejectsUnknownOrAlreadyFinishedMatch() {
            MatchId id = board.startMatch("Mexico", "Canada");
            board.finishMatch(id);

            assertThatThrownBy(() -> board.finishMatch(id))
                    .isInstanceOf(MatchNotFoundException.class);
        }

        @Test
        void rejectsNullMatchId() {
            assertThatThrownBy(() -> board.finishMatch(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class GetMatch {

        @Test
        void returnsTheCurrentStateOfAnInProgressMatch() {
            MatchId id = board.startMatch("Mexico", "Canada");
            board.updateScore(id, 1, 2);

            Optional<Match> found = board.getMatch(id);

            assertThat(found).isPresent();
            assertThat(found.get().score().home()).isEqualTo(1);
            assertThat(found.get().score().away()).isEqualTo(2);
        }

        @Test
        void returnsEmptyAfterTheMatchHasFinished() {
            MatchId id = board.startMatch("Mexico", "Canada");
            board.finishMatch(id);

            assertThat(board.getMatch(id)).isEmpty();
        }

        @Test
        void returnsEmptyForANullId() {
            assertThat(board.getMatch(null)).isEmpty();
        }
    }

    @Nested
    class Summary {

        @Test
        void isEmptyInitially() {
            assertThat(board.getSummary()).isEmpty();
        }

        @Test
        void ordersByTotalScoreDescendingThenMostRecentlyStartedFirst() {
            MatchId mexicoCanada = board.startMatch("Mexico", "Canada");
            MatchId spainBrazil = board.startMatch("Spain", "Brazil");
            MatchId germanyFrance = board.startMatch("Germany", "France");
            MatchId uruguayItaly = board.startMatch("Uruguay", "Italy");
            MatchId argentinaAustralia = board.startMatch("Argentina", "Australia");

            board.updateScore(mexicoCanada, 0, 5);
            board.updateScore(spainBrazil, 10, 2);
            board.updateScore(germanyFrance, 2, 2);
            board.updateScore(uruguayItaly, 6, 6);
            board.updateScore(argentinaAustralia, 3, 1);

            List<Match> summary = board.getSummary();

            assertThat(summary)
                    .extracting(m -> m.homeTeam().name())
                    .containsExactly("Uruguay", "Spain", "Mexico", "Argentina", "Germany");
        }

        @Test
        void ordersByTotalScoreWithoutIntOverflowAtTheBoundary() {
            MatchId huge = board.startMatch("Mexico", "Canada");
            MatchId small = board.startMatch("Spain", "Brazil");

            // home + away here would overflow a 32-bit int total if summed naively.
            board.updateScore(huge, Integer.MAX_VALUE, Integer.MAX_VALUE);
            board.updateScore(small, 1, 0);

            List<Match> summary = board.getSummary();

            assertThat(summary)
                    .extracting(m -> m.homeTeam().name())
                    .containsExactly("Mexico", "Spain");
        }

        @Test
        void doesNotIncludeFinishedMatches() {
            MatchId id = board.startMatch("Mexico", "Canada");
            board.startMatch("Spain", "Brazil");
            board.finishMatch(id);

            assertThat(board.getSummary())
                    .extracting(m -> m.homeTeam().name())
                    .containsExactly("Spain");
        }
    }
}
