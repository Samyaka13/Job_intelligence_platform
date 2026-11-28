# Job Intelligence Platform

A backend-first platform for collecting job listings, extracting structured requirements, and ranking opportunities against candidate profiles. The current implementation is an early foundation for a multi-user SaaS product.

The long-term goal is to improve **expected interview opportunities per unit of user effort**. This is a design objective; outcome-based optimization is not implemented yet.

## What works today

- Create companies, candidate profiles, and candidate skills through the REST API.
- Ingest jobs from a configured **Greenhouse** board.
- Normalize listings and keep canonical jobs separate from source-specific records.
- Extract job requirements with rules and Google Gemini.
- Qualify and rank jobs with deterministic location, employment, experience, skill, role, and seniority checks.
- Persist candidate/job matches and return ranked results through a digest API.
- Recompute matches for active profiles on a daily schedule.

### Current boundaries

- Greenhouse is the only implemented collector. Lever, Ashby, and company career-page integrations are planned; other source names in seed data are placeholders.
- Ingestion is triggered through the API; the daily schedule refreshes matches for jobs already stored.
- Gemini extracts job requirements. Candidate matching and scoring use application code today; semantic LLM matching is planned.
- Multi-user SaaS capabilities are not implemented. There is no authentication, account/tenant model, or tenant-level data isolation.
- Digest results are returned by the API; email delivery, resume processing, recruiter outreach, and application submission are not implemented.

## How it works

```text
Greenhouse board
      |
      v
Manual ingestion API -> Collector -> Normalize and deduplicate
                                          |
                                          v
                              Canonical job + source listing
                                          |
                                          v
                              Rule-based + Gemini extraction
                                          |
                                          v
                                     PostgreSQL
                                          |
Candidate profile + skills -> Deterministic qualification and ranking
                                          |
                                          v
                                  Ranked digest API
```

The application is a **modular monolith** organized around business capabilities. A canonical job holds normalized data; source listings retain source identity and provenance. Deduplication currently uses source/external ID and a normalized job fingerprint; it does not use fuzzy description similarity or canonical URL equality.

### Product direction: multi-user SaaS

The intended product serves independent candidates. Candidate profiles, skills, preferences, resumes, match history, applications, and outcomes should be private to each account. Employer companies, source configurations, canonical jobs, source listings, and extracted job requirements can form a shared job catalog.

This is a target design, not the current data model. Authentication, account/tenant entities, authorization, and tenant-scoped data access must be implemented before multiple real users can safely use the service. Keep an employer **Company** distinct from a platform user or account.

## Technology

- Java 25 and Maven
- Spring Boot 4.1, Spring MVC, Spring Data JPA, Jakarta Validation
- PostgreSQL 17 and Flyway
- Google Gemini for requirement extraction
- JUnit, Spring Boot Test, and Testcontainers
- Docker Compose and Spring Boot Actuator

## Run locally

### Prerequisites

- JDK 25
- Docker with Docker Compose
- A Google Gemini API key

### 1. Set environment variables

```bash
export GEMINI_API_KEY="your-gemini-api-key"
export GREENHOUSE_BOARD_TOKEN="your-greenhouse-board-token"
```

Do not commit API keys or real credentials. The current application configuration expects both variables at startup. Greenhouse ingestion reads the board token from the company's source-configuration JSON, so the `GREENHOUSE_BOARD_TOKEN` environment property is currently not the token used by the collector.

### 2. Start PostgreSQL

```bash
docker compose up -d
```

Compose starts PostgreSQL on `localhost:5432` with the local development database settings:

- Database: `job_intelligence`
- Username: `job_intelligence`
- Password: `job_intelligence_dev`

These credentials are for local development only. Flyway applies migrations when the application starts.

### 3. Start the API

```bash
./mvnw spring-boot:run
```

Check the health endpoint:

```bash
curl http://localhost:8080/actuator/health
```

## Try the API

The examples use ID `1`; replace it with the ID returned by your API. The Greenhouse board token is the identifier in the board's public URL.

### Create a company

```bash
curl -X POST http://localhost:8080/api/v1/companies \
  -H "Content-Type: application/json" \
  -d '{
    "canonicalName": "airbnb",
    "displayName": "Airbnb",
    "websiteUrl": "https://www.airbnb.com"
  }'
```

### Configure and ingest its Greenhouse board

```bash
curl -X POST http://localhost:8080/api/v1/companies/1/source-configurations \
  -H "Content-Type: application/json" \
  -d '{
    "sourceCode": "GREENHOUSE",
    "configuration": {
      "boardToken": "airbnb"
    }
  }'

curl -X POST http://localhost:8080/api/v1/companies/1/ingest
```

### Create a candidate and add skills

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

### Refresh matches

```bash
curl -X POST "http://localhost:8080/api/v1/candidates/1/matching/refresh?limit=10"
```

The response contains ranked matches and the generated digest.

## API reference

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/companies` | Create a company |
| `GET` | `/api/v1/companies/{id}` | Get a company |
| `POST` | `/api/v1/companies/{companyId}/source-configurations` | Configure a job source |
| `POST` | `/api/v1/companies/{companyId}/ingest` | Ingest jobs for a company |
| `POST` | `/api/v1/candidates` | Create a candidate profile |
| `PATCH` | `/api/v1/candidates/{candidateId}` | Update a candidate profile |
| `POST` | `/api/v1/candidates/{candidateProfileId}/skills` | Add a candidate skill |
| `POST` | `/api/v1/candidates/{candidateProfileId}/skills/bulk` | Add candidate skills |
| `POST` | `/api/v1/candidates/{candidateProfileId}/matching/refresh` | Recompute matches and return a digest |
| `GET` | `/api/v1/candidates/{candidateProfileId}/daily-digest` | Generate and return a digest |

The daily-digest GET endpoint currently runs matching again; it does not only retrieve a previously generated result. The matching schedule defaults to 2:00 AM in the JVM's default timezone.

## Tests

```bash
./mvnw test
./mvnw clean verify
```

Docker must be available for tests that start PostgreSQL through Testcontainers. The integration suite also includes a public Greenhouse-board request, which needs network access. Gemini integration tests make a live model call when enabled with the `llm` profile and a configured API key.

## Roadmap

| Phase | Scope | Status |
| --- | --- | --- |
| 1. Job intelligence | Ingestion, normalization, requirement extraction, qualification, and ranking | In progress |
| 2. Resume intelligence | Resume parsing and evidence-backed candidate profiles | Planned |
| 3. Application workflow | Application preparation, human approval, and tracking | Planned |
| 4. Outcome optimization | Track interviews, offers, and rejections; improve ranking from outcomes | Planned |

## Design principles

- Flyway owns database schema changes; Hibernate validates the schema.
- Candidate information must remain truthful; automation must not invent qualifications.
- Prefer conservative deduplication when a possible merge is uncertain.
- Keep a modular monolith until operational needs justify distributed services.
