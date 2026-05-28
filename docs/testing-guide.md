# Guia de pruebas funcionales

Esta guia valida FarmaRedOps-Intelligence antes de dockerizar, sin cambiar logica de negocio ni depender de credenciales reales.

## Alcance revisado

Estructura actual:

- Backend Spring Boot/Maven en la raiz del repositorio.
- Frontend React/Vite en `frontend`.
- Documentacion existente en `docs/crud-testing.md` y `docs/inventory-testing.md`.
- Test backend existente: `src/test/java/com/farmared/opsintelligence/FarmaRedOpsIntelligenceApplicationTests.java`, solo `contextLoads`.
- Servicios frontend reales en `frontend/src/api`.

Endpoints reales revisados:

- Auth: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`.
- Dashboard: `GET /api/v1/dashboard/resumen`, `GET /api/v1/dashboard/metricas`, `GET /api/v1/dashboard/metricas/tipo/{tipoMetrica}`, `POST /api/v1/dashboard/metricas/recalcular`.
- Medicamentos: `GET /api/v1/medicamentos`, `GET /api/v1/medicamentos/{id}`, `POST /api/v1/medicamentos`, `PUT /api/v1/medicamentos/{id}`, `DELETE /api/v1/medicamentos/{id}`.
- Categorias: `GET /api/v1/categorias-medicamento`, `GET /api/v1/categorias-medicamento/{id}`, `POST /api/v1/categorias-medicamento`, `PUT /api/v1/categorias-medicamento/{id}`, `DELETE /api/v1/categorias-medicamento/{id}`.
- Centros: `GET /api/v1/centros-distribucion`, `GET /api/v1/centros-distribucion/{id}`, `POST /api/v1/centros-distribucion`, `PUT /api/v1/centros-distribucion/{id}`, `DELETE /api/v1/centros-distribucion/{id}`.
- Proveedores: `GET /api/v1/proveedores`, `GET /api/v1/proveedores/activos`, `GET /api/v1/proveedores/{id}`, `POST /api/v1/proveedores`, `PUT /api/v1/proveedores/{id}`, `PATCH /api/v1/proveedores/{id}/desactivar`.
- Ordenes de compra: `GET /api/v1/ordenes-compra`, `GET /api/v1/ordenes-compra/{id}`, `GET /api/v1/ordenes-compra/estado/{estado}`, `POST /api/v1/ordenes-compra`, `PATCH /api/v1/ordenes-compra/{id}/estado/{nuevoEstado}`.
- Inventario/movimientos: `POST /api/v1/movimientos-inventario`, `GET /api/v1/movimientos-inventario/{id}`, `GET /api/v1/movimientos-inventario/kardex/inventario/{inventarioId}`.
- Documentos: `POST /api/v1/documentos/upload`, `GET /api/v1/documentos`, `GET /api/v1/documentos/{id}`, `GET /api/v1/documentos/{id}/download`, `PUT /api/v1/documentos/{id}`, `DELETE /api/v1/documentos/{id}`.

Pendientes detectados:

- No existe endpoint global `GET /api/v1/movimientos-inventario`; el listado disponible es Kardex por inventario.
- El frontend tiene `POST /documentos/generar/dashboard`, pero el backend no expone ese endpoint. La UI ya trata el 404 como "Generacion PDF pendiente".
- `SecurityConfig` exige autenticacion para todo excepto auth/error, pero no hay `@PreAuthorize` ni reglas por rol en backend. Los permisos por rol se validan principalmente en frontend con `RoleGuard`.
- `CentroDistribucionServiceImpl.actualizar()` permite cambiar `codigo`. Si la regla funcional es que el codigo no debe cambiar, esa prueba debe quedar como hallazgo hasta ajustar la logica de negocio.

## Requisitos previos

- Java 21 disponible.
- Maven Wrapper del proyecto disponible: `.\mvnw.cmd`.
- Node.js y npm disponibles.
- PostgreSQL corriendo.
- Base de datos local `farmared_db` creada y con datos seed.
- Backend corriendo en `http://localhost:8081`.
- Frontend corriendo en `http://127.0.0.1:5173`.
- `frontend/.env` apuntando a `VITE_API_BASE_URL=http://localhost:8081/api/v1`.
- Backend iniciado con `SERVER_PORT=8081`, porque `application.properties` usa `8080` por defecto si no se define la variable.

Comandos sugeridos:

```powershell
$env:SERVER_PORT = "8081"
.\mvnw.cmd spring-boot:run
```

```powershell
cd frontend
npm install
npm run dev -- --host 127.0.0.1
```

Usuarios locales recomendados:

- Admin: `admin2` / `123456`.
- Auxiliar de prueba: crear o confirmar un usuario local con rol `ROLE_AUXILIAR_BODEGA`, por ejemplo `auxiliar_bodega` / `123456`.
- Analista de prueba: crear o confirmar un usuario local con rol `ROLE_ANALISTA_COMPRAS`.

Estas credenciales son solo para ambiente local de desarrollo. No deben subirse como secretos ni usarse en produccion.

## Pruebas de compilacion

Backend:

```powershell
.\mvnw.cmd compile
```

Resultado esperado:

- `BUILD SUCCESS`.
- No se agregan dependencias nuevas.

Frontend:

```powershell
cd frontend
npm run build
```

Resultado esperado:

- Vite genera build correctamente.
- No subir `frontend/dist`.

## Smoke test automatizable

Script propuesto:

```powershell
.\scripts\smoke-test.ps1 -BaseUrl "http://localhost:8081/api/v1" -Token "<JWT_ADMIN>"
```

Tambien puede hacer login local de desarrollo:

```powershell
.\scripts\smoke-test.ps1 -BaseUrl "http://localhost:8081/api/v1" -Username "admin2" -Password "123456"
```

Por defecto el script no crea, actualiza, elimina ni desactiva datos. Para validar escrituras sin borrar datos:

```powershell
.\scripts\smoke-test.ps1 -Username "admin2" -Password "123456" -RunWrites -CategoriaId 1 -MedicamentoId 1 -ProveedorId 1 -InventarioId 1 -LoteMedicamentoId 1
```

## Checklist backend

### Auth

- `POST /api/v1/auth/login` con `admin2` / `123456`.
- Validar HTTP 200.
- Validar que la respuesta incluya `token`.
- Validar que la respuesta incluya `roles`.
- Validar que roles contenga el rol real del usuario, por ejemplo `ROLE_ADMIN_AUDITOR` o `ADMIN_AUDITOR` segun seed.
- `POST /api/v1/auth/register` con usuario local temporal.
- Validar HTTP 201.
- Validar token y roles devueltos. Nota: el registro asigna rol `USER` en el backend actual.

### Dashboard

- `GET /api/v1/dashboard/resumen`.
- Validar HTTP 200.
- Validar campos: `totalMedicamentosActivos`, `totalProveedoresActivos`, `totalInventarios`, `totalOrdenesPendientes`, `movimientosRecientes`.
- Comparar totales contra datos reales de PostgreSQL.
- `GET /api/v1/dashboard/metricas`.
- Validar HTTP 200 y arreglo.
- Si no hay metricas, ejecutar `POST /api/v1/dashboard/metricas/recalcular` con admin/analista y volver a listar.

### Medicamentos

- `GET /api/v1/medicamentos`.
- `POST /api/v1/medicamentos` con `categoriaMedicamentoId` existente.
- `PUT /api/v1/medicamentos/{id}` manteniendo `codigo`.
- `DELETE /api/v1/medicamentos/{id}`.
- Resultado esperado del delete: HTTP 204 y eliminacion logica (`activo=false`).

### Categorias

- `GET /api/v1/categorias-medicamento`.
- `POST /api/v1/categorias-medicamento`.
- `PUT /api/v1/categorias-medicamento/{id}`.
- `DELETE /api/v1/categorias-medicamento/{id}`.
- Resultado esperado del delete: HTTP 204 y `activo=false`.

### Centros de distribucion

- `GET /api/v1/centros-distribucion`.
- `POST /api/v1/centros-distribucion`.
- `PUT /api/v1/centros-distribucion/{id}`.
- Validacion requerida: el `codigo` no debe cambiar al actualizar.
- Estado actual del codigo: el backend permite cambiarlo; registrar como bug funcional si el criterio del negocio exige inmutabilidad.
- `DELETE /api/v1/centros-distribucion/{id}`.
- Resultado esperado del delete: HTTP 204 y `activo=false`.

### Proveedores

- `GET /api/v1/proveedores`.
- `GET /api/v1/proveedores/activos`.
- `POST /api/v1/proveedores`.
- `PUT /api/v1/proveedores/{id}` manteniendo `nit` salvo que se pruebe validacion de duplicados.
- `PATCH /api/v1/proveedores/{id}/desactivar`.
- Resultado esperado de desactivar: HTTP 204 y `activo=false`.

### Ordenes de compra

- `GET /api/v1/ordenes-compra`.
- `POST /api/v1/ordenes-compra` con `proveedorId` y `medicamentoId` existentes.
- `GET /api/v1/ordenes-compra/{id}`.
- Validar `detalles`, `total`, `estado=PENDIENTE`.
- `GET /api/v1/ordenes-compra/estado/PENDIENTE`.
- Opcional: `PATCH /api/v1/ordenes-compra/{id}/estado/APROBADA`.

### Inventario y movimientos

- `GET /api/v1/movimientos-inventario/kardex/inventario/{inventarioId}` para listar movimientos de un inventario real.
- `POST /api/v1/movimientos-inventario` con `tipoMovimiento=ENTRADA`.
- `POST /api/v1/movimientos-inventario` con `tipoMovimiento=SALIDA`.
- Validar que `stockAntes` y `stockDespues` cambian correctamente.
- Validar reglas de negocio: no permitir salida mayor al stock disponible, lote vencido o lote que no pertenece al medicamento del inventario.

### Documentos

- `POST /api/v1/documentos/upload` con multipart:
  - `archivo`: PDF.
  - `metadata`: JSON con `tipoDocumento`, `descripcion`, `moduloReferencia`, `referenciaId`, `usuarioCarga`.
- `GET /api/v1/documentos`.
- `GET /api/v1/documentos/{id}/download`.
- Validar header `Content-Disposition`.
- `POST /api/v1/documentos/generar/dashboard`: esperado actual HTTP 404 porque no existe endpoint backend.

## Seguridad por roles

### Sin token

- Probar `GET /api/v1/dashboard/resumen`.
- Probar `GET /api/v1/medicamentos`.
- Resultado esperado backend: HTTP 401 o respuesta de autenticacion rechazada.

### ROLE_AUXILIAR_BODEGA

Frontend:

- Puede navegar a catalogos: medicamentos, categorias, centros y documentos.
- Puede navegar a inventario.
- No debe navegar a dashboard, proveedores, ordenes ni usuarios si `RoleGuard` esta activo.

Backend actual:

- Si el token es valido, `SecurityConfig` permite endpoints protegidos sin discriminar rol.
- Por eso una prueba directa HTTP contra crear/editar/eliminar puede devolver 2xx en lugar de 403.
- Registrar esta diferencia como brecha si la rubrica exige permisos por rol tambien en backend.

### ROLE_ANALISTA_COMPRAS

Frontend:

- Puede ver dashboard.
- Puede gestionar proveedores y ordenes.
- Puede consultar catalogos.
- No debe acceder a usuarios.

Backend actual:

- No hay reglas de autorizacion por rol en controllers o servicios.

### ROLE_ADMIN_AUDITOR

Frontend:

- Puede acceder a dashboard.
- Puede gestionar catalogos.
- Puede acceder a modulos administrativos visibles, incluido usuarios.

Backend actual:

- Debe responder correctamente con token valido.
- No hay diferenciacion adicional por rol.

## Checklist frontend manual

Ejecutar con backend en `http://localhost:8081` y frontend en `http://127.0.0.1:5173`.

- Abrir `/login`.
- Login admin con `admin2` / `123456`.
- Validar redireccion a `/dashboard`.
- Validar tarjetas de dashboard con datos reales.
- Validar grafica de ordenes por estado.
- Validar grafica de metricas por tipo, o mensaje de estado vacio si no hay metricas.
- Abrir `/medicamentos` y validar lista.
- Abrir `/proveedores` y validar lista.
- Abrir `/ordenes-compra` y validar lista.
- Abrir `/documentos`, subir PDF local de prueba y descargarlo.
- Cerrar sesion o limpiar storage, abrir `/dashboard` y validar redireccion a `/login`.
- Login auxiliar y validar que catalogos cargan.
- Con auxiliar, intentar navegar a rutas no permitidas por frontend y validar `/forbidden`.
- Provocar un 403 si el backend lo implementa y validar mensaje `No tienes permisos para esta accion.`
- Apagar backend, recargar dashboard y validar mensaje claro: `No hay conexion con el backend. Verifica que el servidor este en ejecucion.`

## Errores comunes

- 401 Unauthorized: no hay token, token vencido, token invalido o storage fue limpiado. Volver a login.
- 403 Forbidden: token valido pero rol sin permiso. En el backend actual casi no aplica porque no hay autorizacion por rol fina.
- 404 Not Found: endpoint incorrecto o no implementado. Caso conocido: `/documentos/generar/dashboard`.
- 500 Internal Server Error: revisar consola backend, stacktrace, datos seed y relaciones en PostgreSQL.
- CORS: confirmar origen `http://127.0.0.1:5173` o `http://localhost:5173`; ambos estan permitidos en `SecurityConfig`.
- Backend apagado: frontend debe mostrar error de conexion por interceptor de Axios.
- Puerto incorrecto: `application.properties` usa 8080 por defecto; para esta guia iniciar backend con `SERVER_PORT=8081`.

## Evidencia recomendada

Registrar por cada corrida:

- Fecha y rama.
- Comando backend ejecutado y resultado.
- Comando frontend ejecutado y resultado.
- Usuario usado para pruebas.
- Capturas de dashboard, catalogos y documentos.
- Salida de `scripts/smoke-test.ps1`.
- Pendientes o fallos reproducibles.
