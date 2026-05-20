# AGENTS.md

## Build & Run

```bash
./mvnw quarkus:dev          # dev mode (hot reload), Dev UI at /q/dev/
./mvnw test                 # unit tests only
./mvnw verify -B            # full build + tests (same as CI)
./mvnw package              # produces target/quarkus-app/quarkus-run.jar
```

## Project Shape

- **Single-module Maven** project (no workspaces, no parent/child POMs)
- **Quarkus 3.35.3** with **Java 25** (`maven.compiler.release=25`)
- No Node.js / npm — the **Web Bundler extension** resolves JS dependencies from Maven Central (`org.mvnpm:*` artifacts). `src/main/resources/web/app.js` is the bundler entrypoint.

## Framework Stack

- **Renarde** (server-side web framework): controllers in `src/main/java/rest/`, Qute templates in `src/main/resources/templates/`. A controller class `Foo` with method `bar()` renders `templates/Foo/bar.html` by default.
- **Qute** template engine: `{#include}`, `{#for}`, `{#if}`, `{#form}`, `{uri:...}` syntax. Use `@CheckedTemplate` + native interface for type-safe templates.
- **No database** yet — `model/Todo.java` uses an in-memory `ArrayList`.

## Testing

- Framework: Quarkus JUnit (JUnit 5)
- **Integration tests are skipped** (`<skipITs>true</skipITs>`), enabled only in the `native` profile
- No test files exist yet — the project is a scaffold
- Test source tree: `src/test/java/`

## CI

- GitHub Actions: `.github/workflows/ci.yml` runs `./mvnw verify -B` on push/PR to `main`
- JDK: Temurin 25 (via `setup-java@v4`)

## Code Style

- No linter, formatter, or pre-commit hooks configured
- Package naming uses short names (`rest`, `model`, `util`) rather than reverse-domain convention
- `Startup.java` seed data only runs in `LaunchMode.DEVELOPMENT`
