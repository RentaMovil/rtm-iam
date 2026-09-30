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
├── domain/            Modelo y puertos. Sin Spring, sin JPA: Java puro
│   ├── model/
│   └── port/
│       ├── in/          casos de uso (interfaces)
│       └── out/         lo que el dominio necesita de afuera (repositorios, etc.)
├── application/       Implementación de los casos de uso
│   └── usecase/
└── infrastructure/    Adaptadores: todo lo que depende de un framework
    ├── web/             controllers REST
    ├── persistence/     entidades JPA y repositorios
    └── config/          wiring de Spring
```

Regla: `domain` no importa nada de `org.springframework.*` ni `jakarta.persistence.*`. Ver
`rtm-docs/05-architecture/hexagonal-architecture.md` y `rtm-docs/_stacks/java-spring.md`.

## El servicio no toca el esquema

`spring.jpa.hibernate.ddl-auto: validate` — Hibernate solo **verifica** al arrancar que las
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
