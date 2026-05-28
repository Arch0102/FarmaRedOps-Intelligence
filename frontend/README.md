# FarmaRed Ops-Intelligence Frontend

Aplicacion React para operar el backend de FarmaRed Ops-Intelligence: autenticacion JWT, dashboard, catalogos, inventario, compras y documentos.

## Requisitos

- Node.js 18 o superior.
- npm.
- Backend Spring Boot ejecutandose en `http://localhost:8080/api/v1`.

## Instalacion

```bash
cd frontend
npm install
```

## Variables de entorno

Crear un `.env` local a partir de `.env.example`:

```text
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

No se deben versionar secretos reales.

## Comandos

```bash
npm run dev
npm run build
```

## Modulos implementados

- Login, registro y logout.
- Rutas protegidas.
- Control visual por roles.
- Dashboard con tarjetas y graficas Recharts.
- Medicamentos.
- Categorias.
- Centros de distribucion.
- Inventario y kardex.
- Proveedores.
- Ordenes de compra.
- Documentos PDF.
- Usuarios preparado para integracion backend.

## Roles soportados

- `ROLE_AUXILIAR_BODEGA`
- `ROLE_ANALISTA_COMPRAS`
- `ROLE_ADMIN_AUDITOR`

El frontend oculta acciones por rol, pero la seguridad real sigue estando en el backend. Si el backend devuelve `403`, la UI muestra un mensaje de acceso denegado.

## Conexion con backend local

Ejecutar el backend en el puerto configurado:

```bash
cd ..
.\mvnw.cmd spring-boot:run
```

Luego iniciar frontend:

```bash
cd frontend
npm run dev
```

## Conexion con Docker

Si el backend y PostgreSQL se levantan con Docker, mantener:

```text
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## Manejo de ApiResponse

Los services usan `unwrapResponse` para leer respuestas con forma:

```json
{
  "success": true,
  "message": "Operacion correcta",
  "data": {}
}
```

Normalmente devuelven `response.data.data`. Tambien toleran respuestas directas para ramas donde algunos endpoints aun no esten normalizados.

## Endpoints pendientes o condicionados

- Listado general de inventarios y lotes: la UI de inventario deja el registro por IDs y consulta de kardex.
- Usuarios: pagina preparada porque no hay controller de usuarios en la estructura inspeccionada.
- Generacion PDF: la UI intenta `POST /documentos/generar/dashboard`; si no existe en la rama backend actual, muestra estado pendiente sin romper navegacion.
