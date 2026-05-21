# Evidencia de pruebas - CRUD base con JWT

## Rama de trabajo

feature/crud-testing-docs

## Objetivo

Documentar la prueba de los CRUD base implementados para la segunda entrega del proyecto FarmaRed Ops-Intelligence.

Se validaron tres entidades principales:

- CategoriaMedicamento
- CentroDistribucion
- Medicamento

Los endpoints fueron probados usando autenticación JWT mediante Bearer Token.

## Estado del backend probado

- Java 21.
- Spring Boot 3.5.13.
- PostgreSQL.
- Seguridad JWT activa.
- Manejo global de errores activo.
- Endpoints protegidos mediante Bearer Token.
- Arquitectura por capas: Controller, Service, Repository, DTO y Entity.

## Autenticación usada

Antes de probar los CRUD se realizó registro o login para obtener un token JWT.

Endpoint de registro:

POST /api/v1/auth/register

Body usado:

{
  "username": "santiago",
  "email": "santiago@farmared.com",
  "password": "123456",
  "nombreCompleto": "Santiago Ortiz"
}

Resultado esperado:

- HTTP 201 Created.
- Usuario creado correctamente.
- Respuesta con token JWT.

Endpoint de login:

POST /api/v1/auth/login

Body usado:

{
  "username": "santiago",
  "password": "123456"
}

Resultado esperado:

- HTTP 200 OK.
- Respuesta con token JWT.
- El token fue usado como Bearer Token en los endpoints protegidos.

Autorización usada en Postman:

Authorization: Bearer TOKEN_JWT

## CRUD 1 - Categorias de medicamento

### Crear categoria

Endpoint:

POST /api/v1/categorias-medicamento

Body usado:

{
  "nombre": "Antibioticos",
  "descripcion": "Medicamentos usados para tratar infecciones bacterianas",
  "activo": true
}

Resultado esperado:

- HTTP 201 Created.
- Categoria creada correctamente.
- Registro almacenado en la tabla categorias_medicamento.

### Listar categorias

Endpoint:

GET /api/v1/categorias-medicamento

Resultado esperado:

- HTTP 200 OK.
- Lista de categorias registrada en base de datos.

### Consultar categoria por ID

Endpoint:

GET /api/v1/categorias-medicamento/{id}

Resultado esperado:

- HTTP 200 OK.
- Informacion de la categoria consultada.

### Actualizar categoria

Endpoint:

PUT /api/v1/categorias-medicamento/{id}

Body usado:

{
  "nombre": "Antibioticos actualizados",
  "descripcion": "Categoria actualizada desde prueba CRUD",
  "activo": true
}

Resultado esperado:

- HTTP 200 OK.
- Categoria actualizada correctamente.

### Eliminar categoria

Endpoint:

DELETE /api/v1/categorias-medicamento/{id}

Resultado esperado:

- HTTP 204 No Content.
- Eliminacion logica aplicada mediante activo = false.

## CRUD 2 - Centros de distribucion

### Crear centro de distribucion

Endpoint:

POST /api/v1/centros-distribucion

Body usado:

{
  "codigo": "CD-BOG-001",
  "nombre": "Centro de Distribucion Bogota",
  "direccion": "Av. Principal #123",
  "ciudad": "Bogota",
  "activo": true
}

Resultado esperado:

- HTTP 201 Created.
- Centro de distribucion creado correctamente.
- Registro almacenado en la tabla centros_distribucion.

### Listar centros de distribucion

Endpoint:

GET /api/v1/centros-distribucion

Resultado esperado:

- HTTP 200 OK.
- Lista de centros registrada en base de datos.

### Consultar centro por ID

Endpoint:

GET /api/v1/centros-distribucion/{id}

Resultado esperado:

- HTTP 200 OK.
- Informacion del centro consultado.

### Actualizar centro de distribucion

Endpoint:

PUT /api/v1/centros-distribucion/{id}

Body usado:

{
  "codigo": "CD-BOG-001",
  "nombre": "Centro de Distribucion Bogota Norte",
  "direccion": "Av. Principal #456",
  "ciudad": "Bogota",
  "activo": true
}

Resultado esperado:

- HTTP 200 OK.
- Centro de distribucion actualizado correctamente.

### Eliminar centro de distribucion

Endpoint:

DELETE /api/v1/centros-distribucion/{id}

Resultado esperado:

- HTTP 204 No Content.
- Eliminacion logica aplicada mediante activo = false.

## CRUD 3 - Medicamentos

### Crear medicamento

Endpoint:

POST /api/v1/medicamentos

Body usado:

{
  "codigo": "MED-CRUD-001",
  "nombre": "Acetaminofen 500mg CRUD",
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

Resultado esperado:

- HTTP 201 Created.
- Medicamento creado correctamente.
- Medicamento asociado a una categoria existente.
- Registro almacenado en la tabla medicamentos.

### Listar medicamentos

Endpoint:

GET /api/v1/medicamentos

Resultado esperado:

- HTTP 200 OK.
- Lista de medicamentos registrada en base de datos.

### Consultar medicamento por ID

Endpoint:

GET /api/v1/medicamentos/{id}

Resultado esperado:

- HTTP 200 OK.
- Informacion del medicamento consultado.

### Actualizar medicamento

Endpoint:

PUT /api/v1/medicamentos/{id}

Body usado:

{
  "codigo": "MED-CRUD-001",
  "nombre": "Acetaminofen 500mg actualizado",
  "descripcion": "Medicamento actualizado desde prueba CRUD",
  "principioActivo": "Paracetamol",
  "concentracion": "500mg",
  "presentacion": "Tableta",
  "unidadMedida": "unidad",
  "stockMinimo": 25,
  "stockMaximo": 600,
  "puntoReorden": 40,
  "activo": true,
  "categoriaMedicamentoId": 1
}

Resultado esperado:

- HTTP 200 OK.
- Medicamento actualizado correctamente.

### Eliminar medicamento

Endpoint:

DELETE /api/v1/medicamentos/{id}

Resultado esperado:

- HTTP 204 No Content.
- Eliminacion logica aplicada mediante activo = false.

## Validacion en base de datos

Consultas usadas en PostgreSQL:

SELECT * FROM categorias_medicamento;
SELECT * FROM centros_distribucion;
SELECT * FROM medicamentos;

Resultado esperado:

- Registros creados correctamente.
- Registros listados correctamente.
- Registros consultados por ID correctamente.
- Registros actualizados correctamente.
- Registros eliminados logicamente con activo = false.
- Relacion entre medicamento y categoria funcionando correctamente.

## Validacion de seguridad

Se valido que los endpoints CRUD requieren token JWT.

Endpoints publicos:

- POST /api/v1/auth/register
- POST /api/v1/auth/login

Endpoints protegidos:

- /api/v1/categorias-medicamento
- /api/v1/centros-distribucion
- /api/v1/medicamentos

Resultado esperado:

- Sin token, los endpoints protegidos no permiten acceso.
- Con Bearer Token valido, los endpoints responden correctamente.

## Validacion de errores

El manejo global de errores permite respuestas controladas para casos como:

- Recurso no encontrado.
- Recurso duplicado.
- Datos invalidos.
- Token invalido o expirado.
- Errores de validacion.

## Resultado general

El CRUD base de tres entidades fue probado correctamente con JWT:

- CRUD de categorias de medicamento.
- CRUD de centros de distribucion.
- CRUD de medicamentos.
- Uso de Bearer Token.
- Validaciones basicas.
- Manejo de errores mediante GlobalExceptionHandler.
- Persistencia en PostgreSQL.

## Relacion con la rubrica

Esta evidencia respalda el requisito de CRUD funcional minimo para tres entidades, usando Spring Boot, PostgreSQL, JWT y arquitectura por capas.

## Pendientes

- Agregar pruebas automatizadas mas adelante.
- Documentar endpoints con Swagger/OpenAPI.
- Definir permisos por rol para cada modulo.
- Integrar estos endpoints con frontend React en una etapa posterior.
