# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

`exiftool-lib` (`com.github.mjeanroy:exiftool-lib`) is a Java wrapper around Phil Harvey's ExifTool executable. It is a fork of `rkalla/exiftool`. The root package is still `com.thebuzzmedia.exiftool`. It is a single-module Maven library with no required runtime dependencies. slf4j, logback, log4j, log4j2 and Guava are all `<optional>` and are detected on the classpath at runtime.

## Build & test

Use the Maven wrapper (`./mvnw`).

- Build without tests: `./mvnw install -DskipTests`
- Unit tests only: `./mvnw test`
- Unit and integration tests: `./mvnw verify`
- Single test class: `./mvnw test -Dtest=StayOpenStrategyTest`
- Single test method: `./mvnw test -Dtest=StayOpenStrategyTest#it_should_execute_command`
- Single integration test: `./mvnw verify -Dtest=ExifToolStayOpenIT -Dsurefire.failIfNoSpecifiedTests=false`
- Javadoc (CI checks this): `./mvnw javadoc:javadoc`

Notes:
- Surefire is set up so that `*IT.java` classes are excluded from the `test` phase. A second surefire execution runs them in the `integration-test` phase. There is no failsafe plugin.
- The source targets **Java 8** (`java.version=1.8`), and CI runs the tests on JDK 8, 11, 17, 21, 25 and 27. But the Javadoc is written as **Markdown doc comments (`///`)**, so CI builds and runs Javadoc with JDK 25. Keep the code Java 8 compatible: no `var`, records, `List.of`, etc. Write new docs in the same `///` Markdown style.
- Integration tests run a real ExifTool 10.16 that is bundled under `src/test/resources/exiftool-10_16/{unix,windows}`. `TestConstants.EXIF_TOOL` picks the right binary for the OS, so you don't need ExifTool installed. The unix build is a Perl script, so `perl` must be on the PATH.
- Every source file starts with the Apache 2 license header (copyright "The Buzz Media, LLC" plus "Mickael Jeanroy"). Copy it from an existing file.
- Releases are done with the `Release` GitHub workflow (`maven-release-plugin` with the `-Prelease` profile, publishing to Maven Central). Don't release locally. The `[maven-release-plugin] ...` commits come from that workflow; never write them by hand.
- Commit messages follow Conventional Commits: `feat:`, `fix:`, `chore(ci):`, `chore(docs):`, `build(deps):`, ...
- The `pom.xml` and the CI matrix are the source of truth for the project version and supported JDKs. The README's "Java >= 7" and its `2.6.0` install snippet are out of date.

## Code style

Follow `idea-formatting.xml`:
- Indent with tabs.
- Put `else`, `catch`, `finally` and the `while` of a do/while on a new line, not after the closing `}`.
- No wildcard imports.

## Architecture

`ExifToolBuilder` → `ExifTool` → `ExecutionStrategy` → `CommandExecutor` → OS process.

- **`ExifToolBuilder`** resolves the defaults lazily through small `FactoryFunction`s:
  - The path comes from the `exiftool.path` system property, defaulting to `exiftool`.
  - The cleanup delay comes from `exiftool.processCleanupDelay`, defaulting to 10 minutes.
  - The executor comes from `CommandExecutors.newExecutor()`.
  - It picks the strategy: `withPoolSize(n)` gives a `PoolStrategy` made of n `StayOpenStrategy`s; `enableStayOpen()` gives a `StayOpenStrategy`; otherwise it uses `DefaultStrategy`.
- **`ExifTool`** is `AutoCloseable` and thread-safe.
  - The constructor queries the ExifTool version through `VersionCache` and calls `strategy.isSupported(version)`. If that check fails it throws `UnsupportedFeatureException`; if the binary can't be run it throws `ExifToolNotFoundException`.
  - It registers a GC cleanup task through `commons/gc/CleanerFactory`, which uses `java.lang.ref.Cleaner`, then `sun.misc.Cleaner`, then a no-op.
  - `close()` calls `strategy.shutdown()`. `stop()` only stops the process (`strategy.close()`), and the strategy can restart it later.
- **Strategies** (`core/strategies`):
  - `DefaultStrategy` starts one process per call.
  - `StayOpenStrategy` keeps a daemon running (`-stay_open True -@ -`), with access synchronized on the strategy. A `Scheduler` from `core/schedulers` stops the daemon after a period of inactivity, and the next call restarts it. To shut down it writes `-stay_open\nFalse\n`.
  - `PoolStrategy` spreads calls across several stay-open strategies for parallel use.
- **Process layer** (`process/`): `Command`/`CommandBuilder` describe a command line. `CommandExecutor` either runs a one-shot command (`execute`) or starts a long-lived `CommandProcess` (`start`) that you write to and read from. Output is consumed line by line with `OutputHandler`s.
- **Output handlers** (`core/handlers`):
  - `StandardTagHandler` and `AllTagHandler` parse `Tag: value` lines into `Map<Tag,String>`.
  - `StopHandler` stops reading at the stay-open `{ready}` sentinel.
  - `RawOutputHandler` backs `getRawExifToolOutput`.
- **Tags and formats**: `Tag` and `Format` are interfaces, so users can add custom tags.
  - The built-in implementations are the enums `core/StandardTag` and `core/StandardFormat`.
  - `UnspecifiedTag` and `NonConvertedTag` are for tags that aren't predefined.
  - `ExifToolOptions` / `StandardOptions` hold per-call options such as format, overwrite and ignore-minor-errors.
- **Optional-dependency detection** (`commons/reflection/DependencyUtils`, `ClassUtils.isPresent`):
  - `logs/LoggerFactory` first tries a `ServiceLoader<LoggerProvider>`, then slf4j, then log4j2, then log4j, then falls back to `DefaultLogger`.
  - `core/cache/VersionCacheFactory` uses Guava if it is present and `DefaultVersionCache` otherwise.
  - Never add a hard dependency on any of these libraries.
- **`commons/`** contains internal helpers (`PreConditions`, `Strings`, `IOs`, …) used in place of third-party libraries. Use them instead of adding dependencies.

## Tests

- JUnit 5 with AssertJ, Mockito 4 (kept on 4.x because of Java 8), EqualsVerifier and Awaitility.
- Shared helpers live in `src/test/java/.../tests`: `TestConstants`, the builders, and the JUnit extensions `SystemPropertyExtension`, `SystemOutExtension` and `ProcessLeakDetectorExtension`.
- Test methods are named in the `it_should_...` snake_case style.
- `Benchmark.java` and `Example.java` in the root test package are `main` programs you run by hand, not tests. Surefire doesn't pick them up because of their names, so keep those names.
- `src/test/java/.../readme/**` holds the example classes that are quoted in `README.md`. If you change one of them, update the README snippet too.
