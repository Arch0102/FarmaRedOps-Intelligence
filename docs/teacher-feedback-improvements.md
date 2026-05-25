# Teacher Feedback Improvements

Se normalizo la estructura de respuesta de la API con `ApiResponse`, incluyendo `success`, `timestamp`, `status`, `message`, `path`, `data` y `errors` para respuestas exitosas y de error.

Se ajusto el manejo de codigos operativos para `CentroDistribucion` y `CategoriaMedicamento`: el codigo se asigna al crear y se conserva estable en actualizaciones.

Se implemento control de acceso por roles manteniendo JWT:

- Auxiliar de Bodega (`ROLE_AUXILIAR_BODEGA`)
- Analista de Compras (`ROLE_ANALISTA_COMPRAS`)
- Administrador / Auditor (`ROLE_ADMIN_AUDITOR`)

Se agregaron mappers MapStruct para las entidades restantes, mapeando relaciones complejas como IDs y nombres relevantes para evitar ciclos innecesarios.

La arquitectura por capas se mantiene, PostgreSQL sigue como base configurada y no se modificaron endpoints existentes.
