# Job Intelligence Platform

A backend-first job intelligence platform that discovers job listings, converts job descriptions into structured requirements, and ranks opportunities against a candidate profile.

The product objective is to improve:

> **Expected interview opportunities per unit of user effort**

This is the optimization goal, not yet a measured or outcome-trained metric. The current repository focuses on the job-ingestion and matching foundation.

**Project status:** Phase 1 — Job Intelligence (active development)

---

## What is implemented

The repository contains working backend slices for:

- **Company and candidate management:** company records, candidate profiles, preferred locations and employment types, and candidate skills.
- **Job-source configuration:** source adapters are resolved through a common collector abstraction.
- **Greenhouse ingestion:** fetches jobs from a configured Greenhouse public board and stores source-specific listing data.
- **Normalization:** normalizes job titles, descriptions, URLs and locations; parses employment type, seniority, experience and salary fields.
- **Canonical jobs and source provenance:** stores a canonical job separately from each source listing, allowing a job to retain its origin and external identifiers.
- **Requirement extraction:** combines rule-based extraction with structured output from the configured Gemini model.
- **Candidate matching:** applies deterministic qualification rules, skill matching, role-family classification and seniority scoring.
- **Match persistence and digest APIs:** saves candidate/job match results and exposes ranked recommendations through REST endpoints.
- **Scheduled matching:** recomputes matches for active candidate profiles daily.

### Current implementation boundaries

A few distinctions are important:

- **Greenhouse is the only implemented job collector.** Other source codes are seeded in the database, but that does not mean their collectors exist. Lever, Ashby, LinkedIn, Indeed, Naukri, Wellfound, email and generic career-page ingestion are not implemented as working adapters yet.
- **Ingestion is manually triggered through an API.** The daily scheduler recomputes matches for jobs already stored in the database; it does not fetch new jobs.
- **Gemini is used for job-requirement extraction, not candidate/job semantic ranking.** Candidate matching currently uses deterministic application code.
- **The daily digest is returned by the API.** Email delivery, recruiter outreach, resume tailoring and application submission are not implemented.
- The expected-interview optimization target is not yet trained on application outcomes.

---

## How the system works

### High-level flow

```text
Company + source configuration
            |
            v
     Job source collector
       (Greenhouse today)
            |
            v
       Raw job listings
            |
            v
 Normalization and parsing
            |
            v
 Canonical job + source listing
            |
            v
 Rule-based + Gemini requirement extraction
            |
            v
         PostgreSQL
            |
            +------------------------------+
                                           |
Candidate profile + skills                 |
            |                              |
            v                              |
 Deterministic qualification               |
            |                              |
            v                              |
 Skill + role + seniority scoring          |
            |                              |
            v                              |
 Persisted candidate/job matches <---------+
            |
            v
      Ranked digest API
```

The application is organized as a **modular monolith**. Business capabilities are separated into feature areas rather than split into independently deployed services. Microservices are not part of the current design.

### 1. Ingestion and canonical job model

A source adapter returns raw listings. The ingestion pipeline normalizes the listing and creates or updates:

- A **canonical job**, which contains the normalized job data.
- A **source listing**, which retains the source, external job ID, source URL and raw source payload.

This keeps source provenance separate from the job used by the rest of the application. When a source no longer returns a previously active listing, that source listing can be deactivated.

### 2. Current deduplication behavior

The current merge lookup uses these signals, in order:

1. **Source + external job ID:** find an existing canonical job through its source listing.
2. **Canonical fingerprint:** fall back to a SHA-256 fingerprint built from normalized company name, normalized title, normalized locations, employment type, seniority and experience range.

A description hash is also stored. It is used to detect description changes and trigger requirement re-extraction; it is **not** a description-similarity deduplication algorithm.

URLs are normalized, but the current ingestion lookup does not use canonical application-URL equality to merge jobs. Nor does it currently perform fuzzy description matching. These are possible future improvements, not implemented deduplication signals.

The approach favors avoiding incorrect merges over aggressively combining similar-looking listings.

### 3. Requirement extraction

For a new job, or when its description changes:

1. A rule-based extractor identifies requirements.
2. The configured Gemini model produces structured requirement output.
3. The results are merged and persisted as typed job requirements, including mandatory status and requirement grouping information.

This LLM step extracts facts from the job description. It does not decide whether a candidate is a good match.

### 4. Candidate qualification and ranking

Matching currently has deterministic stages:

1. **Qualification:** checks configured constraints such as preferred location, employment type, experience range and salary compatibility. Unknown location data is treated conservatively rather than automatically rejected.
2. **Skill matching:** compares candidate skills with extracted language and technology requirements, including mandatory requirements and grouped alternatives.
3. **Role and seniority scoring:** classifies the job role and evaluates role-family and seniority fit.
4. **Persistence and ranking:** saves the match result and returns eligible jobs ordered by score.

The current overall score combines:

| Signal | Weight |
| --- | ---: |
| Technical skill score | 45% |
| Role fit | 40% |
| Seniority fit | 15% |

A missing mandatory skill applies an additional score reduction. These are current deterministic rules, not an LLM-generated relevance score.

---

## Technology stack

| Area | Technologies |
| --- | --- |
| Language and build | Java 25, Maven Wrapper |
| Application | Spring Boot 4.1.0, Spring MVC, Spring Data JPA, Hibernate, Jakarta Validation |
| Database | PostgreSQL 17, Flyway |
| AI requirement extraction | Google Gemini API (configured model: `gemini-3.5-flash-lite`) |
| Job-description processing | Jsoup |
| Testing | JUnit, Spring Boot Test, Testcontainers |
| Operations | Docker Compose, Spring Boot Actuator |

The repository also includes the OpenAI Java SDK and an OpenAI client implementation, but the Spring-managed `LlmClient` currently used by requirement extraction is the Gemini implementation. OpenAI is not currently an active runtime provider.

---

## Local development

### Prerequisites

- JDK 25
- Docker and Docker Compose
- A Google Gemini API key for requirement extraction

### 1. Configure environment variables

The checked-in `application.yml` references these environment variables:

```bash
export GEMINI_API_KEY="your-gemini-api-key"
export GREENHOUSE_BOARD_TOKEN="your-greenhouse-board-token"
```

**Greenhouse configuration note:** the current collector reads the actual `boardToken` from each company's stored source-configuration JSON (shown below). The global `GREENHOUSE_BOARD_TOKEN` property is bound by the application configuration but is not read by the collector itself. This is a configuration inconsistency that can be cleaned up separately.

Do not commit API keys or real credentials to the repository.

### 2. Start PostgreSQL

```bash
docker compose up -d
```

The Compose file starts PostgreSQL 17 and exposes it on `localhost:5432`. The local development database is:

| Setting | Value |
| --- | --- |
| Database | `job_intelligence` |
| Username | `job_intelligence` |
| Password | `job_intelligence_dev` |
| Port | `5432` |

These credentials are for local development only. Do not reuse them in a deployed environment.

### 3. Run the application

```bash
./mvnw spring-boot:run
```

Flyway applies the database migrations at startup. Hibernate is configured with `ddl-auto: validate`, so Flyway remains the schema owner.

### 4. Check application health

```bash
curl http://localhost:8080/actuator/health
```

The application exposes the Actuator `health`, `info` and `metrics` endpoints.

---

## API quick start

The examples below show the current REST workflow. Replace the IDs with the values returned by your API calls.

### 1. Create a company

```bash
curl -X POST http://localhost:8080/api/v1/companies \
  -H "Content-Type: application/json" \
  -d '{
    "canonicalName": "airbnb",
    "displayName": "Airbnb",
    "websiteUrl": "https://www.airbnb.com"
  }'
```

### 2. Configure its Greenhouse board

Use the board token from the company's Greenhouse board URL.

```bash
curl -X POST http://localhost:8080/api/v1/companies/1/source-configurations \
  -H "Content-Type: application/json" \
  -d '{
    "sourceCode": "GREENHOUSE",
    "configuration": {
      "boardToken": "airbnb"
    }
  }'
```

### 3. Ingest jobs for the company

```bash
curl -X POST http://localhost:8080/api/v1/companies/1/ingest
```

This invokes the enabled source configurations for the company. With the current implementation, Greenhouse is the supported collector.

### 4. Create a candidate profile

```bash
curl -X POST http://localhost:8080/api/v1/candidates \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Example Candidate",
    "email": "candidate@example.com",
    "currentLocation": "Bhopal, India",
    "preferredLocations": [],
    "minimumSalary": 0,
    "minimumSalaryCurrency": "INR",
    "experienceYears": 3,
    "preferredEmploymentTypes": ["FULL_TIME"]
  }'
```

### 5. Add candidate skills

```bash
curl -X POST http://localhost:8080/api/v1/candidates/1/skills/bulk \
  -H "Content-Type: application/json" \
  -d '{
    "skills": [
      {
        "skill": "Java",
        "proficiency": "ADVANCED",
        "yearsExperience": 3,
        "primary": true
      },
      {
        "skill": "Spring Boot",
        "proficiency": "ADVANCED",
        "yearsExperience": 2,
        "primary": true
      }
    ]
  }'
```

### 6. Refresh matches and get a digest

```bash
curl -X POST "http://localhost:8080/api/v1/candidates/1/matching/refresh?limit=10"
```

The response contains the generated digest and ranked matches.

### Main endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/companies` | Create a company |
| `GET` | `/api/v1/companies/{id}` | Get a company |
| `POST` | `/api/v1/companies/{companyId}/source-configurations` | Add a source configuration |
| `POST` | `/api/v1/companies/{companyId}/ingest` | Ingest jobs for a company |
| `POST` | `/api/v1/candidates` | Create a candidate profile |
| `PATCH` | `/api/v1/candidates/{candidateId}` | Update a candidate profile |
| `POST` | `/api/v1/candidates/{candidateProfileId}/skills` | Add a candidate skill |
| `POST` | `/api/v1/candidates/{candidateProfileId}/skills/bulk` | Add multiple candidate skills |
| `POST` | `/api/v1/candidates/{candidateProfileId}/matching/refresh` | Recompute matches and generate a digest |
| `GET` | `/api/v1/candidates/{candidateProfileId}/daily-digest` | Generate and return a digest |

**API behavior note:** the current `GET /daily-digest` implementation invokes the matching pipeline again; it is not a read-only retrieval of a previously generated digest.

---

## Testing

Run the Maven test lifecycle with:

```bash
# Surefire tests
./mvnw test

# Surefire + Failsafe integration-test/verify phases
./mvnw clean verify
```

Testcontainers is used for PostgreSQL-backed tests, so Docker must be available for tests that start containers. The integration-test suite also contains a Greenhouse client test that calls a public Greenhouse board and therefore needs network access. The Gemini integration test uses `GEMINI_API_KEY`; it is skipped when the key is not configured and makes a live model call when it is.

The current test setup is not entirely Docker-free under `./mvnw test`: the Spring application-context test imports the Testcontainers PostgreSQL configuration.

---

## Repository layout

```text
src/main/java/com/samyak/job_intelligence/
├── candidate/   # Candidate profiles, skills and related APIs
├── company/     # Company domain and APIs
├── common/      # Shared persistence and exception handling
├── job/         # Jobs, requirements, qualification, matching and digest
├── llm/         # LLM clients and requirement-extraction schemas
└── source/      # Source configuration, collectors and ingestion APIs

src/main/resources/
├── application.yml
└── db/migration/   # Flyway schema migrations
```

---

## Roadmap

| Phase | Scope | Status |
| --- | --- | --- |
| **1. Job Intelligence** | Source ingestion, normalization, deduplication, requirement extraction, qualification and ranking | In progress |
| **2. Resume Intelligence** | Resume parsing, evidence-backed candidate profile and role-specific resume customization | Planned |
| **3. Application Workflow** | Application preparation, form automation, answer drafting, human approval and tracking | Planned |
| **4. Outcome Optimization** | Track interviews/offers/rejections and improve ranking from observed outcomes | Planned |

---

## Design principles

- **Flyway owns the schema.** Migrations are versioned and applied; Hibernate validates rather than generating the schema.
- **Candidate facts must remain truthful.** Automation must not invent skills, experience, qualifications or other candidate information.
- **Prefer conservative deduplication.** A false merge can corrupt provenance and job data; uncertain duplicates should remain separate until stronger evidence is available.
- **Keep the system a modular monolith** until operational requirements justify distributed services.

## Current operational limitations

- The application is a backend API; no frontend is included in this repository.
- API authentication and authorization are not configured. Do not expose this development service publicly.
- The Compose file starts the database only; it does not containerize the Spring Boot application.
- Job ingestion is initiated through the API. Only matching is scheduled (default cron: 2:00 AM, using the JVM's default timezone (no explicit scheduler timezone is configured)).
- No application submission, email delivery, resume processing or recruiter outreach is performed yet.
