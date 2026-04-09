# AI-Powered SOC + Phishing Detector — Backend

Hackathon-ready **Spring Boot 3** API (Java **21**) for a cybersecurity dashboard: ingest logs, triage alerts, rule-based phishing analysis, risk scoring, and AI-style incident narratives (Gemini when configured, deterministic mocks offline).

## Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 14+

## Quick start

1. Create database: `CREATE DATABASE soc;`
2. Copy `.env.example` to `.env` (or set env vars / IDE run config).
3. Run:

```bash
mvn spring-boot:run
```

4. Swagger UI: [http://localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html) (or your `SERVER_PORT`)
5. Register the first user (becomes **ADMIN** when the `users` table is empty), or enable demo seeding:

```bash
set APP_DEMO_MODE=true
mvn spring-boot:run
```

Demo user: `admin@demo.local` / `DemoPass123!` (only created when `APP_DEMO_MODE=true` and that email does not exist).

**Frontend (React / Vite):** bax [FRONTEND.md](FRONTEND.md).

## Database (Supabase / PostgreSQL)

Tables are created automatically by Hibernate (`spring.jpa.hibernate.ddl-auto=update`) on first startup — you do **not** need to run SQL in the Supabase SQL Editor for the Java entities to exist. On your hosting provider, set `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, and `DB_SSL_MODE=require` (see `.env.example`).

## Configuration highlights

| Key | Purpose |
|-----|---------|
| `DB_*` / `DB_SSL_MODE` | PostgreSQL connection (required for cloud DB; defaults target local `soc` DB) |
| `APP_DEMO_MODE` | Seeds integrations row + demo admin + mock logs when DB empty |
| `GEMINI_API_KEY` | Enables Gemini HTTP path in `GeminiClient` / richer report text |
| `GROQ_API_KEY` | Enables Groq (Llama) for `/api/ai/chat` and alert triage when configured |
| `JWT_SECRET` | HS256 signing secret (use a long random value in production) |
| `CORS_ORIGINS` | Comma-separated allowed browser origins for the API |
| `SPRING_PROFILES_ACTIVE=prod` | Enables `application-prod.yml` (forwarded headers behind HTTPS proxy) |
| `APP_SCHEDULER_ENABLED` | Hourly risk snapshot via `RiskSnapshotScheduler` |

## API response shape

```json
{
  "success": true,
  "message": "...",
  "data": { }
}
```

Paginated `data`:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 57,
  "totalPages": 3
}
```

## Endpoint summary

| Method | Path | Notes |
|--------|------|--------|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public; returns JWT |
| GET | `/api/auth/me` | Bearer JWT |
| GET | `/api/health` | Public |
| GET | `/api/dashboard/summary` | Dashboard KPIs + recent alerts + risk trend |
| GET | `/api/dashboard/risk-trend?days=7` | Time series |
| GET | `/api/dashboard/recent-activity` | Logs + alerts merged |
| POST | `/api/logs` | Ingest log; may auto-create alerts + risk refresh |
| POST | `/api/logs/bulk` | Bulk ingest |
| GET | `/api/logs` | Filters: severity, sourceType, eventType, status, from, to, keyword, pageable |
| GET | `/api/logs/{id}` | Detail |
| PATCH | `/api/logs/{id}/status` | Triage status |
| POST | `/api/logs/simulate?count=15` | Mock generator |
| GET | `/api/alerts` | Filters + page |
| GET | `/api/alerts/{id}` | Detail |
| POST | `/api/alerts` | Manual alert |
| PATCH | `/api/alerts/{id}/status` | Workflow |
| PATCH | `/api/alerts/{id}/assign` | Assign analyst |
| GET | `/api/alerts/open/count` | Open + in-progress |
| GET | `/api/risk/current` | Latest snapshot (or empty template) |
| GET | `/api/risk/history?days=7` | Snapshots |
| POST | `/api/risk/recalculate` | Persist new `RiskSnapshot` |
| POST | `/api/phishing/analyze` | Rule engine + optional alert + risk |
| GET | `/api/phishing/history` | Pageable |
| GET | `/api/phishing/{id}` | Detail |
| POST | `/api/phishing/simulate` | Canned malicious sample |
| POST | `/api/reports/generate/daily` | Daily narrative |
| POST | `/api/reports/generate/incident/{alertId}` | Incident narrative |
| POST | `/api/reports/generate/phishing/{scanId}` | Phishing narrative |
| GET | `/api/reports` | Pageable |
| GET | `/api/reports/{id}` | Detail |
| GET | `/api/reports/{id}/download` | `text/plain` body |
| POST | `/api/integrations/wazuh/test` | Placeholder connectivity |
| POST | `/api/integrations/gemini/test` | Key smoke test |
| GET | `/api/integrations/status` | Provider map |
| GET | `/api/admin/stats` | **ADMIN** only |

**Roles:** `ADMIN`, `ANALYST` (mutations + analysis), `VIEWER` (read-only dashboard/reports/lists).

## Database schema (logical)

| Table | Purpose |
|-------|---------|
| `users` | Operators (email, password hash, role) |
| `security_logs` | Normalized SOC events |
| `alerts` | Incidents / work queue |
| `phishing_scans` | Email analysis history |
| `incident_reports` | Generated narratives |
| `risk_snapshots` | Point-in-time risk KPIs |
| `integration_configs` | External system metadata (masked keys) |

Enums map to `VARCHAR` columns.

## Implementation phases

1. **Phase 1 — Auth + logs + alerts:** JWT, log ingestion, auto-alert rules, triage endpoints.  
2. **Phase 2 — Phishing + risk:** `PhishingRuleEngine`, risk formula + snapshots, dashboard wiring.  
3. **Phase 3 — Reports + integrations + polish:** `AiContentGenerator` + Gemini, integration tests, PDF/email/Slack (future).

## Package layout

`com.project.soc` — `config`, `controller`, `dto`, `entity`, `enums`, `exception`, `repository`, `security`, `service` (+ `impl`), `integration`, `mapper`, `scheduler`, `util`.

> **Note:** Java reserves `enum`; the package is `enums`, not `enum`.

## TODOs for production

- Wazuh / OpenSearch / Kafka ingestion (`IntegrationService`, webhooks).  
- ML model or dedicated phishing microservice behind `PhishingRuleEngine`.  
- PDF export, notifications, SSO/OIDC.  
- Stronger JWT rotation, refresh tokens, audit trail.
