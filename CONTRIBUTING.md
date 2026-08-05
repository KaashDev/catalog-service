# Contributing to CatalogService

Thanks for picking up an issue during the sprint phase! This doc covers how
to claim work, how to run things locally, and what we expect in a PR.

## Claiming an issue

1. Browse the [open issues](../../issues). Each is labelled with a difficulty
   (`difficulty:easy`, `difficulty:medium`, `difficulty:hard`) and an area
   (`area:api`, `area:data`, `area:tests`, `area:docs`).
2. Comment on the issue to claim it (e.g. "I'll take this"). One person per
   issue at a time.
3. **You have 48 hours from your claiming comment to open a PR.** If 48 hours
   pass with no PR (draft PRs count!), the issue is considered released and
   anyone else can claim it. This keeps issues moving during the short sprint
   window — it's not a judgment on you, life happens.
4. Fork the repo (or branch directly if you have write access), do the work,
   and open a PR that references the issue (e.g. `Closes #12`).

If you're not sure how to approach an issue, ask questions on the issue
thread before or after claiming — that's normal and encouraged.

## Running things locally

Clone your fork, then:

```bash
# Run the full test suite (uses an in-memory H2 database, no setup needed)
./mvnw test

# Run the app against a local Postgres via Docker
docker-compose up postgres
./mvnw spring-boot:run

# Or run everything in Docker
docker-compose up --build
```

The app listens on `http://localhost:8080`. See the [README](README.md) for
the full API reference.

## Before opening a PR

- `./mvnw test` passes locally.
- New behavior has a test (unit test in `service`, or a `MockMvc` test in
  `controller`, following the existing patterns).
- Bean Validation annotations are used for new input constraints rather than
  manual `if` checks, to stay consistent with the rest of the codebase.
- Keep PRs scoped to the issue you claimed — small, focused PRs are much
  faster to review.

## Getting help

Ask in the cohort Discord channel for this repo, or leave a comment on your
issue/PR. Maintainers will review PRs as they come in.
