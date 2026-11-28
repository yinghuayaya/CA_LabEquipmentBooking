# 🧰 Laboratory Equipment Booking Manager

Reserve lab equipment from the command line and keep a searchable catalog and booking history in a local JSON file.

[![Verify on main](https://github.com/yinghuayaya/LabEquipmentBooking/actions/workflows/verify.yml/badge.svg?branch=main)](https://github.com/yinghuayaya/LabEquipmentBooking/actions/workflows/verify.yml)
![Kotlin 2.1.21](https://img.shields.io/badge/Kotlin-2.1.21-7356BF)
![Gradle 8.12.1](https://img.shields.io/badge/Gradle-8.12.1-255B60)
![JDK 17](https://img.shields.io/badge/JDK-17-47627A)

## ✨ Features

| Module | What it does |
| --- | --- |
| 🔬 Equipment Management | Add, list, inspect and search equipment. Delete items with no booking history. |
| 📅 Booking Management | Reserve a time slot, reject overlaps and filter active or cancelled bookings. |
| 💾 JSON Persistence | Restore the catalog, booking history and next booking number between runs. |
| 🧪 Testing and Static Analysis | Run JUnit tests, generate JaCoCo coverage and check Kotlin code with Detekt through Gradle. |

---

## 🚀 Build and Run

Install **JDK 17** and set `JAVA_HOME` to its installation directory. Run the commands below from the repository root. The wrapper downloads Gradle 8.12.1 on the first build, which requires network access. `installDist` creates the `lab-booking` launcher.

### Windows PowerShell

```powershell
.\gradlew.bat clean build installDist
.\gradlew.bat run --args="help"
$env:Path = "$PWD\build\install\lab-booking\bin;$env:Path"
lab-booking --help
```

### Linux

```sh
./gradlew clean build installDist
./gradlew run --args="help"
export PATH="$PWD/build/install/lab-booking/bin:$PATH"
lab-booking --help
```

## ⌨️ Commands

Run these examples in order with an empty database. Equipment and booking IDs are case-sensitive.

### Equipment Commands

Search matches a substring in the equipment ID, name, category or location, ignoring case.

```sh
lab-booking equipment add --id EQ-001 --name "Projector" --category AV --location "Lab A"
lab-booking equipment list
lab-booking equipment show --id EQ-001
lab-booking equipment search --query project
lab-booking equipment add --id EQ-002 --name Lens --category Optics --location "Lab B"
lab-booking equipment delete --id EQ-002
```

### Booking Commands

Use ISO local times. Equipment and status filters are optional and can be combined. Only active bookings can be cancelled.

```sh
lab-booking booking create --equipment EQ-001 --requester "Yao Yi" --start 2026-12-01T09:00 --end 2026-12-01T11:00 --purpose "Project work"
lab-booking booking list
lab-booking booking list --equipment EQ-001 --status ACTIVE
lab-booking booking cancel --id BK-0001
lab-booking booking list --status CANCELLED
```

### Storage and Help

Use `--data-file` to select another database for one command. Both `help` and `--help` show usage.

```sh
lab-booking --data-file "demo/bookings.json" equipment list
lab-booking help
```

---

## 🗂 Project Structure

| Location | Purpose |
|---|---|
| `src/main/kotlin/com/yi/labbooking/` | CLI, booking rules and JSON storage |
| `src/test/kotlin/com/yi/labbooking/` | JUnit tests for the catalog, reservations, storage and CLI |
| `gradle/wrapper/`, `gradlew`, `gradlew.bat` | Run the pinned Gradle version |
| `build.gradle.kts`, `settings.gradle.kts` | Configure the application, dependencies and checks |
| `.github/workflows/verify.yml` | Run the JDK 17 build on Ubuntu |

## ⚙️ Architecture and Storage

### CLI and Command Processing

In `com.yi.labbooking`, `Main.kt` passes arguments to `CliApplication.kt` and returns its exit code. `CommandParser.kt` checks commands, required options, dates and filters before dispatch. `CliApplication.kt` formats results and writes input or storage errors to stderr with exit code 1. `Equipment.kt` and `Models.kt` define the domain records.

### Booking Rules and Data Structures

`BookingService.kt` uses separate `HashMap` indexes for average **O(1)** lookup by equipment or booking ID. Each equipment item has a `TreeMap` from active start time to booking ID. Because its reservations never overlap, only the neighbours found by `floorEntry()` and `ceilingEntry()` can conflict with a proposed slot. Each lookup costs **O(log n)** for *n* active bookings of that item.

Bookings use half-open intervals: a 09:00–11:00 reservation allows another to start at 11:00. Cancellation releases the slot and preserves its history. Equipment with any booking history stays in the catalog.

### JSON Persistence and File Safety

`BookingStore.kt` initializes `data/bookings.json` relative to the working directory. `StorageDtos.kt` describes the snapshot saved with kotlinx.serialization JSON 1.8.1. Each invocation loads the whole snapshot, validates the records and rebuilds the active schedule. This keeps storage simple for a small inventory.

A `FileChannel` lock on a sibling file coordinates CLI processes across the read–modify–write operation. The store writes a temporary file in the same directory and requests atomic replacement, falling back to regular replacement if necessary. Invalid JSON or inconsistent records stop the command before a save.

## 🧪 Testing and Analysis

The **26 November 2026 Windows clean build on JDK 17** produced the following results with JUnit Jupiter 5.12.2, JaCoCo 0.8.13 and Detekt 1.23.8.

| Check | Result |
| --- | --- |
| JUnit 5 | **48 passed**, 0 failed or skipped |
| JaCoCo Line Coverage | **98.3%** (227/231) |
| JaCoCo Branch Coverage | **86.4%** (165/191) |
| Detekt | **0 findings**, no baseline or suppressions |

The tests cover catalog operations, booking rules, parsing, command execution and storage. Persistence tests use temporary directories. Run the wrapper tasks `test`, `jacocoTestReport` and `detekt` individually or use `clean build`. The test task finalizes with the coverage report, and `check` includes static analysis.

| Local report | Open after building |
| --- | --- |
| JUnit | `build/reports/tests/test/index.html` |
| JaCoCo | `build/reports/jacoco/test/html/index.html` |
| Detekt | `build/reports/detekt/detekt.html` |

## 🔀 Development Workflow

Each feature branch started from the latest merged `main`. After local verification, a pull request ran the independent Ubuntu JDK 17 build in GitHub Actions before integration into `main`.

[PR #1 · Equipment catalog](https://github.com/yinghuayaya/LabEquipmentBooking/pull/1) · [PR #2 · Booking rules](https://github.com/yinghuayaya/LabEquipmentBooking/pull/2) · [PR #3 · JSON persistence](https://github.com/yinghuayaya/LabEquipmentBooking/pull/3) · [PR #4 · CLI and testing](https://github.com/yinghuayaya/LabEquipmentBooking/pull/4)

The empty bootstrap built successfully, but the first catalog tests exposed a Windows test-worker classpath encoding issue. Using the launcher's native encoding let JUnit run under the Chinese project path. Boundary tests checked that adjacent reservations are allowed, and restart tests checked that the schedule could be rebuilt from disk.

[Development record](PROJECT_LOG.md) · [Assessment report and academic disclosure](REPORT.md) · [Assessed release v1.0.2](https://github.com/yinghuayaya/LabEquipmentBooking/releases/tag/v1.0.2)

## 📚 References

The supplied *Software Development Tools Continuous Assessment Brief 2026–2027* defines the project requirements.

- [Kotlin Gradle compatibility](https://kotlinlang.org/docs/gradle-configure-project.html)
- [Gradle Wrapper](https://docs.gradle.org/8.12.1/userguide/gradle_wrapper.html)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
- [JUnit 5](https://docs.junit.org/5.12.2/user-guide/)
- [JaCoCo with Gradle](https://docs.gradle.org/8.12.1/userguide/jacoco_plugin.html)
- [Detekt compatibility](https://detekt.dev/docs/introduction/compatibility/)
