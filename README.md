# rtm-iam

Servicio **IAM** de RentaMovil: registro, login, emisión de JWT (RS256) y permisos por rol.
Documentación del servicio: `rtm-docs/09-microservices/services/02-iam/`.

| | |
|---|---|
| Stack | Java 21 · Spring Boot 4.1 · Maven · arquitectura hexagonal |
| Puerto local | 3001 |
| Base de datos | SQL Server `rentamovil`, schema `iam`, usuario `iam_app` |
| Migraciones | **no están aquí** — las maneja `rtm-iam-db` (Liquibase) |

## Estructura (hexagonal)

```
src/main/java/com/rentamovil/iam/
├── domain/                   Reglas del negocio. No usa Spring ni JPA
│   ├── entity/                 Person, User, Role
│   ├── enums/                  UserStatus
│   ├── exception/              ResourceAlreadyExistsException, ...
│   └── port/
│       ├── in/                 casos de uso (RegisterUserUseCase, ...)
│       └── out/                lo que el dominio necesita (UserRepositoryPort, PasswordHasherPort, ...)
├── application/usecase/      Implementación de los casos de uso (RegisterUserUseCaseImpl, ...)
└── infrastructure/           Adaptadores
    ├── config/                 BeanConfig: conecta casos de uso con adaptadores
    ├── persistence/
    │   ├── entity/             entidades JPA (tablas del schema iam)
    │   ├── repository/         Spring Data
    │   └── adapter/            implementan los *RepositoryPort
    ├── security/               BCrypt y claves JWT
    └── web/
        ├── controller/         AuthController, JwksController, HealthController
        ├── dto/                requests y responses
        └── exception/          GlobalExceptionHandler
```

Ver `rtm-docs/05-architecture/hexagonal-architecture.md`.

## El servicio no toca el esquema

`spring.jpa.hibernate.ddl-auto=validate` — Hibernate solo **verifica** al arrancar que las
entidades coincidan con las tablas creadas por `rtm-iam-db`, y falla si no coinciden. Nunca crea
ni modifica tablas. Un cambio de esquema = un changeset en `rtm-iam-db`, primero.

## Requisitos

- Java 21+ (se compila para 21; corre en cualquier JDK ≥ 21)
- Maven 3.9+
- SQL Server local levantado y migrado desde `rtm-iam-db` (`docker compose up -d` + `mvn liquibase:update`)

## Uso local

```bash
# 1. Configuración (una sola vez)
cp .env.example .env
# DATABASE_PASSWORD = el parameter.iamAppPassword de rtm-iam-db/liquibase.properties

# 2. Tests
mvn test

# 3. Levantar el servicio
mvn spring-boot:run

# 4. Verificar
curl http://localhost:3001/health
# {"status":"ok","db":"connected","timestamp":"..."}
```

`/health` responde `503` con `"db":"disconnected"` si la base deja de responder.

## Endpoints

| Método | Ruta | Auth | Historia | Respuestas |
|---|---|---|---|---|
| GET | `/health` | — | — | 200 / 503 |
| GET | `/jwks` | — | — | 200 (clave pública) |
| POST | `/register` | — | HU-IAM-001 | 201 (tokens, queda logueado) · 400 `VALIDATION_ERROR` · 409 `EMAIL_ALREADY_EXISTS` / `USERNAME_ALREADY_EXISTS` |
| POST | `/login` | — | HU-IAM-002 | 200 (tokens) · 401 `INVALID_CREDENTIALS` · 423 `ACCOUNT_BLOCKED` |
| POST | `/refresh` | — | — | 200 (tokens nuevos) · 401 `INVALID_TOKEN` |
| POST | `/logout` | Bearer | — | 204 · 401 `INVALID_TOKEN` |
| GET | `/users/me` | Bearer | HU-IAM-004 | 200 (perfil) · 401 |
| PATCH | `/users/me` | Bearer | HU-IAM-004 | 200 · 400 — cambia `firstName`, `lastName`, `phone` (lo que no venga no se toca) |
| PATCH | `/users/me/email` | Bearer | HU-IAM-004 | 200 · 401 `INCORRECT_PASSWORD` · 409 `EMAIL_ALREADY_EXISTS` |

| GET | `/users?search=` | Bearer + `users:read` | HU-IAM-005 | 200 · 403 `FORBIDDEN` |
| PATCH | `/users/{id}/role` | Bearer + `roles:grant` | HU-IAM-005 | 200 · 403 · 404 · 409 `CANNOT_CHANGE_OWN_ROLE` |
| PATCH | `/users/{id}/status` | Bearer + `users:update-status` | HU-IAM-005 | 200 · 400 · 403 · 404 · 409 `INVALID_STATUS_TRANSITION` |

Estados: solo `BLOCKED → ACTIVE` (desbloquear) y `ACTIVE → INACTIVE` (desactivar). Cambios de rol y
de estado quedan en `shared.audit` (`ROLE_GRANTED`, `USER_UNBLOCKED`, `USER_DEACTIVATED`).

Los endpoints con Bearer reciben al usuario con `@CurrentUser AuthenticatedUser` (ver
`infrastructure/web/auth`): el id sale del token, nunca de la URL.

Refresh: cada refresh token sirve **una sola vez** (rotación). Si llega uno ya usado, se revocan
todas las sesiones del usuario (posible robo). Logout: `{"refreshToken": "...", "allDevices": false}`;
el access token no se puede anular, vence solo a los 30 min.

Login: `{"identifier": "<email o username>", "password": "..."}`. Al 5.º intento fallido seguido
la cuenta queda `BLOCKED` y solo un `SUPER_ADMIN` la desbloquea. El contador de intentos está en
memoria (se reinicia con el servicio); la doc lo pone en Redis, se cambia solo el adaptador
`InMemoryLoginAttemptAdapter`.

```bash
curl -X POST http://localhost:3001/register -H "Content-Type: application/json" \
  -d '{"firstName":"María","lastName":"García","email":"maria@example.com","username":"mgarcia","password":"SecurePass123!"}'
```

## Claves JWT (RS256)

`iam` firma los tokens con una **clave privada** que solo él tiene, y publica la **clave
pública** en `GET /jwks` para que el gateway y los demás servicios los verifiquen
(`rtm-docs/07-api/authentication.md`).

Generar un par para tu entorno local (Git Bash; OpenSSL viene con Git):

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out priv.pem
openssl pkey -in priv.pem -pubout -out pub.pem

# Copia a .env el contenido SIN las líneas -----BEGIN/END----- y en una sola línea:
sed '/-----/d' priv.pem | tr -d '\r\n'   # → APP_IAM_JWT_PRIVATE_KEY=
sed '/-----/d' pub.pem  | tr -d '\r\n'   # → APP_IAM_JWT_PUBLIC_KEY=

rm priv.pem pub.pem                      # no dejes las claves sueltas en el disco
```

El servicio **no arranca** si las claves faltan, no son válidas, miden menos de 2048 bits o no
son un par. Cada entorno tiene su propio par: nunca se reutilizan ni se suben al repo.

```bash
curl http://localhost:3001/jwks
# {"keys":[{"kty":"RSA","e":"AQAB","use":"sig","kid":"…","alg":"RS256","n":"…"}]}
```

## Ramas

`feat/`, `fix/`, `chore/` → PR a `develop` (Squash and Merge). Ver
`rtm-docs/00-governance/git-conventions.md`.
