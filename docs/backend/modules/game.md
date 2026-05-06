# Módulo: Juego singleplayer

Paquete raíz: `com.versus.api.game`  
Depende de: `match`, `questions`, `stats`  
Estado: ✅ implementado (Sprint 1-2)

---

## Responsabilidad

Implementa los dos modos de juego para un solo jugador: **Survival** (preguntas BINARY, vidas) y **Precision** (preguntas NUMERIC, daño por desviación). Crea y gestiona la sesión de partida (`Match` + `MatchPlayer`) y delega al módulo `stats` al finalizar.

---

## Modos de juego

### Survival
- Preguntas de tipo `BINARY`
- 3 vidas iniciales (`SURVIVAL_INITIAL_LIVES = 3`)
- Acierto: +0 vidas, puntos = `50 * streak_actual`
- Fallo: −1 vida
- Fin: vidas = 0

### Precision
- Preguntas de tipo `NUMERIC`
- 100 puntos de vida iniciales (`PRECISION_INITIAL_LIVES = 100`)
- Puntuación basada en desviación porcentual respecto al valor correcto
- Fallo total si la desviación supera el umbral definido (`tolerancePercent`)
- Fin: vidas ≤ 0

> ⚠️ **TODO #59:** La fórmula exacta de daño para Precision está pendiente de confirmación con el equipo. La implementación actual es aproximada.

---

## Diagrama de clases

```mermaid
classDiagram
    class SurvivalController {
        <<RestController /api/game/survival>>
        <<RequiresAuth>>
        +POST /start : SurvivalStartResponse
        +POST /answer : SurvivalAnswerResponse
    }

    class PrecisionController {
        <<RestController /api/game/precision>>
        <<RequiresAuth>>
        +POST /start : PrecisionStartResponse
        +POST /answer : PrecisionAnswerResponse
    }

    class GameService {
        <<Service>>
        -MatchRepository matchRepo
        -MatchPlayerRepository playerRepo
        -MatchRoundRepository roundRepo
        -MatchAnswerRepository answerRepo
        -QuestionService questionService
        -StatsService statsService
        +startSurvival(UUID userId) SurvivalStartResponse
        +answerSurvival(UUID userId, SurvivalAnswerRequest) SurvivalAnswerResponse
        +startPrecision(UUID userId) PrecisionStartResponse
        +answerPrecision(UUID userId, PrecisionAnswerRequest) PrecisionAnswerResponse
        -loadSession(UUID matchId, UUID userId) MatchPlayer
        -createRound(UUID matchId, UUID questionId, int roundNum) MatchRound
        -createAnswer(MatchRound, UUID userId, ...) MatchAnswer
        -finishMatch(Match, MatchPlayer) void
        -averageDeviation(UUID matchId, UUID userId) double
    }

    class SurvivalAnswerRequest {
        <<DTO>>
        +UUID matchId
        +UUID questionId
        +UUID selectedOptionId
    }

    class SurvivalAnswerResponse {
        <<DTO>>
        +boolean correct
        +int lifeDelta
        +int newLives
        +int score
        +int streak
        +boolean gameOver
        +QuestionResponse nextQuestion
    }

    class PrecisionAnswerRequest {
        <<DTO>>
        +UUID matchId
        +UUID questionId
        +double userValue
    }

    class PrecisionAnswerResponse {
        <<DTO>>
        +double deviation
        +int lifeDelta
        +int newLives
        +double correctValue
        +boolean gameOver
        +QuestionResponse nextQuestion
    }

    SurvivalController --> GameService : delega
    PrecisionController --> GameService : delega
    GameService ..> SurvivalAnswerRequest : consume
    GameService ..> SurvivalAnswerResponse : produce
    GameService ..> PrecisionAnswerRequest : consume
    GameService ..> PrecisionAnswerResponse : produce
```

---

## Flujo de una partida Survival

```mermaid
sequenceDiagram
    participant C as Cliente
    participant SC as SurvivalController
    participant GS as GameService
    participant QS as QuestionService
    participant DB as PostgreSQL

    C->>SC: POST /api/game/survival/start
    SC->>GS: startSurvival(userId)
    GS->>DB: INSERT match (mode=SURVIVAL, status=IN_PROGRESS)
    GS->>DB: INSERT match_player (lives=3, score=0, streak=0)
    GS->>QS: findRandomActiveQuestion(BINARY)
    GS->>DB: INSERT match_round (round=1, questionId)
    GS-->>C: {matchId, sessionId, question (sin isCorrect)}

    loop Mientras vidas > 0
        C->>SC: POST /api/game/survival/answer {matchId, questionId, selectedOptionId}
        SC->>GS: answerSurvival(userId, request)
        GS->>DB: cargar MatchPlayer (validar ownership)
        GS->>DB: cargar MatchRound actual
        GS->>DB: cargar QuestionOption (verificar isCorrect)
        alt Respuesta correcta
            GS->>DB: UPDATE match_player (streak++, score += 50*streak)
            GS->>DB: INSERT match_answer (isCorrect=true, lifeDelta=0)
        else Respuesta incorrecta
            GS->>DB: UPDATE match_player (lives--, streak=0)
            GS->>DB: INSERT match_answer (isCorrect=false, lifeDelta=-1)
        end
        alt Vidas > 0
            GS->>QS: findRandomActiveQuestion(BINARY)
            GS->>DB: INSERT match_round (round++)
            GS-->>C: {correct, lifeDelta, score, streak, nextQuestion}
        else Vidas = 0
            GS->>DB: UPDATE match (status=FINISHED, finishedAt)
            GS->>StatsService: recordFinishedGame(userId, SURVIVAL, rounds, ...)
            GS-->>C: {correct, lifeDelta, score, gameOver=true}
        end
    end
```

---

## Cálculo de puntuación Survival

```
Acierto:  score += 50 × streak_actual
          streak++
          lifeDelta = 0

Fallo:    score += 0
          streak = 0
          lives--
          lifeDelta = -1
```

Ejemplo de partida: `[✓✓✓✗✓✓]` → puntuaciones acumuladas: `50, 150, 300, 0, 50, 150`

---

## Cálculo de desviación Precision

```
desviación = |respuesta_usuario - correctValue| / correctValue × 100

Si desviación ≤ tolerancePercent (default 5%):
    lifeDelta = 0  (acierto perfecto / dentro del margen)

Si desviación > tolerancePercent:
    lifeDelta = -round(desviación)  ← pendiente confirmación TODO#59
```

---

## Endpoints

### Survival

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| `POST` | `/api/game/survival/start` | — | `SurvivalStartResponse` |
| `POST` | `/api/game/survival/answer` | `SurvivalAnswerRequest` | `SurvivalAnswerResponse` |

### Precision

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| `POST` | `/api/game/precision/start` | — | `PrecisionStartResponse` |
| `POST` | `/api/game/precision/answer` | `PrecisionAnswerRequest` | `PrecisionAnswerResponse` |

### Errores comunes

| Situación | ErrorCode | HTTP |
|---|---|---|
| Match no encontrado o no pertenece al usuario | `NOT_FOUND` / `FORBIDDEN` | 404 / 403 |
| Partida ya finalizada | `CONFLICT` | 409 |
| No hay preguntas disponibles | `NOT_FOUND` | 404 |

---

## Relación con otros módulos

```mermaid
graph LR
    GS[GameService] -->|crea/actualiza| Match
    GS -->|crea/actualiza| MatchPlayer
    GS -->|crea| MatchRound
    GS -->|crea| MatchAnswer
    GS -->|obtiene preguntas| QuestionService
    GS -->|registra estadísticas| StatsService
```

`GameService` es el único componente que escribe en `Match*` durante el juego singleplayer. El futuro multiplayer tendrá su propio service con WebSocket.

---

## Extensión futura

- Los modos **Binary Duel**, **Precision Duel** y **Sabotage** usan las mismas entidades `Match*` pero se orquestan mediante WebSocket (Sprint 3).
- Añadir un endpoint `GET /api/game/{matchId}/summary` para historial de partida.
- Considerar SSE (Server-Sent Events) para un timer server-side en modos con tiempo límite.
