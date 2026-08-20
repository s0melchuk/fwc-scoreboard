package com.sportradar.fwcscoreboard.example;

import com.sportradar.fwcscoreboard.InMemoryScoreBoard;
import com.sportradar.fwcscoreboard.ScoreBoard;
import com.sportradar.fwcscoreboard.model.Match;
import com.sportradar.fwcscoreboard.model.MatchId;

/**
 * A runnable, no-dependencies walkthrough of the exact example scenario from the exercise brief:
 * five matches are started and scored, and the resulting summary is printed in order.
 *
 * <p>
 * Not part of the library's public API &mdash; this exists purely so the behaviour described in
 * README.md can be seen running rather than taken on faith from reading test code. Run it with:
 *
 * <pre>{@code
 * mvn -q compile
 * java -cp target/classes com.sportradar.fwcscoreboard.example.ScoreBoardDemo
 * }</pre>
 */
public final class ScoreBoardDemo {

    private ScoreBoardDemo() {
    }

    public static void main(String[] args) {
        ScoreBoard board = new InMemoryScoreBoard();

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

        System.out.println("Summary (expected: Uruguay, Spain, Mexico, Argentina, Germany):");
        int rank = 1;
        for (Match match : board.getSummary()) {
            System.out.printf(
                    "%d. %s %d - %d %s%n",
                    rank++,
                    match.homeTeam().name(),
                    match.score().home(),
                    match.score().away(),
                    match.awayTeam().name());
        }
    }
}
