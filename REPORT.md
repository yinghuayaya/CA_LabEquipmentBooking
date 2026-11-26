# Laboratory equipment booking

**Software Development Tools CA1 · 2026–2027 · 26 November 2026**

The Kotlin CLI records equipment and reservations in a local JSON file. A requester can find an item, reserve it and cancel the reservation. Cancelled records stay available for inspection. The repository demonstrates the toolchain through four feature pull requests and independent Windows and Linux builds.

## Preventing double bookings

Each item has a sorted index of active start times. When adding a reservation, the service checks whether the preceding booking ends after the new start or the following booking starts before the new end. Endpoints can coincide. Looking up these two neighbors costs O(log n), while HashMaps provide average constant-time lookup by equipment or booking ID. Cancelling a booking removes its slot from the index. Deleting equipment with any history is rejected to preserve the relationship between records.

## Restoring the schedule

The JSON snapshot contains the equipment, full history and next booking number. On startup the service rebuilds the index using its insertion rules. A file with conflicting active slots or an invalid sequence fails validation before replacement. A lock covers each read-modify-write transaction. The store writes its replacement in the same directory and requests an atomic move. Reading the whole snapshot per command keeps the implementation small at the cost of startup time as history grows.

## Verification and development evidence

The JDK 17 clean build ran 48 tests with zero failures or skips. JaCoCo reported 227 of 231 lines and 165 of 191 branches covered. Detekt reported zero findings without a baseline or suppressions. The first catalog build exposed a classpath encoding mismatch in the Windows test worker. Running the launcher with its native encoding fixed discovery in the Chinese project path.

GitHub recorded separate merges for the [catalog][source-1], [booking rules][source-2], [storage][source-3] and [CLI][source-4]. Each passed its Ubuntu JDK 17 check before merge. Tests converted the interval boundary and corrupted-file requirements into reproducible checks. The project log records the failures alongside their fixes.

## Ten-minute demonstration

1. **Minutes 0–2.** Show the assessed commit and four merged PRs. Start the wrapper clean build.
2. **Minutes 2–5.** Add equipment, book it and try an overlapping slot. Show that an adjacent slot succeeds.
3. **Minutes 5–7.** Cancel and rebook the original slot. Run a new process and filter for cancelled history.
4. **Minutes 7–10.** Open the JUnit, JaCoCo and Detekt reports. Explain how the schedule is rebuilt after restart and show the corruption-preservation test.

**Sources and disclosure.** The supplied CA1 brief defines the deliverables. Implementation references include the [Gradle Wrapper][source-5], [Kotlin serialization][source-6], [JUnit 5][source-7], [JaCoCo][source-8] and [Detekt][source-9]. OpenAI Codex assisted with implementation, testing, documentation and repository operations.

[source-1]: https://github.com/yinghuayaya/CA_LabEquipmentBooking/pull/1
[source-2]: https://github.com/yinghuayaya/CA_LabEquipmentBooking/pull/2
[source-3]: https://github.com/yinghuayaya/CA_LabEquipmentBooking/pull/3
[source-4]: https://github.com/yinghuayaya/CA_LabEquipmentBooking/pull/4
[source-5]: https://docs.gradle.org/8.12.1/userguide/gradle_wrapper.html
[source-6]: https://github.com/Kotlin/kotlinx.serialization
[source-7]: https://docs.junit.org/5.12.2/user-guide/
[source-8]: https://docs.gradle.org/8.12.1/userguide/jacoco_plugin.html
[source-9]: https://detekt.dev/docs/introduction/compatibility/
