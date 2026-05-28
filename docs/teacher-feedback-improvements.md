# Mejoras finales del backend

## Normalizacion API

Los endpoints de datos responden con `ApiResponse`, incluyendo `success`, `timestamp`, `status`, `message`, `path`, `data` y `errors`. La descarga de documentos se mantiene como `ResponseEntity<Resource>` para no romper la respuesta binaria.

## Seguridad por roles

`SecurityConfig` mantiene `/api/v1/auth/**` publico y aplica reglas por rol para catalogos, inventario, compras, dashboard, usuarios y documentos. Los roles base se inicializan como:

- `ROLE_AUXILIAR_BODEGA`
- `ROLE_ANALISTA_COMPRAS`
- `ROLE_ADMIN_AUDITOR`

## Codigos fijos

`CentroDistribucionServiceImpl` conserva el campo `codigo` al actualizar. Categoria de medicamento no tiene campo `codigo`, por lo que solo valida nombre duplicado.

## MapStruct

Se mantienen los mappers existentes y se agregan mappers puntuales para los modulos tocados: dashboard, documentos y movimientos de inventario.

## Dashboard

El resumen del dashboard usa datos reales de base de datos: medicamentos activos, proveedores activos, ordenes de compra, ordenes por estado, inventario, stock critico, alertas, vencimientos y movimientos recientes.

## Documentos PDF

El modulo de documentos permite subir, listar, consultar, actualizar, desactivar y descargar PDFs. Tambien puede generar un reporte PDF de dashboard usando OpenPDF y datos reales del sistema.

## Dockerizacion

Se agregan `Dockerfile`, `docker-compose.yml`, `.dockerignore` y `docs/docker.md` para levantar backend y PostgreSQL en local/dev con variables de entorno.
