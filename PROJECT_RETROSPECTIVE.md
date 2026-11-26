# CA1 development retrospective
**Lab Equipment Booking Manager · 30 October–26 November 2026**

## Method and evidence

This retrospective was prepared after development. The timeline uses local Git commits and reflogs for changes and branch creation, GitHub PR descriptions and merge records for integration, and GitHub Actions for CI outcomes. Commit times mark when changes were recorded. All times below use America/Chicago (UTC−06:00 on 26 November).

The Kotlin sources, Gradle configuration and saved test reports were checked against `PROJECT_LOG.md`, `README.md`, `REPORT.md` and `evidence/assessment/ACCEPTANCE.md` in the submission archive. The formal disclosure is in [REPORT.md](REPORT.md).

## 30 October–25 November

| Period | Evidence found |
|---|---|
| 30 October–5 November | No contemporaneous CA1 records were found in the inspected repository and archive. |
| 6 November–12 November | No contemporaneous CA1 records were found in the inspected repository and archive. |
| 13 November–19 November | No contemporaneous CA1 records were found in the inspected repository and archive. |
| 20 November–25 November | No contemporaneous CA1 records were found in the inspected repository and archive. |

The available evidence cannot establish what work, if any, took place outside those records.

## 26 November

| Commit or tag time | Milestone and Git evidence | Verification or integration |
|---|---|---|
| 01:58:59 | `78f0bee` — “chore: bootstrap pinned CA1 toolchain and document prerequisites” | Gradle wrapper JAR checksum verified. The initial distribution download was incomplete. |
| 02:20:57 | `dbe3028` — “build: verify the Gradle baseline on JDK 17” | Windows clean build passed in 3m 39s. Tests and Detekt reported `NO-SOURCE`; JaCoCo was skipped. |
| 03:23:07 | `934a950` — `feature/equipment-catalog`: add, find, list, search and delete equipment | 7 tests passed. [PR #1](https://github.com/yinghuayaya/CA_LabEquipmentBooking/pull/1) merged at 03:25:09 as `8e6700f`. |
| 03:27:03 | `d46f892` — `feature/booking-rules`: prevent conflicts and retain cancelled history | 15 cumulative tests passed. [PR #2](https://github.com/yinghuayaya/CA_LabEquipmentBooking/pull/2) merged at 03:28:15 as `076bc86`. |
| 03:30:25 | `a20bd50` — `feature/json-persistence`: save and restore the catalog, history and ID sequence | Six storage tests covered restart and failure cases. [PR #3](https://github.com/yinghuayaya/CA_LabEquipmentBooking/pull/3) merged at 03:31:27 as `1793d31`. |
| 03:34:24 | `750ad8f` — `feature/cli-testing-analysis`: expose the domain operations through validated CLI commands | Clean build and `installDist` passed with 48 tests. [PR #4](https://github.com/yinghuayaya/CA_LabEquipmentBooking/pull/4) merged at 03:35:47 as `c1faeeb`. |
| 03:47:16 | `4d2a4be` — `feature/documentation`: document the commands, measured results and demonstration outline | README examples ran in separate processes. Both pages of the PDF export were inspected. [PR #5](https://github.com/yinghuayaya/CA_LabEquipmentBooking/pull/5) merged at 03:48:36 as `83f639c`. |
| 03:51:08 | Annotated assessment tag [`ca1-v1.0.0`](https://github.com/yinghuayaya/CA_LabEquipmentBooking/tree/ca1-v1.0.0) created for `83f639c` | The acceptance record documents publication. GitHub’s tag resolves to the verified commit. The tagger timestamp gives the creation time only. |

## Technical progress, problems and solutions

**Build and test environment.** The Kotlin DSL build pinned Kotlin 2.1.21 and Gradle 8.12.1 and configured JUnit 5, JaCoCo and Detekt. Windows initially used Java 26. A truncated Temurin archive failed its checksum, so the download was resumed and verified before installing JDK 17. The Gradle distribution was also checked against its pinned digest. Maven Central was placed first for plugin resolution after a slow download. The first source-backed build then exposed a separate problem: JUnit could not discover the compiled test class under the Chinese project path. Using the native JDK 17 encoding in the Windows launcher restored discovery. Catalog naming and line-length findings were corrected in source.

**Catalog and reservations.** An equipment ID and a booking ID each have their own `HashMap` lookup. Duplicate equipment IDs preserve the original record. Each item has a `TreeMap` of active booking starts. Because the schedule cannot contain overlaps, `floorEntry` and `ceilingEntry` are sufficient to check the two neighbors. For example, a 09:00–11:00 booking leaves the item free at 11:00. This is the half-open interval rule. Cancellation frees that slot and retains the record. Equipment with booking history cannot be deleted. Tests exercised both neighbors, adjacency, rebooking and rejected requests that leave the ID sequence unchanged.

**Storage and CLI.** JSON serialization stores equipment, booking history and the next ID. Restoration rebuilds the active schedule using the insertion rules. `FileChannel` locking on a sibling file covers the read–modify–write operation. A temporary file enables atomic file replacement where supported, with ordinary replacement as the fallback. Temporary-directory tests showed that malformed data, conflicting bookings and stale sequences were rejected without overwriting the database. The CLI parser validates commands, options, dates and filters before file access. Errors reach stderr with exit code 1. Two long CLI lines flagged by Detekt were wrapped without suppressions.

## Final verification and evidence gaps

The four feature PRs and the documentation PR passed their checks. All five subsequent pushes to `main` passed the Ubuntu JDK 17 GitHub Actions workflow. The acceptance record also documents a fresh Windows clone using the local Gradle dependency cache. The README session ended with one equipment item, one cancelled booking and next sequence 2. [Original assessment CI run](https://github.com/yinghuayaya/CA_LabEquipmentBooking/actions/runs/45795416026)

The saved JUnit XML reports show **48 passed, 0 failed, 0 errors and 0 skipped**: 7 catalog, 8 booking rules, 6 storage, 23 parser and 4 CLI tests. JaCoCo recorded **227/231 lines (98.3%)** and **165/191 branches (86.4%)** covered. The Detekt XML contains **zero findings**. These are the saved development results; no new build was run for this retrospective.

Two documentation PRs followed the original assessment. [PR #6](https://github.com/yinghuayaya/CA_LabEquipmentBooking/pull/6) passed its [CI check](https://github.com/yinghuayaya/CA_LabEquipmentBooking/actions/runs/45838969683) and merged as `7aebd0e`. The original annotated tags remain at `83f639c` (`ca1-v1.0.0`) and `7aebd0e` (`ca1-v1.0.1`). For the final wording review, see [PR #7 and its check result](https://github.com/yinghuayaya/CA_LabEquipmentBooking/pull/7/checks). The [final main commit](https://github.com/yinghuayaya/CA_LabEquipmentBooking/commit/ca1-v1.0.2) is identified by `ca1-v1.0.2`. The packaged acceptance record lists the full SHA, merge time and post-merge CI run.

Earlier activity would require genuine dated student evidence, such as an original design note or a dated prototype with identifiable provenance. The records contain a demonstration outline. Confirming a delivered demonstration or a build on the designated laboratory machine would require separate evidence.

## Reflection and peer review

The first clean build succeeded before there was any test code. Only the catalog tests exposed the Windows classpath problem. That distinction matters when reading a build log: a successful task with no sources gives different evidence from seven tests that actually ran. The restart tests checked something the interval tests alone could miss: a cancelled reservation must still be visible after reload without blocking its old slot.

All five PRs covered by this retrospective passed CI. GitHub shows no submitted reviews, conversation comments or inline review comments for those PRs. The following checklist is for a student who independently reviews the assessed tag.

- [ ] Trace a 09:00–11:00 reservation and check the outcomes for 10:59 and 11:00 starts.
- [ ] Cancel and rebook the slot, then reopen the database and check both records.
- [ ] Use a temporary data file to check the error message and file contents after invalid JSON.
- [ ] Record the commit reviewed, the commands actually run and any findings in the reviewer’s own words.
