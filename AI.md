# AI Usage

## Summary

This solution was built with Claude Code (Anthropic's CLI agent), acting as a pair-programmer:
reading the task PDF, proposing a design, implementing it, and writing tests, docs and commits.
I reviewed and directed each step rather than accepting a single one-shot generation — the plan
was discussed and adjusted (target Java version, choice of the additional feature) before any
code was written, and the resulting design decisions and their rationale are mine, documented in
[README.md](README.md).

## How AI was used, step by step

1. **Requirements extraction.** Gave Claude the task PDF (`ODDS and Data - JAVA Coding Task.pdf`)
   and asked it to read the requirements.
2. **Planning.** Asked Claude to propose an implementation plan before writing any code. It
   proposed a domain model (`Team`, `Score`, `Match`, `MatchId`), a `ScoreBoard` interface with an
   `InMemoryScoreBoard` implementation, validation/thread-safety choices, and a commit structure.
   Two decisions were surfaced as explicit questions rather than assumed silently:
   - Which feature to add as the required 5th operation (chose: a single-match lookup,
     `getMatch`, over alternatives like a "one team, one match" validation rule or a
     human-readable formatted summary).
   - Target Java version (chose: match the local JDK, 25).
3. **Implementation.** Claude wrote the Maven project, domain records, the `ScoreBoard` interface,
   `InMemoryScoreBoard`, custom exceptions, and JUnit 5 + AssertJ tests (including the exact
   example scenario from the brief), compiling and running the test suite after each stage to
   verify correctness before committing.
4. **The additional feature (`getMatch`)** was implemented and committed separately from the core
   four operations, as the brief requires a distinct commit for it.
5. **Documentation.** Claude drafted `README.md` (assumptions, reasoning, trade-offs, and the
   added-feature writeup) and this file, from the actual decisions made during the session above.

## Prompt history (condensed)

1. *"@[task PDF] Now to the actual coding challenge - here are the requirements I have received -
   can we plan an implementation?"*
2. Claude read the PDF and proposed a design; asked two clarifying questions (extra feature choice,
   Java version) via a multiple-choice prompt.
3. *"Get match by ID / lookup"* and *"Match local JDK (25)"* — my answers to those two questions.
4. Claude presented the full plan (domain model, API, decisions to document, testing approach,
   commit shape) and asked for confirmation or adjustments.
5. *"Sounds good - You can start. Please follow the instructions from task PDF (especially point
   5)"* — approval to proceed, with an explicit reminder to satisfy the brief's requirement that
   the additional operation get its own distinct commit.
6. Claude implemented the project across three commits (skeleton, core operations, the `getMatch`
   feature), running `mvn test` after each stage, then wrote `README.md` and this file.

## Artifacts that guided the implementation

- The task brief itself: `ODDS and Data - JAVA Coding Task.pdf` (provided by the user), in
  particular the exact example scenario, which is encoded verbatim as a test case
  (`InMemoryScoreBoardTest.Summary#ordersByTotalScoreDescendingThenMostRecentlyStartedFirst`).
- No other external code samples, templates, or repositories were used as a reference.
