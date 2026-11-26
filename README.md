# Lab Equipment Booking Manager

An individual Kotlin command-line project for Software Development Tools CA1 (2026–2027).

## Current state

This is the local toolchain bootstrap, not a completed application. Equipment management, reservations, JSON persistence, command parsing, and tests are pending. No GitHub remote or pull requests exist. Feature implementation will start after authorization of a suitable new repository so that development and review history remain genuine.

On 2026-11-26, the committed wrapper launched Gradle 8.12.1 on Windows 11 with Temurin 17.0.21.1+2. The bootstrap passed `clean build`. Gradle reported `NO-SOURCE` for tests and Detekt, and skipped JaCoCo because the source sets were empty. Feature verification begins with the equipment catalog.

## Development environment

Windows 11 or Linux, JDK 17, Git, and network access for the first Gradle build. Versions are pinned: Gradle 8.12.1, Kotlin 2.1.21, kotlinx.serialization JSON 1.8.1, JUnit Jupiter 5.12.2 (Platform 1.12.2), Detekt 1.23.8, and JaCoCo 0.8.13.

Set `JAVA_HOME` to a JDK 17 installation and put its `bin` directory on `PATH`. The repository does not store machine-specific JDK paths.

Windows PowerShell:

```powershell
.\gradlew.bat clean build
.\gradlew.bat test
.\gradlew.bat jacocoTestReport
.\gradlew.bat detekt
```

Linux:

```sh
./gradlew clean build
./gradlew test jacocoTestReport detekt
```

The build runs JUnit 5 tests when present, schedules JaCoCo after tests, and includes Detekt in `check`. Coverage will be written to `build/reports/jacoco/test/html/index.html` once tests exist. No coverage percentage is claimed for an empty baseline. No Detekt baseline or suppressions are configured.

## Planned application and architecture

Package: `com.yi.labbooking`. `Main.kt` will be the entry point; `Models.kt` domain values; `CommandParser.kt` independently testable parsing; `CliApplication.kt` command execution/output; `BookingService.kt` domain rules; `BookingStore.kt` file operations; `StorageDtos.kt` JSON models.

Equipment commands: `equipment add`, `equipment list`, `equipment show`. Booking commands: `booking create`, `booking list` (equipment/status filters), `booking cancel`. Help: `help`, `--help`. A global `--data-file` will default to `data/bookings.json`. These commands are planned and cannot run yet.

HashMaps will provide equipment and booking lookup by ID. Each equipment's active schedule will use a TreeMap keyed by start time; `floorEntry()` and `ceilingEntry()` will detect conflicts in logarithmic time under the non-overlap invariant. Half-open intervals allow adjacent bookings. Cancelled records will remain in history but leave the active schedule.

JSON storage will retain equipment, complete booking history, and the ID sequence. Loading will validate stored invariants, and corrupt data must fail explicitly without being replaced. Saves will use temporary files and atomic replacement where supported. Generated data and build output are ignored by Git.

## Branching model and next stage

`main` holds the verified bootstrap and subsequently accepted changes. Create each feature branch from the latest accepted `main`, make meaningful commits while working, run relevant checks, open a genuine GitHub PR, inspect its diff and results, and merge it before starting the next dependent branch. Never manufacture approvals, discussions, or history. At least four feature PRs are required.

1. `feature/equipment-catalog`: models, equipment validation and catalog tests.
2. `feature/booking-rules`: schedules, conflicts, cancellation and domain tests.
3. `feature/json-persistence`: validated JSON loading, safe saves and restart tests.
4. `feature/cli-testing-analysis`: parser, execution, remaining tests, coverage and analysis.
5. `feature/documentation`: complete command examples, verified results, reflection and demonstration guide.

No feature branches have been presented as completed. Repository creation and publication require the owner's confirmation. The remote must be a new suitable project, never the calculator repository.

## Reflection and assessment evidence

The environment check identified Java 26 before invoking a Gradle release that should run on JDK 17 for this project. Pinning the toolchain and checking distribution hashes makes the baseline reproducible. The missing remote was identified before feature work, preserving the opportunity to demonstrate actual incremental PR development. Test and analysis reflections will be extended with real findings as implementation proceeds.

The official brief additionally requires a ten-minute live demonstration, a PDF report exported from Markdown, the repository URL, and the assessed tag/release. These remain pending, as does verification on a laboratory machine. See `PROJECT_LOG.md` for actual progress.

## References and assistance disclosure

- [Kotlin Gradle compatibility](https://kotlinlang.org/docs/gradle-configure-project.html)
- [Detekt compatibility](https://detekt.dev/docs/introduction/compatibility/)
- [Gradle Wrapper](https://docs.gradle.org/8.12.1/userguide/gradle_wrapper.html)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
- [JUnit 5.12.2 guide](https://docs.junit.org/5.12.2/user-guide/)
- [JaCoCo Gradle plugin](https://docs.gradle.org/8.12.1/userguide/jacoco_plugin.html)
- Assessment authority: the supplied `CA.docx`, Software Development Tools Continuous Assessment Brief 2026–2027.

OpenAI Codex assisted with environment inspection, build configuration, and documentation. This is assisted work, not evidence of unaided student authorship. The student must review and understand all submitted code and maintain this disclosure as work continues.
