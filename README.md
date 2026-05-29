# FarmaRedOps Intelligence

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![React](https://img.shields.io/badge/React-18-blue)
![Vite](https://img.shields.io/badge/Vite-6-purple)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue)
![Docker](https://img.shields.io/badge/Docker-ready-blue)
![Build](https://img.shields.io/badge/build-passing-brightgreen)
![License](https://img.shields.io/badge/license-Academic%20Project-lightgrey)

FarmaRedOps Intelligence es una plataforma full stack para apoyar la operacion de una red farmaceutica. Integra backend Spring Boot, frontend React/Vite, PostgreSQL, autenticacion JWT, inventario, Kardex, alertas, dashboard analitico, documentos, Docker Compose y datos demo automaticos.

## Tabla De Contenido

- [Capturas](#capturas)
- [Descripcion Del Proyecto](#descripcion-del-proyecto)
- [Caracteristicas Principales](#caracteristicas-principales)
- [Roles](#roles)
- [Arquitectura](#arquitectura)
- [Tecnologias](#tecnologias)
- [Ejecucion Con Docker](#ejecucion-con-docker)
- [Comandos Utiles](#comandos-utiles)
- [Ejecucion Local Sin Docker](#ejecucion-local-sin-docker)
- [Endpoints Principales](#endpoints-principales)
- [Pruebas](#pruebas)
- [Evidencia De Calidad](#evidencia-de-calidad)
- [Equipo](#equipo)

## Capturas

Agregar las capturas de la aplicacion en estas rutas:

| Vista | Ruta |
| --- | --- |
| Login | `docs/screenshots/login.png` |
| Dashboard | `docs/screenshots/dashboard.png` |
| Inventario | `docs/screenshots/inventario.png` |
| Kardex | `docs/screenshots/kardex.png` |
| Centros | `docs/screenshots/centros.png` |

Flujo sugerido: iniciar el stack con Docker, entrar con `admin / admin123`, abrir cada modulo, guardar las imagenes PNG en `docs/screenshots/` y subirlas junto con este README.

## Descripcion Del Proyecto

FarmaRedOps Intelligence centraliza procesos operativos de una red farmaceutica: autenticacion, usuarios por rol, medicamentos, categorias, centros de distribucion, inventario, movimientos de stock, Kardex, ordenes de compra, proveedores, alertas de stock, dashboard analitico y documentos.

El proyecto esta preparado para ejecutarse desde un clone limpio:

- PostgreSQL corre en Docker.
- El backend Spring Boot corre en Docker y queda publicado en `8081`.
- El frontend React/Vite se compila y se sirve con Nginx en `5173`.
- La base demo se carga automaticamente con `DemoDataInitializer` usando el perfil `docker`.

## Caracteristicas Principales

- Autenticacion JWT.
- Seguridad por roles.
- CRUD de medicamentos, categorias y centros de distribucion.
- Inventario, movimientos y Kardex.
- Dashboard analitico con metricas operativas.
- Alertas de stock critico y punto de reorden.
- Gestion de proveedores y ordenes de compra.
- Carga, descarga y generacion de documentos PDF.
- Docker full stack con PostgreSQL, backend y frontend.
- Datos demo automaticos.
- Pruebas unitarias relevantes para servicios de negocio.

## Roles

| Rol | Descripcion |
| --- | --- |
| `ROLE_ADMIN_AUDITOR` | Administra catalogos, usuarios, dashboard, auditoria y supervision operativa. |
| `ROLE_AUXILIAR_BODEGA` | Opera medicamentos, inventario, movimientos y Kardex. |
| `ROLE_ANALISTA_COMPRAS` | Gestiona proveedores, ordenes de compra, abastecimiento y analitica. |

Usuarios demo:

| Usuario | Password | Rol |
| --- | --- | --- |
| `admin` | `admin123` | `ROLE_ADMIN_AUDITOR` |
| `auxiliar` | `auxiliar123` | `ROLE_AUXILIAR_BODEGA` |
| `compras` | `compras123` | `ROLE_ANALISTA_COMPRAS` |

## Arquitectura

El backend usa arquitectura por capas:

```text
src/main/java/com/farmared/opsintelligence
|-- config
|-- controller
|-- dto
|   |-- request
|   `-- response
|-- entity
|   `-- enums
|-- exception
|-- mapper
|-- repository
|-- security
|-- service
|   `-- impl
`-- FarmaRedOpsIntelligenceApplication.java
```

Responsabilidad de cada capa:

| Capa | Responsabilidad |
| --- | --- |
| `controller` | Expone endpoints REST y respuestas HTTP. |
| `service` | Define contratos de negocio. |
| `service.impl` | Implementa reglas de negocio. |
| `repository` | Acceso a datos con Spring Data JPA. |
| `entity` | Modelo JPA y relaciones de base de datos. |
| `dto.request` | Payloads de entrada y validaciones. |
| `dto.response` | Modelos de salida de la API. |
| `mapper` | Conversion Entity/DTO con MapStruct. |
| `security` | JWT, autenticacion y autorizacion. |
| `config` | Configuracion, CORS y datos demo. |

## Tecnologias

Backend:

- Java 21
- Spring Boot 3.5.x
- Spring Web
- Spring Security
- JWT con JJWT
- Spring Data JPA
- Hibernate
- MapStruct
- Lombok
- PostgreSQL
- Maven
- JUnit 5
- Mockito

Frontend:

- React 18
- Vite 6
- Axios
- React Router
- Recharts
- Lucide React
- CSS propio

Infraestructura:

- Docker
- Docker Compose
- PostgreSQL 17 Alpine
- Nginx para servir el build productivo del frontend

## Ejecucion Con Docker

Forma recomendada:

```bash
git clone https://github.com/Arch0102/FarmaRedOps-Intelligence.git
cd FarmaRedOps-Intelligence
docker compose up --build
```

Servicios disponibles:

| Servicio | URL |
| --- | --- |
| Frontend | http://localhost:5173 |
| Backend | http://localhost:8081 |
| PostgreSQL Docker | `localhost:5433` |

El frontend se compila con:

```text
VITE_API_URL=http://localhost:8081/api/v1
```

La base PostgreSQL se crea en el contenedor y los datos demo se insertan automaticamente al iniciar el backend con el perfil `docker`.

## Comandos Utiles

```bash
docker compose up --build
docker compose down
docker compose down -v
docker compose logs backend
docker compose logs frontend
docker compose ps
```

Usar `docker compose down -v` cuando se quiera eliminar el volumen de PostgreSQL y recargar la base demo desde cero en el siguiente arranque.

## Ejecucion Local Sin Docker

Docker es la forma recomendada porque levanta PostgreSQL, backend, frontend y datos demo con un solo comando.

Para ejecutar el backend localmente sin Docker:

1. Crear una base PostgreSQL llamada `farmared_db`.
2. Configurar variables de entorno:

```text
DB_URL=jdbc:postgresql://localhost:5432/farmared_db
DB_USER=postgres
DB_PASSWORD=tu_password
SERVER_PORT=8080
JWT_SECRET=clave_jwt_de_desarrollo_con_minimo_32_caracteres
JWT_EXPIRATION_MS=3600000
```

3. Iniciar el backend:

```powershell
.\mvnw.cmd spring-boot:run
```

4. Iniciar el frontend:

```bash
cd frontend
npm install
npm run dev
```

Para desarrollo local con Vite, conservar en `frontend/.env`:

```text
VITE_API_BASE_URL=http://localhost:8081/api/v1
```

## Endpoints Principales

Autenticacion:

| Metodo | Endpoint |
| --- | --- |
| `POST` | `/api/v1/auth/login` |
| `POST` | `/api/v1/auth/register` |

Catalogos:

| Metodo | Endpoint |
| --- | --- |
| `GET` | `/api/v1/medicamentos` |
| `GET` | `/api/v1/medicamentos/{id}` |
| `POST` | `/api/v1/medicamentos` |
| `PUT` | `/api/v1/medicamentos/{id}` |
| `DELETE` | `/api/v1/medicamentos/{id}` |
| `GET` | `/api/v1/categorias-medicamento` |
| `GET` | `/api/v1/centros-distribucion` |

Inventario y Kardex:

| Metodo | Endpoint |
| --- | --- |
| `GET` | `/api/v1/inventarios` |
| `GET` | `/api/v1/inventarios/resumen` |
| `GET` | `/api/v1/inventarios/{id}` |
| `POST` | `/api/v1/movimientos-inventario` |
| `GET` | `/api/v1/movimientos-inventario/{id}` |
| `GET` | `/api/v1/movimientos-inventario/kardex/inventario/{inventarioId}` |

Dashboard y alertas:

| Metodo | Endpoint |
| --- | --- |
| `GET` | `/api/v1/dashboard/resumen` |
| `GET` | `/api/v1/dashboard/metricas` |
| `GET` | `/api/v1/dashboard/metricas/tipo/{tipoMetrica}` |
| `POST` | `/api/v1/dashboard/metricas/recalcular` |
| `GET` | `/api/v1/alertas-stock` |
| `PATCH` | `/api/v1/alertas-stock/{id}/resolver` |

Compras y documentos:

| Metodo | Endpoint |
| --- | --- |
| `GET` | `/api/v1/proveedores` |
| `GET` | `/api/v1/proveedores/activos` |
| `POST` | `/api/v1/proveedores` |
| `GET` | `/api/v1/ordenes-compra` |
| `GET` | `/api/v1/ordenes-compra/estado/{estado}` |
| `POST` | `/api/v1/ordenes-compra` |
| `PATCH` | `/api/v1/ordenes-compra/{id}/estado/{nuevoEstado}` |
| `GET` | `/api/v1/documentos` |
| `POST` | `/api/v1/documentos/upload` |
| `GET` | `/api/v1/documentos/{id}/download` |
| `POST` | `/api/v1/documentos/generar/dashboard` |

## Pruebas

Ejecutar:

```powershell
.\mvnw.cmd test
```

Las pruebas unitarias cubren:

- `AuthServiceImpl`: registro, cifrado de password, asignacion del rol por defecto, username duplicado y login con token.
- `CentroDistribucionServiceImpl`: creacion, actualizacion, eliminacion logica y recurso no encontrado.
- `DashboardMetricaServiceImpl`: recalculo de metricas, listas vacias, filas nulas, medicamentos proximos a agotarse y tipos validos de alerta.
- `AlertaStockServiceImpl`: generacion de alertas, no duplicar alertas pendientes y evaluacion de inventarios.

Las pruebas usan JUnit 5 y Mockito. No requieren PostgreSQL, Docker ni contexto Spring.

## Evidencia De Calidad

- Arquitectura por capas con controller, service, repository, entity, DTO y mapper.
- Autenticacion JWT y seguridad por roles.
- Validacion de DTOs con Jakarta Validation.
- Mappers MapStruct.
- Respuestas normalizadas con `ApiResponse` donde aplica.
- Manejo global de excepciones con errores personalizados.
- Docker full stack para PostgreSQL, backend y frontend.
- Frontend productivo servido con Nginx.
- Datos demo automaticos.
- Pruebas unitarias para logica critica de servicios.

## Equipo

- Santiago
- Mancera
- Juan
- Cardona
