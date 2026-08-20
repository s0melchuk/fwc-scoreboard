package com.sportradar.fwcscoreboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sportradar.fwcscoreboard.exception.IllegalScoreException;
import com.sportradar.fwcscoreboard.exception.MatchNotFoundException;
import com.sportradar.fwcscoreboard.exception.TeamAlreadyPlayingException;
import com.sportradar.fwcscoreboard.model.Match;
import com.sportradar.fwcscoreboard.model.MatchId;
import java.util.List;
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
            Match match = summary.get(0);
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

            Match match = board.getSummary().get(0);
            assertThat(match.score().home()).isZero();
            assertThat(match.score().away()).isEqualTo(5);
        }

        @Test
        void rejectsUnknownMatch() {
            assertThatThrownBy(() -> board.updateScore(new MatchId(999), 1, 0))
                    .isInstanceOf(MatchNotFoundException.class);
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
        void rejectsUnknownMatch() {
            assertThatThrownBy(() -> board.finishMatch(new MatchId(999)))
                    .isInstanceOf(MatchNotFoundException.class);
        }

        @Test
        void rejectsFinishingTheSameMatchTwice() {
            MatchId id = board.startMatch("Mexico", "Canada");
            board.finishMatch(id);

            assertThatThrownBy(() -> board.finishMatch(id))
                    .isInstanceOf(MatchNotFoundException.class);
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
