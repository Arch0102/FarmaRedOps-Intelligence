# Docker local/dev

Esta configuracion levanta el backend Spring Boot y PostgreSQL para desarrollo local.
Las credenciales incluidas en `docker-compose.yml` son solo para local/dev.

## Servicios

- `postgres`: PostgreSQL 16 con base `farmared_db`.
- `backend`: API Spring Boot expuesta en `http://localhost:8080`.

## Ejecutar

```bash
docker compose up --build
```

## Variables usadas por el backend

```text
DB_URL=jdbc:postgresql://postgres:5432/farmared_db
DB_USER=postgres
DB_PASSWORD=Farmared123
SERVER_PORT=8080
JWT_SECRET=farmared_dev_jwt_secret_12345678901234567890
JWT_EXPIRATION_MS=3600000
DOCUMENT_STORAGE_PATH=/app/uploads/documentos
DOCUMENT_MAX_SIZE_BYTES=10485760
```

## Detener

```bash
docker compose down
```

Para borrar volumenes locales de desarrollo:

```bash
docker compose down -v
```
