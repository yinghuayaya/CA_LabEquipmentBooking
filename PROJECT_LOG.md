# Project log

Dates use America/Chicago. Entries record work as performed; they are not a reconstructed weekly history.

## 2026-11-26 — Environment inspection and bootstrap started

- Read the supplied `../CA.docx` assessment brief. It requires genuine feature pull requests, contemporaneous records, disclosure of generated code, a demonstration, and submission report/tag evidence.
- Inspected the workspace and the existing empty `CA_LabEquipmentBooking` directory. No project files, wrapper, repository, or remote existed. Other laboratory projects were left untouched.
- Commands: `Get-ChildItem`, `java -version`, `git --version`, `git -C CA_LabEquipmentBooking status --short --branch`, `git -C CA_LabEquipmentBooking remote -v`, `gh auth status`, JDK directory inspection, and DOCX XML text extraction.
- Git is 2.53.0.windows.2. GitHub CLI authentication succeeded for `yinghuayaya`. Active Java is 26; the inspected installations contain Java 25/26 rather than 17.
- Started downloading a portable Eclipse Temurin JDK 17 and Gradle 8.12.1 into the user-local `CA1Tools` directory, outside the repository, with SHA-256 verification. No global Java settings changed.
- Added the Gradle Kotlin DSL baseline with the requested pinned versions, JUnit Platform launcher 1.12.2, JDK 17 toolchain, JaCoCo report finalizer, and Detekt check dependency. No application or test implementation exists yet.
- Compatibility reference: Kotlin documents 2.1.21 support through Gradle 8.12.1. Detekt 1.23.8 uses its own Kotlin 2.0.21 analysis compiler; do not force its compiler dependencies to the application compiler version. Actual task execution is still pending.
- Decision: stop after the safe local baseline because there is no authorized remote. Implementing all features now and constructing PR history later would undermine the assessment workflow. No remote repository, publication, PR, or simulated review will be created.
- Assistance disclosure: this baseline and log were prepared with OpenAI Codex assistance. The student must review, understand, and disclose assisted work under the assessment rules.
- Remaining: verify wrapper/build baseline, initialize and commit the local bootstrap, obtain remote authorization, then implement each feature on a branch and merge through a genuine PR.

## 2026-11-26 — Wrapper retrieved; environment and remote blockers confirmed

- Retrieved `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar` directly from the official Gradle `v8.12.1` source tag. Added wrapper properties with the published distribution checksum.
- Verified the wrapper JAR SHA-256 against Gradle's published checksum: `2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046`.
- Pinned the distribution SHA-256: `8d97a97984f6cbd2b85fe4c60a743440a347544bf18818048e611f5288d46c94`.
- The Temurin download failed with an unexpected EOF. The partial archive was 187135009 bytes, while the API reported 191150540 bytes; its checksum did not match. It was not extracted or executed.
- Attempted a bounded Microsoft JDK 17 download and a resumable Temurin download. Neither supplied an installed JDK during this session. Partial downloads remain outside the repository under `%LOCALAPPDATA%\CA1Tools`.
- Ran `.\gradlew.bat --version`; it began downloading Gradle but did not complete. Stopped only the download/wrapper processes started for this task after lack of progress. Wrapper execution and distribution integrity are therefore not verified, despite successful verification of the small wrapper JAR.
- Ran `git init -b main` after checking repository status, remotes, and GitHub authentication. Used the existing Git identity without changing it. No remote was added and nothing was published.
- Results: no build completed; no tests exist or ran; no coverage report was generated; Detekt has not run. There is no claim of zero findings or BUILD SUCCESS.
- Remaining prerequisites: install/provide a usable JDK 17, complete the Gradle download, verify the baseline, and obtain permission for a suitable new GitHub repository. The five feature branches and real PR workflow must follow those prerequisites. The application remains unimplemented.

## 2026-11-26 — JDK and wrapper verified

- Resumed the existing Temurin archive with `curl.exe -C -`, verified SHA-256 `eafba0944f777abdc74d5aad16049c9b5a1275b6ce99e2939ba9f7f3e6f08c87` against Adoptium metadata, and extracted it under `%LOCALAPPDATA%\CA1Tools`. Its `java -version` reports Temurin 17.0.21.1+2.
- Retrieved the Gradle 8.12.1 binary distribution from the Tencent mirror. Verified its SHA-256 against the official value pinned in the wrapper properties before placing the ZIP in the local wrapper cache. The repository retains the official Gradle URL for clean clones.
- Set `JAVA_HOME` for the verification process. `.\gradlew.bat --version` succeeded on Windows 11 with Gradle 8.12.1 and launcher JVM 17.0.21.1. Started `.\gradlew.bat clean build` against the bootstrap commit.
- The initial plugin download progressed slowly. Configured Maven Central first for plugins with the Plugin Portal as fallback. Used a resumed Maven Central download for the Kotlin plugin and verified its published digest before caching it.
- Ran `.\gradlew.bat clean build --console=plain --info` successfully in 3m 39s. The task graph included `detekt NO-SOURCE`, `test NO-SOURCE`, and `jacocoTestReport SKIPPED`. No tests executed or reports were produced. The feature implementation still requires the authorized remote and genuine PRs.
