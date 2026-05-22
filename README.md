# FarmaRed Ops-Intelligence

FarmaRed Ops-Intelligence es un sistema backend desarrollado con Spring Boot para apoyar la gestión operativa de una red farmacéutica. El objetivo del proyecto es centralizar procesos relacionados con medicamentos, centros de distribución, inventario, movimientos de stock, usuarios, autenticación, proveedores, órdenes de compra y futuras métricas de analítica.

El sistema está construido como una API REST, usando arquitectura por capas, seguridad con JWT, persistencia en PostgreSQL, DTOs, MapStruct, manejo global de errores y trabajo colaborativo mediante Git/GitHub.

---

## Estado actual del proyecto

Actualmente el backend cuenta con:

- API REST funcional.
- Conexión a PostgreSQL.
- Seguridad con Spring Security y JWT.
- Registro y login de usuarios.
- Endpoints protegidos mediante Bearer Token.
- Manejo global de errores con `GlobalExceptionHandler`.
- CRUD funcional de tres entidades:
  - Categorías de medicamento.
  - Centros de distribución.
  - Medicamentos.
- Módulo de inventario:
  - Entrada de inventario.
  - Salida de inventario.
  - Consulta de Kardex.
  - Alertas de stock crítico.
- Modelo base para:
  - Proveedores.
  - Medicamentos por proveedor.
  - Órdenes de compra.
  - Detalles de orden.
  - Métricas de dashboard.
- Mapeo DTO/Entity con MapStruct.
- Documentación de pruebas en la carpeta `docs`.

---

## Tecnologías utilizadas

- Java 21
- Spring Boot 3.5.13
- Spring Web
- Spring Data JPA
- Hibernate
- Spring Security
- JWT con JJWT
- PostgreSQL
- Maven
- Lombok
- MapStruct
- Jakarta Validation
- Postman
- Git y GitHub

---

## Arquitectura del proyecto

El proyecto sigue una arquitectura por capas para separar responsabilidades y facilitar mantenimiento.

Estructura lógica:

```text
Controller
Service
ServiceImpl
Repository
Entity
DTO Request / DTO Response
Mapper
Exception Handler
Security
```

Explicación de capas:

- `Controller`: recibe peticiones HTTP y retorna respuestas REST.
- `Service`: define operaciones de negocio.
- `ServiceImpl`: implementa la lógica de negocio.
- `Repository`: acceso a datos con Spring Data JPA.
- `Entity`: representación de tablas de base de datos.
- `DTO Request`: datos recibidos desde el cliente.
- `DTO Response`: datos enviados como respuesta.
- `Mapper`: conversión entre entidades y DTOs.
- `Exception Handler`: manejo centralizado de errores.
- `Security`: configuración JWT y seguridad de endpoints.

Estructura principal:

```text
src/main/java/com/farmared/opsintelligence
├── config
├── controller
├── dto
│   ├── request
│   └── response
├── entity
│   └── enums
├── exception
├── mapper
├── repository
├── security
├── service
│   └── impl
└── FarmaRedOpsIntelligenceApplication.java
```

---

## Configuración del proyecto

El proyecto usa variables de entorno para evitar subir contraseñas o secretos reales al repositorio.

Configuración principal esperada en `application.properties`:

```properties
spring.application.name=FarmaRedOpsIntelligence

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/farmared_db}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

server.port=${SERVER_PORT:8080}

jwt.secret=${JWT_SECRET:clave_jwt_de_desarrollo_con_minimo_32_caracteres}
jwt.expiration-ms=${JWT_EXPIRATION_MS:3600000}
```

Variables necesarias para ejecutar localmente:

```text
DB_URL=jdbc:postgresql://localhost:5432/farmared_db
DB_USER=postgres
DB_PASSWORD=TU_CONTRASEÑA_REAL
SERVER_PORT=8080
JWT_SECRET=clave_jwt_de_desarrollo_con_minimo_32_caracteres
JWT_EXPIRATION_MS=3600000
```

Ejemplo para IntelliJ en `Environment variables`:

```text
DB_URL=jdbc:postgresql://localhost:5432/farmared_db;DB_USER=postgres;DB_PASSWORD=TU_CONTRASEÑA_REAL;SERVER_PORT=8080;JWT_SECRET=clave_jwt_de_desarrollo_con_minimo_32_caracteres;JWT_EXPIRATION_MS=3600000
```

---

## Base de datos

El proyecto utiliza PostgreSQL.

Base de datos esperada:

```text
farmared_db
```

Si la base de datos no existe, puede crearse con:

```sql
CREATE DATABASE farmared_db;
```

Hibernate está configurado con:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Esto permite crear o actualizar tablas automáticamente durante el desarrollo.

Tablas principales actuales:

```text
usuarios
roles
usuarios_roles
categorias_medicamento
centros_distribucion
medicamentos
inventarios
lotes_medicamento
movimientos_inventario
alertas_stock
proveedores
medicamentos_proveedor
ordenes_compra
detalles_orden
dashboard_metricas
```

---

## Seguridad JWT

La aplicación usa Spring Security con JWT.

Flujo de autenticación:

1. El usuario se registra o inicia sesión.
2. El backend genera un token JWT.
3. El cliente usa el token para consumir endpoints protegidos.
4. El token se envía en el header `Authorization`.

Formato:

```text
Authorization: Bearer TOKEN_JWT
```

Endpoints públicos:

```http
POST /api/v1/auth/register
POST /api/v1/auth/login
```

Todos los demás endpoints requieren Bearer Token.

---

## Autenticación

### Registro

```http
POST /api/v1/auth/register
```

Body de ejemplo:

```json
{
  "username": "santiago",
  "email": "santiago@farmared.com",
  "password": "123456",
  "nombreCompleto": "Santiago Ortiz"
}
```

Resultado esperado:

```text
201 Created
```

---

### Login

```http
POST /api/v1/auth/login
```

Body de ejemplo:

```json
{
  "username": "santiago",
  "password": "123456"
}
```

Resultado esperado:

```text
200 OK
```

La respuesta retorna un token JWT que debe usarse para consumir los endpoints protegidos.

---

## CRUD base implementado

Para la segunda entrega se implementó CRUD funcional mínimo de tres entidades principales:

- `CategoriaMedicamento`
- `CentroDistribucion`
- `Medicamento`

Cada entidad cuenta con operaciones para:

- Crear.
- Listar.
- Consultar por ID.
- Actualizar.
- Eliminar lógicamente.

La eliminación lógica significa que el registro no se borra físicamente de la base de datos, sino que se marca como inactivo mediante el campo `activo = false`.

---

## CRUD de Categorías de medicamento

Endpoint base:

```http
/api/v1/categorias-medicamento
```

Operaciones:

```http
GET    /api/v1/categorias-medicamento
GET    /api/v1/categorias-medicamento/{id}
POST   /api/v1/categorias-medicamento
PUT    /api/v1/categorias-medicamento/{id}
DELETE /api/v1/categorias-medicamento/{id}
```

Ejemplo de creación:

```json
{
  "nombre": "Antibióticos",
  "descripcion": "Medicamentos usados para tratar infecciones bacterianas",
  "activo": true
}
```

---

## CRUD de Centros de distribución

Endpoint base:

```http
/api/v1/centros-distribucion
```

Operaciones:

```http
GET    /api/v1/centros-distribucion
GET    /api/v1/centros-distribucion/{id}
POST   /api/v1/centros-distribucion
PUT    /api/v1/centros-distribucion/{id}
DELETE /api/v1/centros-distribucion/{id}
```

Ejemplo de creación:

```json
{
  "codigo": "CD-BOG-001",
  "nombre": "Centro de Distribución Bogotá",
  "direccion": "Av. Principal #123",
  "ciudad": "Bogotá",
  "activo": true
}
```

---

## CRUD de Medicamentos

Endpoint base:

```http
/api/v1/medicamentos
```

Operaciones:

```http
GET    /api/v1/medicamentos
GET    /api/v1/medicamentos/{id}
POST   /api/v1/medicamentos
PUT    /api/v1/medicamentos/{id}
DELETE /api/v1/medicamentos/{id}
```

Ejemplo de creación:

```json
{
  "codigo": "MED-CRUD-001",
  "nombre": "Acetaminofén 500mg CRUD",
  "descripcion": "Medicamento creado desde prueba CRUD",
  "principioActivo": "Paracetamol",
  "concentracion": "500mg",
  "presentacion": "Tableta",
  "unidadMedida": "unidad",
  "stockMinimo": 20,
  "stockMaximo": 500,
  "puntoReorden": 30,
  "activo": true,
  "categoriaMedicamentoId": 1
}
```

Para crear medicamentos debe existir previamente una categoría.

---

## Módulo de inventario

El módulo de inventario permite registrar movimientos de entrada y salida, actualizar stock y consultar Kardex.

Endpoint base:

```http
/api/v1/movimientos-inventario
```

Operaciones implementadas:

```http
POST /api/v1/movimientos-inventario
GET  /api/v1/movimientos-inventario/{id}
GET  /api/v1/movimientos-inventario/kardex/inventario/{inventarioId}
```

### Entrada de inventario

```http
POST /api/v1/movimientos-inventario
```

Body de ejemplo:

```json
{
  "tipoMovimiento": "ENTRADA",
  "inventarioId": 1,
  "loteMedicamentoId": 1,
  "cantidad": 100,
  "motivo": "Ingreso inicial de prueba",
  "observacion": "Prueba de entrada de inventario con JWT",
  "usuarioResponsable": "Santiago"
}
```

La entrada incrementa el stock del inventario y del lote.

### Salida de inventario

```http
POST /api/v1/movimientos-inventario
```

Body de ejemplo:

```json
{
  "tipoMovimiento": "SALIDA",
  "inventarioId": 1,
  "loteMedicamentoId": 1,
  "cantidad": 75,
  "motivo": "Salida de prueba",
  "observacion": "Prueba de salida para validar Kardex y alerta",
  "usuarioResponsable": "Santiago"
}
```

La salida disminuye el stock del inventario y del lote. Si el stock queda por debajo o igual al punto de reorden, se genera una alerta de stock crítico.

### Kardex

```http
GET /api/v1/movimientos-inventario/kardex/inventario/{inventarioId}
```

Este endpoint permite consultar el historial de movimientos de inventario.

---

## Modelo complementario

También se implementó un modelo base para funcionalidades futuras:

- `Proveedor`
- `MedicamentoProveedor`
- `OrdenCompra`
- `DetalleOrden`
- `DashboardMetrica`

Este modelo permitirá desarrollar posteriormente:

- Gestión de proveedores.
- Gestión de órdenes de compra.
- Dashboard de analítica.
- Métricas de operación.
- Abastecimiento predictivo.

---

## Manejo global de errores

El proyecto cuenta con `GlobalExceptionHandler` usando `@RestControllerAdvice`.

Esto permite que los errores tengan una respuesta uniforme.

Excepciones customizadas:

```text
ResourceNotFoundException
BusinessRuleException
BadRequestException
DuplicateResourceException
UnauthorizedException
ForbiddenException
InvalidTokenException
```

Ejemplo de respuesta de error:

```json
{
  "timestamp": "2026-05-20T20:48:00",
  "status": 400,
  "error": "Solicitud inválida",
  "message": "Mensaje del error",
  "path": "/api/v1/recurso"
}
```

También se manejan errores de validación de campos en los DTOs.

---

## MapStruct

El proyecto integra MapStruct para apoyar el mapeo entre entidades y DTOs.

Mappers implementados:

```text
CategoriaMedicamentoMapper
CentroDistribucionMapper
MedicamentoMapper
```

Ubicación:

```text
src/main/java/com/farmared/opsintelligence/mapper
```

MapStruct ayuda a separar la transformación de datos de la lógica de negocio y fortalece la arquitectura por capas.

---

## Documentación de pruebas

La carpeta `docs` contiene evidencia de pruebas manuales.

Archivos actuales:

```text
docs/inventory-testing.md
docs/crud-testing.md
```

### inventory-testing.md

Incluye pruebas de:

- Registro y login.
- Uso de token JWT.
- Entrada de inventario.
- Salida de inventario.
- Consulta de Kardex.
- Validación de stock y alertas.

### crud-testing.md

Incluye pruebas de:

- CRUD de categorías de medicamento.
- CRUD de centros de distribución.
- CRUD de medicamentos.
- Uso de Bearer Token.
- Validación en PostgreSQL.
- Validación de endpoints protegidos.

---

## Cómo ejecutar el proyecto

### 1. Clonar el repositorio

```bash
git clone https://github.com/Arch0102/FarmaRedOps-Intelligence.git
```

### 2. Entrar al proyecto

```bash
cd FarmaRedOps-Intelligence
```

### 3. Cambiar a la rama de desarrollo

```bash
git checkout dev
git pull origin dev
```

### 4. Configurar variables de entorno

```text
DB_URL=jdbc:postgresql://localhost:5432/farmared_db
DB_USER=postgres
DB_PASSWORD=TU_CONTRASEÑA_REAL
SERVER_PORT=8080
JWT_SECRET=clave_jwt_de_desarrollo_con_minimo_32_caracteres
JWT_EXPIRATION_MS=3600000
```

### 5. Compilar

En Windows PowerShell:

```powershell
.\mvnw.cmd compile
```

En Linux o macOS:

```bash
./mvnw compile
```

### 6. Ejecutar

En Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

También puede ejecutarse desde IntelliJ usando la clase principal:

```text
FarmaRedOpsIntelligenceApplication
```

---
## Funcionalidades pendientes

Para próximas fases quedan pendientes:

- Servicios y controladores completos para órdenes de compra.
- Dashboard de analíticas.
- Reportes y generación de PDF.
- Subida y descarga de documentos.
- Dockerización.
- Pruebas automatizadas.
- Swagger/OpenAPI.
- Permisos específicos por roles.
- Frontend en React.

---

## Elevadores del proyecto

Los elevadores definidos para el proyecto son:

1. Dashboard de analíticas.
2. Generación y gestión de PDF.
3. Dockerización.
4. Frontend en React.

El frontend se dejará para una fase posterior, cuando el backend esté más estable y los endpoints principales estén definidos y probados.

---

## Estado para la segunda entrega

El proyecto cuenta actualmente con:

- API REST funcional.
- CRUD mínimo de tres entidades.
- Seguridad con JWT.
- PostgreSQL.
- Arquitectura por capas.
- DTOs.
- MapStruct.
- Manejo global de errores.
- Documentación de pruebas.
- Trabajo colaborativo con Git y Pull Requests.

