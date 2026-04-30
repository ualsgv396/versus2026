# Versus — Contexto del proyecto

> Estado actual del MVP de Versus (juego de preguntas multijugador). Este documento resume **qué está hecho y qué falta** en backend y frontend, branch a branch. Mantenlo actualizado al cerrar cada sprint.

Última actualización: **2026-04-30** · branch activo: `backend`

---

## TL;DR

- **Backend Sprint 1 + 2** completos: Auth + Users + Questions + Survival + Precision singleplayer + Stats básicos. Tests de integración pasan. Seed dev disponible.
- **Frontend** cableado al backend en este branch: HttpClient + AuthService + interceptor JWT + guards + servicios HTTP. Login/register/Survival/Precision/Dashboard/Profile/Result consumen la API real. Build (`ng build`) verde.
- **Pendiente**: Sprints 3 (multijugador WebSocket) y 4 (ranking, history detallado, moderación, admin, scraping). Frontend de admin y multiplayer aún con mocks.

---

## Stack

| Capa | Tecnología |
|---|---|
| Frontend | Angular 21 (standalone components, signals), SCSS con tokens `--vs-` |
| Backend | Spring Boot 4.0.5, Java 25, JPA, Lombok, JWT (JJWT), springdoc OpenAPI |
| DB | PostgreSQL 18 (H2 sólo en tests) |
| Realtime | `spring-boot-starter-websocket` añadido (no usado todavía) |
| Dev infra | Docker Compose (`docker-compose.yml` + `docker-compose.dev.yml`), VS Code Dev Containers |
| Puertos | 4200 Angular · 8080 API · 5432 Postgres · 5050 pgAdmin · 5005 debug |

Variables relevantes (`.env.example`): `DB_URL`, `DB_USER`, `DB_PASS`, `JWT_SECRET`, `JWT_EXPIRY`, `JWT_REFRESH_EXPIRY`.

---

## Backend — qué está hecho

Estructura en `backend/src/main/java/com/versus/api/`:

```
ApiApplication.java
config/      SecurityConfig, JwtAuthFilter wired, CORS para http://localhost:4200, DevSeedConfig
common/      ErrorCode, ApiException, GlobalExceptionHandler, ErrorResponse
auth/        AuthController, AuthService, JwtService, RefreshToken entity+repo, DTOs
users/       UserController, UserService, User entity, UserRepository, DTOs
questions/   QuestionController, QuestionService, Question + QuestionOption entities, DTOs (BINARY/NUMERIC con @JsonTypeInfo)
game/        SurvivalController, PrecisionController, GameService, DTOs
match/       Match, MatchPlayer (composite @EmbeddedId), MatchRound, MatchAnswer entities (sólo modelado, sin endpoints)
stats/       StatsController, StatsService, PlayerStats entity, DTOs
moderation/  entidades QuestionReport (sólo modelado)
scraping/    entidades Spider, SpiderRun (sólo modelado)
```

### Sprint 1 — Auth + Users (Issues #39, #84, #85)
- ✅ Entidades + repos JPA con todos los cambios de schema (composite key en `match_players`, `questions.unit/correct_value/tolerance_percent`, `match_answers.is_correct`, `users.updated_at/is_active`, tabla `refresh_tokens`).
- ✅ Enums: `Role`, `QuestionType`, `QuestionStatus`, `GameMode`, `MatchStatus`, `MatchResult`, `ReportStatus`, `SpiderStatus`.
- ✅ Manejo global de errores formato `{error, message, status}`.
- ✅ `JwtService` (access 15 min + refresh 7 días persistido como hash, rotación en `/refresh`).
- ✅ `JwtAuthFilter` + `SecurityConfig` (stateless, BCrypt, CORS abierto a `http://localhost:4200`).
- ✅ Endpoints:
  - `POST /api/auth/register|login|refresh|logout`
  - `GET/PUT /api/users/me`, `GET /api/users/{id}`
- ✅ Tests: `AuthFlowIntegrationTest` (registro, login, refresh, /me con/sin token).

### Sprint 2 — Questions + Singleplayer + Stats (Issues #41, #42, #43, #53, #55, #56, #59, #60, #77)
- ✅ `GET /api/questions/{random,{id},categories}` — nunca expone `correct_value` ni `is_correct`.
- ✅ Survival singleplayer: 3 vidas, `streak * 50` puntos, finaliza Match al llegar a 0.
- ✅ Precision singleplayer: 100 vidas, fórmula `dev = |v-correct|/correct*100` con `lifeDelta` `+5/0/-min(50,round(dev))`. Marcador `// TODO(#59): confirmar fórmula con el equipo` en `GameService`.
- ✅ `POST /api/game/{survival,precision}/{start,answer}` validan que `sessionId` pertenece al usuario y está `IN_PROGRESS`.
- ✅ Upsert de `PlayerStats` al finalizar partida (gamesPlayed, gamesWon, bestStreak, currentStreak, avgDeviation para PRECISION).
- ✅ `GET /api/stats/me` y `GET /api/stats/me?mode=...`. `winRate` calculado en DTO.
- ✅ Seed dev (`DevSeedConfig`, profile `dev`, gated por `versus.seed.enabled=true`):
  - 3 usuarios: `player@versus.com/player123`, `moderator@versus.com/moderator123`, `admin@versus.com/admin123`
  - 15 preguntas BINARY (football, geography, cinema)
  - 10 preguntas NUMERIC con `unit`/`correct_value`/`tolerance_percent`
- ✅ Tests: `SingleplayerGameIntegrationTest`.

### Nits conocidos (no bloquean MVP)
- `GameService.averageDeviation` usa `matchRounds.findAll()` en vez de `findByMatchId(...)` → O(N) sobre todos los rounds. Sustituir por query indexada.
- `StatsController.mine` devuelve `Object` (rompe tipado OpenAPI). Unificar en un único response DTO con union de modos.
- Falta seguir el TODO(#59) y consensuar la fórmula final de Precisión.

---

## Backend — qué falta

### Sprint 3 — Multijugador WebSocket (`docs/backend-mvp-sprint-3.md`)
- ❌ STOMP sobre SockJS (`ws://localhost:8080/ws`).
- ❌ Endpoints REST de match: crear sala privada con código, unirse, abandonar, listar partidas.
- ❌ Matchmaking automático (`matchmaking_queue`).
- ❌ Lógica PvP: Duelo Binario, Duelo de Precisión, Sabotaje.
- ❌ Eventos WebSocket: `match:start`, `round:question`, `round:answered`, `match:end`, `ERROR`.
- ❌ Suscripciones: `/user/queue/match`, `/topic/match/{matchId}`. Envío: `/app/match/answer`, `/app/match/ready`.

### Sprint 4 — Ranking + History + Moderación + Admin (`docs/backend-mvp-sprint-4.md`)
- ❌ `GET /api/rankings?mode=...` paginado, `GET /api/rankings/me`.
- ❌ `GET /api/users/me/history` paginado con detalle por partida.
- ❌ `POST /api/questions/{id}/report`, `GET /api/moderation/reports` (rol MODERATOR).
- ❌ Panel admin: `GET /api/admin/users`, suspender/reactivar, cambiar rol.
- ❌ Endpoints REST de scrapers (`/api/admin/spiders`, runs, ingest de preguntas en `PENDING_REVIEW`).
- ❌ Decoradores `@PreAuthorize("hasRole('MODERATOR')")` / `hasRole('ADMIN')`.
- ❌ Refactor `StatsController.mine` a respuesta tipada.

### Otros
- ❌ `application-prod.properties` con `ddl-auto=validate` aún sin verificar contra una DB de prod.
- ❌ Migraciones reales (Flyway/Liquibase). Hoy `ddl-auto=create-drop` en dev.
- ❌ Pipeline Scrapy independiente (otra carpeta del monorepo, no implementada).

---

## Frontend — qué está hecho

Estructura en `frontend/src/app/`:

```
app.config.ts        provideHttpClient(withInterceptors([authInterceptor]))
app.routes.ts        rutas con canActivate: [authGuard] y [authGuard, adminGuard]
core/
  models/            auth.models.ts, game.models.ts (interfaces espejo de los DTOs backend)
  services/          auth.service.ts (login/register/refresh/logout, signals user/isAuthenticated, persistencia localStorage)
                     user.service.ts, question.service.ts, game.service.ts, stats.service.ts
  interceptors/      auth.interceptor.ts (Bearer + retry 401 con refresh + redirige a /login si refresh falla)
  guards/            authGuard, adminGuard
features/
  auth/              login-form, register-form (cableados a AuthService, mapeo de errores UNAUTHORIZED/CONFLICT/VALIDATION_ERROR)
  player/
    dashboard/       username + stats agregadas (totalGames, winRate, bestStreak) desde /api/stats/me
    profile/         /api/users/me + tabla por modo desde /api/stats/me
    mode-select/     Survival → /play/survival, Precision → /play/precision (resto siguen a /play/lobby)
    result/          lee history.state {mode, score, bestStreak, rounds, won}
    lobby/           sin tocar (multiplayer)
  survival/          flow real /api/game/survival/{start,answer}, UI BINARY (2 botones), feedback animado, gameOver → /play/result
  precision/         NUEVO: input numérico + barra de vidas 0-100, /api/game/precision/{start,answer}
  landing/           sin cambios
  admin/             dashboard, spiders, reports, users — todos con mocks
shared/components/   vs-button, vs-card, vs-input, vs-badge, vs-divider, layout/topbar (datos por defecto estáticos)
environments/        environment.ts con apiBaseUrl=http://localhost:8080/api
```

### Verificado
- `ng build` pasa (warnings de presupuesto SCSS en `landing.scss` y `survival.scss`, no errores).
- Tokens del style guide intactos (`--vs-` prefix, fonts Bebas Neue / IBM Plex Mono / Inter).
- Auth interceptor reintenta automáticamente con refresh token al recibir 401.

---

## Frontend — qué falta

### Multijugador (espera Sprint 3 backend)
- ❌ Cliente STOMP/SockJS (`@stomp/stompjs`, `sockjs-client` no añadidos al `package.json`).
- ❌ Servicio de match con suscripciones a `/user/queue/match` y `/topic/match/{matchId}`.
- ❌ Páginas reales para Lobby, Duelo Binario, Duelo de Precisión, Sabotaje. Hoy todo apunta a `/play/lobby` que está mockeado.
- ❌ Matchmaking UI (cola, código privado).

### Datos no expuestos todavía por backend
- ❌ Dashboard: actividad reciente + ranking semanal (depende de Sprint 4: history + ranking).
- ❌ Profile: categorías favoritas, logros, historial reciente (mismo motivo).
- ❌ Topbar: pasarle el user real (`AuthService.user()`) en vez del default estático `{ name: 'aritzz92', xp: 4280 }`.

### Admin (espera Sprint 4 backend)
- ❌ `features/admin/dashboard|spiders|reports|users` siguen totalmente mockeados.
- ❌ Cliente para `/api/admin/users`, `/api/moderation/reports`, `/api/admin/spiders`.

### Higiene
- ❌ Logout UI (botón en topbar que llame a `AuthService.logout()`).
- ❌ Tests unitarios de los servicios del core (vitest disponible, no escritos).
- ❌ `AuthGuard` aún no comprueba que el access token no esté expirado antes de cargar rutas — confía en el 401 → refresh del interceptor (suficiente, pero un check temprano evita parpadeo).
- ❌ Manejo de errores de red más amable (toast/notificación global) — hoy se renderiza inline por componente.

---

## Cómo arrancar

El stack de dev levanta **todo** en Docker (db + backend + frontend + pgAdmin) con hot reload. No hace falta `npm start` ni `mvn` en el host.

```bash
# 1. Variables de entorno
cp .env.example .env

# 2. Stack completo
docker compose -f docker-compose.yml -f docker-compose.dev.yml up
```

Servicios expuestos:
- Frontend → http://localhost:4200 (Angular `ng serve` con `--poll 500`)
- Backend  → http://localhost:8080 (Spring Boot dev tools, debug en 5005)
- Postgres → localhost:5432
- pgAdmin  → http://localhost:5050

> La **primera vez** el contenedor `frontend` tarda 1–2 min haciendo `npm install` dentro del volumen `node_modules`. Espera a ver el log `Local: http://localhost:4200/` antes de abrir el navegador.

Si ya tienes el front corriendo local y sólo quieres backend+db:
```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up db backend
```

Login de prueba (seed dev): `player@versus.com` / `player123`.

Tests (también dentro de Docker o en local):
```bash
docker compose exec backend  ./mvnw test
docker compose exec frontend ng test
# o local:
cd backend  && ./mvnw test
cd frontend && ng test
```

---

## Documentos canónicos

| Documento | Contenido |
|---|---|
| `docs/guia-de-coordinación-técnica.md` | **Fuente de verdad** del contrato API + eventos WebSocket + códigos de error |
| `docs/bd-scheme.md` | Schema final de la DB (Mermaid) tras Sprints 1-2 |
| `docs/backend-mvp-sprint-1-2.md` | Briefing Sprints 1+2 — completado |
| `docs/backend-mvp-sprint-3.md` | Briefing Sprint 3 — pendiente |
| `docs/backend-mvp-sprint-4.md` | Briefing Sprint 4 — pendiente |
| `docs/style-guide.md` | Tokens, clases `vs-*`, animaciones, fuentes |
| `CLAUDE.md` (raíz) | Reglas y arquitectura para asistentes Claude Code |

---

## Glosario rápido de contratos clave

**Auth response** (mismo shape para `register`, `login`, `refresh`):
```json
{ "accessToken": "...", "refreshToken": "...", "user": { "id": "...", "username": "...", "role": "PLAYER", "avatarUrl": null } }
```

**Error response** (toda la API):
```json
{ "error": "UNAUTHORIZED", "message": "...", "status": 401 }
```

**Question polimórfica** (discriminator `type`):
```json
// BINARY
{ "type": "BINARY", "id": "...", "text": "...", "category": "football", "options": [{ "id": "...", "text": "..." }, ...], "scrapedAt": null }
// NUMERIC
{ "type": "NUMERIC", "id": "...", "text": "...", "category": "football", "unit": "minutes", "scrapedAt": null }
```

**Survival answer response**:
```json
{ "correct": true, "livesRemaining": 3, "lifeDelta": 0, "streak": 4, "scoreDelta": 200, "nextQuestion": { ... }, "gameOver": false }
```

**Precision answer response**:
```json
{ "correctValue": 90, "deviation": 4.5, "deviationPercent": 4.5, "lifeDelta": 5, "livesRemaining": 95, "nextQuestion": { ... }, "gameOver": false }
```
