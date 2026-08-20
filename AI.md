# AI Usage

## Summary

This solution was built with Claude Code (Anthropic's CLI agent), acting as a pair-programmer
across several sessions: reading the task PDF, proposing a design, implementing it, responding to
PR review comments, and hardening the repository's build/test setup. I reviewed and directed each
step rather than accepting one-shot generation — plans and open design questions were discussed
before code was written, and the resulting decisions and their rationale are mine, documented in
[README.md](README.md). Below is a per-session summary rather than a full prompt transcript, since
a verbatim log would grow unboundedly across sessions without adding much beyond what's already in
this file and the commit history itself.

## Sessions

1. **Initial implementation.** Gave Claude the task PDF and asked it to plan an implementation
   before writing code. Two decisions were surfaced as explicit questions rather than assumed
   silently: which feature to add as the required 5th operation (chose a single-match lookup,
   `getMatch`), and the target Java version (chose to match the local JDK, 25). Claude then built
   the Maven project, domain model (`Team`, `Score`, `Match`, `MatchId`), the `ScoreBoard`
   interface with its `InMemoryScoreBoard` implementation, JUnit 5 + AssertJ tests (including the
   brief's exact example scenario), and `README.md`/`AI.md`, across commits matching the brief's
   requirement that the added feature land in its own distinct commit. I confirmed a feature-branch
   workflow afterward — commits had gone directly to `main` — so Claude split the work onto
   `feature/live-scoreboard` and reset `main` back to its prior state.
2. **PR review response.** I pasted three review comments (a Javadoc/exception mismatch on
   negative-score handling, `MatchId`'s "opaque" Javadoc overstating what a public record actually
   enforces, and tests constructing arbitrary `MatchId` values). Claude fixed the Javadoc to match
   actual exception types, softened the `MatchId` wording to describe convention rather than an
   enforced constraint, and rewrote the affected tests to derive an unknown id via the real API
   (start then finish) instead of `new MatchId(999)`, deduplicating tests that became redundant
   as a result.
3. **Repository setup.** Asked what else would be expected for a senior-level submission; Claude
   suggested a prioritized list (CI, null/boundary tests, a concurrency test, formatting/static
   analysis tooling, coverage reporting, a runnable example) and, on approval, implemented it on
   a new feature branch: explicit null handling plus a fix for an `int`-overflow bug in score
   totals it surfaced while adding boundary tests, a concurrency test exercising the board's
   thread-safety claim under real contention, `package-info.java` for each package, Spotless
   formatting wired into `mvn verify` (after `google-java-format` and `palantir-java-format` both
   crashed against JDK 25's javac internals — worked around by switching to the Eclipse formatter),
   JaCoCo coverage reporting, a GitHub Actions CI workflow, and a runnable demo reproducing the
   brief's example scenario. SpotBugs was evaluated for static analysis but dropped: its bundled
   class reader doesn't yet support JDK 25 bytecode.

## Artifacts that guided the implementation

- The task brief itself: `ODDS and Data - JAVA Coding Task.pdf` (provided by the user), in
  particular the exact example scenario, which is encoded verbatim as a test case
  (`InMemoryScoreBoardTest.Summary#ordersByTotalScoreDescendingThenMostRecentlyStartedFirst`) and
  as the runnable demo (`ScoreBoardDemo`).
- Three PR review comments (provided by the user, pasted from the actual review) drove the fixes
  described in Session 2 above.
- No other external code samples, templates, or repositories were used as a reference.
