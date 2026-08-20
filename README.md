# Live Football World Cup Scoreboard

A small, in-memory Java library for tracking football matches that are currently in progress:
start a match, update its score, finish it, and get an ordered summary of everything still live.

## Requirements

- Java 25
- Maven 3.8+

## Building and testing

```bash
mvn verify
```

Runs the full test suite (including a dedicated concurrency test, see [AI.md](AI.md) for context)
plus a code-formatting check (`mvn spotless:apply` fixes violations locally). A coverage report
is written to `target/site/jacoco/index.html`.

## Try it

A runnable, dependency-free walkthrough of the exact example scenario from the exercise brief:

```bash
mvn -q compile
java -cp target/classes com.sportradar.fwcscoreboard.example.ScoreBoardDemo
```

```
Summary (expected: Uruguay, Spain, Mexico, Argentina, Germany):
1. Uruguay 6 - 6 Italy
2. Spain 10 - 2 Brazil
3. Mexico 0 - 5 Canada
4. Argentina 3 - 1 Australia
5. Germany 2 - 2 France
```

## Usage

```java
ScoreBoard board = new InMemoryScoreBoard();

MatchId mexicoCanada = board.startMatch("Mexico", "Canada");
board.updateScore(mexicoCanada, 0, 5);

List<Match> summary = board.getSummary(); // ordered per the rules below

board.finishMatch(mexicoCanada);
```

## API

| Operation | Method |
|---|---|
| Start a match | `startMatch(String homeTeam, String awayTeam) -> MatchId` |
| Update the score | `updateScore(MatchId, int homeScore, int awayScore)` |
| Finish a match | `finishMatch(MatchId)` |
| Get an ordered summary | `getSummary() -> List<Match>` |
| **Get a single match (added feature)** | `getMatch(MatchId) -> Optional<Match>` |

## Assumptions

The brief deliberately leaves several things open. Here's what I assumed, and why:

- **`updateScore` takes the absolute score, not a delta.** The worked example in the brief
  ("Mexico 0 – Canada 5") gives final scores directly, and an absolute set is unambiguous —
  callers never need to know the prior score to make a correct call. An increment-based API
  (`homeScored()` / `awayScored()`) was the alternative; I rejected it because it pushes more
  state-tracking responsibility onto the caller for no real benefit at this scale.
- **A team can't be in two in-progress matches at once.** Not stated in the brief, but a team
  can't physically play two matches simultaneously, and allowing it would let the board reach a
  state with no real-world counterpart. Enforced in `startMatch` via `TeamAlreadyPlayingException`.
- **Scores can't decrease.** Football scores are monotonic during a match; a caller passing a
  lower score than what's on record is far more likely to be a bug (stale data, swapped
  arguments) than a legitimate correction. Enforced via `IllegalScoreException`. I treated actual
  corrections (e.g. a wrongly awarded goal) as out of scope — a real system would need a distinct,
  audited "correction" operation rather than silently accepting a lower score through the normal
  update path.
- **No draws/extra-time/penalties modeling, no match clock, no persistence.** The brief asks for
  a scoreboard of scores and match state, not a full match-event or timing model. `startOrder` is
  a logical sequence counter, not a wall-clock timestamp — it's what "most recently started" is
  defined by, and it keeps ordering deterministic and easy to test regardless of how fast calls
  happen.
- **Finished matches are removed outright**, not kept around in a "finished" state. The brief only
  asks for a summary of matches *in progress*; nothing calls for querying finished matches, so I
  didn't build storage for them. This is the biggest thing I'd revisit first if requirements grew
  (see Trade-offs).
- **The library is thread-safe.** Nothing in the brief says the board is single-threaded, and a
  library with unknown callers shouldn't quietly assume it. `InMemoryScoreBoard` synchronizes its
  public methods.
- **Team names are just non-blank strings.** No validation against a real list of national teams,
  no normalization (case, whitespace beyond trimming blank-checks). Out of scope for "simple
  library."

## Trade-offs

- **Coarse-grained locking (`synchronized` per method) over finer-grained concurrency control.**
  Simple to reason about and verify correct; the expected scale (a handful of simultaneous
  matches, not thousands) means it's very unlikely to be a real bottleneck. A `ReadWriteLock` or
  a concurrent collection would add complexity for a throughput problem this library doesn't have.
- **`getSummary()` sorts on every call rather than maintaining a pre-sorted structure.** O(n log n)
  per call instead of O(1), but `n` is small and this avoids the bug surface of keeping a separate
  sorted index in sync with every mutation. I'd revisit this only if summaries were called far
  more often than matches change.
- **`MatchId` is a public record wrapping a `long`,** not a fully opaque type (e.g. hiding the
  constructor behind a factory). Simpler and still communicates "treat this as an opaque handle"
  via Javadoc, at the cost of not *enforcing* that a caller can't construct one directly.
- **Unchecked exceptions, not a `Result`/`Either` type or checked exceptions.** Keeps the API
  ergonomic for callers who expect the common case to succeed, consistent with how the JDK
  collections and most Java libraries signal "not found" / "invalid argument" failures. The
  trade-off is that callers must know to catch specific exception types rather than the compiler
  forcing them to.
- **No dedicated persistence/removal of finished matches.** As noted above, finished matches
  simply vanish. If a future requirement needed match history, that's a different data model
  (append-only log of state transitions) rather than a small extension of this one — worth
  flagging rather than half-building.

## The added feature: `getMatch(MatchId)`

I added a direct, single-match lookup by id, returning `Optional<Match>`.

**Why this one:** the four required operations describe a workflow that ends at `getSummary()` —
but a real consumer (a UI polling for updates on a match it's already showing, or another service
that only cares about one fixture) shouldn't have to re-scan and filter the whole summary list to
get the current state of a match it already knows about. `getMatch` is the natural, minimal
addition that completes the CRUD-like shape of the API without expanding its scope.

It's also a deliberate contrast in error-handling style: every other operation throws when given
an unknown `MatchId`, because for those operations an unknown id is a caller error. For a lookup,
"this match isn't live" (never existed, or already finished) is a normal, expected outcome rather
than exceptional — hence `Optional` instead of an exception. Documenting that distinction in one
place (`ScoreBoard`'s Javadoc) also makes the pattern explicit for anyone extending the API later.

See [AI.md](AI.md) for how AI tools were used while building this.
