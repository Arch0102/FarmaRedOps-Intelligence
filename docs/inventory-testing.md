# Evidencia de pruebas - Módulo de inventario con JWT

## Rama de trabajo

`feature/inventory-testing`

## Objetivo

Documentar el flujo de prueba del módulo de inventario utilizando autenticación JWT.

Esta evidencia valida que los endpoints de inventario funcionan correctamente después de la integración del módulo de seguridad JWT.

## Estado del backend probado

- Java 21.
- Spring Boot 3.5.13.
- PostgreSQL.
- Seguridad JWT activa.
- Manejo global de errores activo.
- Endpoints de inventario protegidos mediante Bearer Token.

## Flujo probado

### 1. Registro de usuario

**Endpoint:**

```http
POST /api/v1/auth/register
Body usado: 
{
  "username": "santiago",
  "email": "santiago@farmared.com",
  "password": "123456",
  "nombreCompleto": "Santiago Ortiz"
}
POST /api/v1/auth/login
Body usado: 
{
  "username": "santiago",
  "password": "123456"
}
Para probar los endpoints protegidos se usó:

Authorization: Bearer TOKEN_JWT

Entrada de inventario

Endpoint:

POST /api/v1/movimientos-inventario
Autenticación:

Authorization: Bearer TOKEN_JWT

Body usado:

{
  "tipoMovimiento": "ENTRADA",
  "inventarioId": 1,
  "loteMedicamentoId": 1,
  "cantidad": 100,
  "motivo": "Ingreso inicial de prueba",
  "observacion": "Prueba de entrada de inventario con JWT",
  "usuarioResponsable": "Santiago"
}
Salida de inventario

Endpoint:

POST /api/v1/movimientos-inventario

Autenticación:

Authorization: Bearer TOKEN_JWT

Body usado:

{
  "tipoMovimiento": "SALIDA",
  "inventarioId": 1,
  "loteMedicamentoId": 1,
  "cantidad": 75,
  "motivo": "Salida de prueba",
  "observacion": "Prueba de salida para validar Kardex y alerta",
  "usuarioResponsable": "Santiago"
}
Consulta de Kardex

Endpoint:

GET /api/v1/movimientos-inventario/kardex/inventario/1

Autenticación:

Authorization: Bearer TOKEN_JWT
. Validación en base de datos

Consultas usadas en PostgreSQL:

SELECT * FROM inventarios;
SELECT * FROM movimientos_inventario;
SELECT * FROM alertas_stock;
Resultado general

El flujo de inventario protegido con JWT fue probado correctamente.

Se validó:

Registro de usuario.
Login de usuario.
Uso de Bearer Token.
Entrada de inventario.
Salida de inventario.
Consulta de Kardex.
Actualización de inventario y lote.
Generación de alerta de stock crítico.