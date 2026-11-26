# Lab Equipment Booking Manager

Use this Kotlin CLI to add and find lab equipment, reserve a time slot, and cancel a booking. Each command reads the same JSON file, so the next time the program runs it can show both active and cancelled bookings.

## Build and run

Set `JAVA_HOME` to JDK 17. The first build requires network access. The wrapper pins Gradle 8.12.1. The build pins Kotlin 2.1.21, kotlinx.serialization JSON 1.8.1, JUnit Jupiter 5.12.2, JaCoCo 0.8.13 and Detekt 1.23.8.

Windows PowerShell, from the repository root:

```powershell
.\gradlew.bat clean build installDist
.\gradlew.bat run --args="help"
$env:Path = "$PWD\build\install\lab-booking\bin;$env:Path"
lab-booking --help
```

Linux:

```sh
./gradlew clean build installDist
./gradlew run --args="help"
export PATH="$PWD/build/install/lab-booking/bin:$PATH"
lab-booking --help
```

## Commands

Try this session with an empty database. IDs are case-sensitive. Search matches a substring in the ID, name, category or location, ignoring case.

```text
lab-booking equipment add --id EQ-001 --name "Projector" --category AV --location "Lab A"
lab-booking equipment list
lab-booking equipment show --id EQ-001
lab-booking equipment search --query project
lab-booking booking create --equipment EQ-001 --requester "Yao Yi" --start 2026-12-01T09:00 --end 2026-12-01T11:00 --purpose "Project work"
lab-booking booking list
lab-booking booking list --equipment EQ-001 --status ACTIVE
lab-booking booking cancel --id BK-0001
lab-booking booking list --status CANCELLED
lab-booking equipment add --id EQ-002 --name Lens --category Optics --location "Lab B"
lab-booking equipment delete --id EQ-002
lab-booking --data-file "demo/bookings.json" equipment list
lab-booking help
```

Both booking filters are optional. Use ISO local times. A 09:00–11:00 booking permits another booking starting at 11:00. Only active bookings can be cancelled. Equipment with any booking history must remain in the catalog. Invalid input and storage errors produce a diagnostic on stderr and exit code 1.

## Project structure

| Location | Purpose |
|---|---|
| `src/main/kotlin/com/yi/labbooking/` | CLI, booking rules and JSON storage |
| `src/test/kotlin/com/yi/labbooking/` | JUnit tests for the catalog, reservations, storage and CLI |
| `gradle/wrapper/`, `gradlew`, `gradlew.bat` | Run the pinned Gradle version |
| `build.gradle.kts`, `settings.gradle.kts` | Configure the application, dependencies and checks |
| `.github/workflows/verify.yml` | Run the JDK 17 build on Ubuntu |

## Architecture and storage

Under `com.yi.labbooking`, `Main.kt` forwards arguments and the process exit code. `CommandParser.kt` validates commands, required options, dates and filters. `CliApplication.kt` dispatches commands and formats output. `Equipment.kt` and `Models.kt` define the catalog and reservation records.

`BookingService.kt` uses separate HashMaps for average constant-time equipment and booking lookup by ID. Each equipment item has a TreeMap from active start time to booking ID. Because active slots never overlap, only the predecessor from `floorEntry` and the successor from `ceilingEntry` can conflict with a proposed slot. These lookups cost O(log n) for n active bookings of that item. Cancellation removes the start time from the schedule and keeps the booking in history.

`BookingStore.kt` initializes `data/bookings.json` relative to the working directory. Override it with `--data-file PATH`. `StorageDtos.kt` records equipment, history and the next ID sequence. Loading validates records and rebuilds the schedule. A sibling lock coordinates CLI processes. Successful changes replace the file from a temporary file in the same directory, using atomic replacement where supported. Invalid JSON or inconsistent records stop the command before a save. The whole snapshot is loaded per invocation, which suits a small laboratory inventory.

## Testing and analysis

Run the wrapper tasks `test`, `jacocoTestReport` and `detekt` individually, or use `clean build`. Tests finalize with the coverage report; `check` includes static analysis. The Windows JDK 17 clean build on 26 November 2026 produced these results.

| Check | Result |
|---|---|
| JUnit 5 | 48 passed, 0 failed or skipped |
| JaCoCo lines | 227 / 231 (98.3%) |
| JaCoCo branches | 165 / 191 (86.4%) |
| Detekt | 0 findings; no baseline or suppressions |

Open `build/reports/tests/test/index.html`, `build/reports/jacoco/test/html/index.html` and `build/reports/detekt/detekt.html` after building. Tests cover catalog operations, booking rules, parsing, command execution and storage. Persistence tests use temporary directories.

The submission archive includes preserved JUnit, JaCoCo and Detekt reports under `evidence/`, plus the assessment PDF and acceptance record under `evidence/assessment/`. To view reports from a GitHub checkout, run the build and open the `build/reports/` paths above.

## Workflow and reflection

Each branch starts from the latest merged main. After local tests and analysis, its PR targets main and runs an independent Ubuntu JDK 17 build. The implementation history is split into [equipment](https://github.com/yinghuayaya/CA_LabEquipmentBooking/pull/1), [reservations](https://github.com/yinghuayaya/CA_LabEquipmentBooking/pull/2), [persistence](https://github.com/yinghuayaya/CA_LabEquipmentBooking/pull/3) and [CLI](https://github.com/yinghuayaya/CA_LabEquipmentBooking/pull/4).

The empty bootstrap built successfully, but the first real test exposed a Windows JDK 17 worker classpath encoding problem. Using the native encoding in the Windows launcher let JUnit run from the Chinese project path. Boundary tests made the adjacency rule executable, and restart tests checked that the in-memory schedule could be rebuilt from disk. [Development record](PROJECT_LOG.md) · [Assessment report](REPORT.md)

## References

The supplied *Software Development Tools Continuous Assessment Brief 2026–2027* defines CA1. References include [Kotlin Gradle compatibility](https://kotlinlang.org/docs/gradle-configure-project.html), [Gradle Wrapper](https://docs.gradle.org/8.12.1/userguide/gradle_wrapper.html), [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization), [JUnit 5](https://docs.junit.org/5.12.2/user-guide/), [JaCoCo with Gradle](https://docs.gradle.org/8.12.1/userguide/jacoco_plugin.html) and [Detekt compatibility](https://detekt.dev/docs/introduction/compatibility/).

The [assessment report](REPORT.md) includes the academic disclosure.
