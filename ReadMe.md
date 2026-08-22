
# Job Intelligence Platform

An intelligent job-search and job-application platform designed to maximize high-quality interview opportunities per unit of user effort.

The system continuously discovers relevant software engineering jobs, normalizes and deduplicates listings from multiple sources, evaluates them against a structured candidate profile, ranks opportunities, and eventually automates application workflows while keeping the user in control whenever human judgment is required.

**Primary Optimization Target:**  
`Expected Interview Opportunities / User Time Required`

---

## 🚀 Project Status: Phase 1 (Job Intelligence)

### Completed
- Spring Boot 4.1 project setup (Java 25 LTS)
- PostgreSQL development environment with Docker Compose
- Flyway database migrations & Spring Data JPA
- Testcontainers integration (Unit/Integration test separation)
- Initial Company and Job domain implementation

### Currently Building
- Job source abstraction & ingestion pipeline
- Job normalization and deduplication
- Job requirement extraction
- Candidate/job matching and ranking

### Planned
- Resume intelligence & application automation
- Automated application-question answering
- Human approval workflow & application tracking
- Recruiter outreach assistance & interview preparation
- Outcome-based ranking optimization

---

## 🏗 Architecture

This project is built as a **serious backend system**, not a minimal proof of concept. The platform is developed as a **modular monolith**, organized around feature boundaries (business capabilities) rather than global technical layers. Microservices are intentionally avoided until concrete scaling needs arise.

### High-Level Pipeline


Job Sources (Greenhouse, Lever, LinkedIn, Email, etc.)
      ↓
Job Ingestion & Normalization
      ↓
Deduplication (Canonical Job Model)
      ↓
Deterministic Qualification Filtering
      ↓
LLM Semantic Analysis & Job Ranking
      ↓
PostgreSQL (System of Record)
      ↓
Daily Recommendations


---

## 💻 Technology Stack

| **Category**       | **Technologies**                                                                                |
| ------------------ | ----------------------------------------------------------------------------------------------- |
| **Backend**        | Java 25 LTS, Spring Boot 4.1, Maven, Spring MVC, Spring Data JPA, Hibernate, Jakarta Validation |
| **Database**       | PostgreSQL, Flyway                                                                              |
| **Testing**        | JUnit, Spring Boot Test, Testcontainers                                                         |
| **Infrastructure** | Docker, Docker Compose, Spring Boot Actuator                                                    |
| **Future/Planned** | Redis, Playwright, TypeScript browser workers, LLM APIs, n8n, Gmail API                         |

---

## 🧠 Core Systems

### 1. Canonical Job Model & Deduplication

The same job often appears across multiple platforms (e.g., LinkedIn, Greenhouse, Indeed). The system separates the **Canonical Job** from the **Job Source Listing** to maintain accurate provenance without cluttering the pipeline.

Deduplication uses multiple signals:

* Source + External ID uniqueness.
* Canonical application URL matching.
* Canonical fingerprinting (Company + Normalized Title + Location + Employment Type + Experience).
* Normalized description similarity.
* *Conservative merging strategy:* False positives are more damaging than temporary duplicate records.

### 2. Matching Architecture

* **Stage 1 — Deterministic Qualification:** Fast filtering based on hard constraints (role, seniority, location, impossible experience requirements).
* **Stage 2 — Semantic Analysis (LLM):** The strongest candidates pass to an LLM to evaluate semantic skill overlap, project relevance, and transferable experience.

    * *Note:* LLM outputs are structured, stored with reasoning/model metadata for reproducibility, and **never** override hard qualification rules.

### 3. Database Design Principles

* **Flyway owns the schema:** `ddl-auto: validate` is used. Applied migrations are immutable.
* **Human Truth Over Model Guessing:** The system must never invent candidate information simply to automate an application or improve a score.

---

## 🗺 Long-Term Roadmap

* **Phase 1 — Job Intelligence (Current):** Discovery → Normalization → Deduplication → Qualification → Ranking.
* **Phase 2 — Candidate & Resume Intelligence:** Structured profiles → Resume knowledge base → Role-specific customization.
* **Phase 3 — Application Automation:** Application prep → Form automation → Question answering → Human approval → Tracking.
* **Phase 4 — Learning & Optimization:** Track outcomes (Interviews, Offers, Rejections) → Optimize ranking probabilities.

---

## 🛠 Local Development

### Prerequisites

* Java 25 LTS
* Maven
* Docker & Docker Compose

### Quick Start

1. **Start the Database:**

   Bash

   ```bash
   docker compose up -d


2. **Run the Application:**

   Bash

   ```bash
   ./mvnw spring-boot:run
   ```

3. **Run Tests:**

    * Fast unit tests (isolated): `./mvnw test`
    * Unit + Integration tests (requires Docker): `./mvnw clean verify`

4. **Health Check:**

   Bash

   ```bash
   curl http://localhost:8080/actuator/health
   ```

> **Note:** To reset the local database volume and wipe development data, run `docker compose down -v`.

```
```
