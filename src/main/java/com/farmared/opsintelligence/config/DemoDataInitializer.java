package com.farmared.opsintelligence.config;

import com.farmared.opsintelligence.entity.AlertaStock;
import com.farmared.opsintelligence.entity.CategoriaMedicamento;
import com.farmared.opsintelligence.entity.CentroDistribucion;
import com.farmared.opsintelligence.entity.DashboardMetrica;
import com.farmared.opsintelligence.entity.DetalleOrden;
import com.farmared.opsintelligence.entity.Inventario;
import com.farmared.opsintelligence.entity.LoteMedicamento;
import com.farmared.opsintelligence.entity.Medicamento;
import com.farmared.opsintelligence.entity.MedicamentoProveedor;
import com.farmared.opsintelligence.entity.MovimientoInventario;
import com.farmared.opsintelligence.entity.OrdenCompra;
import com.farmared.opsintelligence.entity.Proveedor;
import com.farmared.opsintelligence.entity.Rol;
import com.farmared.opsintelligence.entity.Usuario;
import com.farmared.opsintelligence.entity.UsuarioRol;
import com.farmared.opsintelligence.entity.enums.EstadoAlerta;
import com.farmared.opsintelligence.entity.enums.EstadoLote;
import com.farmared.opsintelligence.entity.enums.EstadoOrdenCompra;
import com.farmared.opsintelligence.entity.enums.TipoAlerta;
import com.farmared.opsintelligence.entity.enums.TipoMetricaDashboard;
import com.farmared.opsintelligence.entity.enums.TipoMovimiento;
import com.farmared.opsintelligence.repository.AlertaStockRepository;
import com.farmared.opsintelligence.repository.CategoriaMedicamentoRepository;
import com.farmared.opsintelligence.repository.CentroDistribucionRepository;
import com.farmared.opsintelligence.repository.DashboardMetricaRepository;
import com.farmared.opsintelligence.repository.DetalleOrdenRepository;
import com.farmared.opsintelligence.repository.InventarioRepository;
import com.farmared.opsintelligence.repository.LoteMedicamentoRepository;
import com.farmared.opsintelligence.repository.MedicamentoProveedorRepository;
import com.farmared.opsintelligence.repository.MedicamentoRepository;
import com.farmared.opsintelligence.repository.MovimientoInventarioRepository;
import com.farmared.opsintelligence.repository.OrdenCompraRepository;
import com.farmared.opsintelligence.repository.ProveedorRepository;
import com.farmared.opsintelligence.repository.RolRepository;
import com.farmared.opsintelligence.repository.UsuarioRepository;
import com.farmared.opsintelligence.repository.UsuarioRolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("docker")
@RequiredArgsConstructor
@Transactional
public class DemoDataInitializer implements CommandLineRunner {

    private static final String ROLE_AUXILIAR_BODEGA = "ROLE_AUXILIAR_BODEGA";
    private static final String ROLE_ANALISTA_COMPRAS = "ROLE_ANALISTA_COMPRAS";
    private static final String ROLE_ADMIN_AUDITOR = "ROLE_ADMIN_AUDITOR";

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final CategoriaMedicamentoRepository categoriaMedicamentoRepository;
    private final CentroDistribucionRepository centroDistribucionRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final InventarioRepository inventarioRepository;
    private final LoteMedicamentoRepository loteMedicamentoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final AlertaStockRepository alertaStockRepository;
    private final ProveedorRepository proveedorRepository;
    private final MedicamentoProveedorRepository medicamentoProveedorRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;
    private final DashboardMetricaRepository dashboardMetricaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (medicamentoRepository.count() > 0 || usuarioRepository.count() > 0) {
            return;
        }

        Map<String, Rol> roles = crearRoles();
        crearUsuarios(roles);

        Map<String, CategoriaMedicamento> categorias = crearCategorias();
        List<CentroDistribucion> centros = crearCentros();
        List<Medicamento> medicamentos = crearMedicamentos(categorias);
        List<Inventario> inventariosPrincipal = crearInventarios(medicamentos, centros);
        List<LoteMedicamento> lotesPrincipales = crearLotes(medicamentos);

        crearMovimientos(inventariosPrincipal, lotesPrincipales);
        crearAlertas(inventariosPrincipal, centros.get(0));

        List<Proveedor> proveedores = crearProveedores();
        crearMedicamentosProveedor(medicamentos, proveedores);
        crearOrdenesCompra(medicamentos, proveedores);
        crearMetricasIniciales(inventariosPrincipal);
    }

    private Map<String, Rol> crearRoles() {
        Map<String, Rol> roles = new LinkedHashMap<>();
        roles.put(ROLE_AUXILIAR_BODEGA, obtenerOCrearRol(
                ROLE_AUXILIAR_BODEGA,
                "Consulta catalogos y registra movimientos de inventario"
        ));
        roles.put(ROLE_ANALISTA_COMPRAS, obtenerOCrearRol(
                ROLE_ANALISTA_COMPRAS,
                "Gestiona proveedores, compras y analitica operativa"
        ));
        roles.put(ROLE_ADMIN_AUDITOR, obtenerOCrearRol(
                ROLE_ADMIN_AUDITOR,
                "Administra catalogos, usuarios, auditoria y configuracion operativa"
        ));
        return roles;
    }

    private Rol obtenerOCrearRol(String nombre, String descripcion) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> {
                    Rol rol = new Rol();
                    rol.setNombre(nombre);
                    rol.setDescripcion(descripcion);
                    rol.setActivo(true);
                    return rolRepository.save(rol);
                });
    }

    private void crearUsuarios(Map<String, Rol> roles) {
        crearUsuario("admin", "admin@farmared.local", "admin123", "Administrador FarmaRed",
                roles.get(ROLE_ADMIN_AUDITOR));
        crearUsuario("auxiliar", "auxiliar@farmared.local", "auxiliar123", "Auxiliar de Bodega Demo",
                roles.get(ROLE_AUXILIAR_BODEGA));
        crearUsuario("compras", "compras@farmared.local", "compras123", "Analista de Compras Demo",
                roles.get(ROLE_ANALISTA_COMPRAS));
    }

    private Usuario crearUsuario(String username, String email, String password, String nombreCompleto, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setActivo(true);
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(usuarioGuardado);
        usuarioRol.setRol(rol);
        usuarioRolRepository.save(usuarioRol);

        return usuarioGuardado;
    }

    private Map<String, CategoriaMedicamento> crearCategorias() {
        Map<String, CategoriaMedicamento> categorias = new LinkedHashMap<>();
        categorias.put("ANALGESICOS", crearCategoria("Analg\u00e9sicos", "Medicamentos para manejo del dolor"));
        categorias.put("ANTIBIOTICOS", crearCategoria("Antibi\u00f3ticos", "Medicamentos antimicrobianos"));
        categorias.put("ANTIINFLAMATORIOS", crearCategoria("Antiinflamatorios", "Control de inflamacion y dolor"));
        categorias.put("ANTIHIPERTENSIVOS", crearCategoria("Antihipertensivos", "Control de presion arterial"));
        categorias.put("ANTIDIABETICOS", crearCategoria("Antidiab\u00e9ticos", "Control glucemico"));
        categorias.put("ANTIALERGICOS", crearCategoria("Antial\u00e9rgicos", "Manejo de alergias"));
        categorias.put("GASTROINTESTINALES", crearCategoria("Gastrointestinales", "Tracto digestivo y acidez"));
        categorias.put("RESPIRATORIOS", crearCategoria("Respiratorios", "Manejo respiratorio e inhaladores"));
        return categorias;
    }

    private CategoriaMedicamento crearCategoria(String nombre, String descripcion) {
        CategoriaMedicamento categoria = new CategoriaMedicamento();
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        categoria.setActivo(true);
        return categoriaMedicamentoRepository.save(categoria);
    }

    private List<CentroDistribucion> crearCentros() {
        return List.of(
                crearCentro("CD-001", "Centro Principal FarmaRed", "Carrera 45 # 26-85", "Bogota"),
                crearCentro("CD-002", "Centro Norte", "Autopista Norte # 150-20", "Bogota"),
                crearCentro("CD-003", "Centro Sur", "Avenida Sur # 12-40", "Bogota")
        );
    }

    private CentroDistribucion crearCentro(String codigo, String nombre, String direccion, String ciudad) {
        CentroDistribucion centro = new CentroDistribucion();
        centro.setCodigo(codigo);
        centro.setNombre(nombre);
        centro.setDireccion(direccion);
        centro.setCiudad(ciudad);
        centro.setActivo(true);
        return centroDistribucionRepository.save(centro);
    }

    private List<Medicamento> crearMedicamentos(Map<String, CategoriaMedicamento> categorias) {
        List<MedicamentoSeed> seeds = List.of(
                new MedicamentoSeed("MED-001", "Acetaminof\u00e9n 500mg", "Acetaminofen", "500 mg", "Tableta", "unidad", 40, 500, 90, "ANALGESICOS"),
                new MedicamentoSeed("MED-002", "Ibuprofeno 400mg", "Ibuprofeno", "400 mg", "Tableta", "unidad", 35, 450, 80, "ANTIINFLAMATORIOS"),
                new MedicamentoSeed("MED-003", "Naproxeno 250mg", "Naproxeno", "250 mg", "Tableta", "unidad", 30, 420, 75, "ANTIINFLAMATORIOS"),
                new MedicamentoSeed("MED-004", "Amoxicilina 500mg", "Amoxicilina", "500 mg", "Capsula", "unidad", 45, 600, 100, "ANTIBIOTICOS"),
                new MedicamentoSeed("MED-005", "Azitromicina 500mg", "Azitromicina", "500 mg", "Tableta", "unidad", 30, 300, 65, "ANTIBIOTICOS"),
                new MedicamentoSeed("MED-006", "Losart\u00e1n 50mg", "Losartan potasico", "50 mg", "Tableta", "unidad", 50, 700, 120, "ANTIHIPERTENSIVOS"),
                new MedicamentoSeed("MED-007", "Enalapril 20mg", "Enalapril maleato", "20 mg", "Tableta", "unidad", 40, 500, 95, "ANTIHIPERTENSIVOS"),
                new MedicamentoSeed("MED-008", "Metformina 850mg", "Metformina clorhidrato", "850 mg", "Tableta", "unidad", 60, 800, 140, "ANTIDIABETICOS"),
                new MedicamentoSeed("MED-009", "Loratadina 10mg", "Loratadina", "10 mg", "Tableta", "unidad", 30, 350, 70, "ANTIALERGICOS"),
                new MedicamentoSeed("MED-010", "Omeprazol 20mg", "Omeprazol", "20 mg", "Capsula", "unidad", 45, 650, 110, "GASTROINTESTINALES"),
                new MedicamentoSeed("MED-011", "Salbutamol inhalador", "Salbutamol", "100 mcg/dosis", "Inhalador", "unidad", 25, 250, 60, "RESPIRATORIOS"),
                new MedicamentoSeed("MED-012", "Cetirizina 10mg", "Cetirizina", "10 mg", "Tableta", "unidad", 35, 400, 85, "ANTIALERGICOS"),
                new MedicamentoSeed("MED-013", "Diclofenaco 50mg", "Diclofenaco sodico", "50 mg", "Tableta", "unidad", 35, 450, 90, "ANTIINFLAMATORIOS"),
                new MedicamentoSeed("MED-014", "Ciprofloxacino 500mg", "Ciprofloxacino", "500 mg", "Tableta", "unidad", 40, 350, 80, "ANTIBIOTICOS"),
                new MedicamentoSeed("MED-015", "Insulina NPH", "Insulina humana NPH", "100 UI/ml", "Vial", "unidad", 20, 180, 45, "ANTIDIABETICOS"),
                new MedicamentoSeed("MED-016", "Hidroclorotiazida 25mg", "Hidroclorotiazida", "25 mg", "Tableta", "unidad", 35, 360, 75, "ANTIHIPERTENSIVOS"),
                new MedicamentoSeed("MED-017", "Prednisolona 5mg", "Prednisolona", "5 mg", "Tableta", "unidad", 30, 320, 70, "ANTIINFLAMATORIOS"),
                new MedicamentoSeed("MED-018", "Dexametasona 4mg", "Dexametasona", "4 mg", "Tableta", "unidad", 25, 260, 55, "ANTIINFLAMATORIOS"),
                new MedicamentoSeed("MED-019", "Ranitidina 150mg", "Ranitidina", "150 mg", "Tableta", "unidad", 30, 300, 65, "GASTROINTESTINALES"),
                new MedicamentoSeed("MED-020", "Esomeprazol 40mg", "Esomeprazol", "40 mg", "Capsula", "unidad", 40, 420, 90, "GASTROINTESTINALES"),
                new MedicamentoSeed("MED-021", "Clorfeniramina 4mg", "Clorfeniramina", "4 mg", "Tableta", "unidad", 30, 300, 75, "ANTIALERGICOS"),
                new MedicamentoSeed("MED-022", "Ambroxol jarabe", "Ambroxol", "15 mg/5 ml", "Jarabe", "frasco", 25, 240, 60, "RESPIRATORIOS"),
                new MedicamentoSeed("MED-023", "Budesonida inhalador", "Budesonida", "200 mcg/dosis", "Inhalador", "unidad", 20, 180, 50, "RESPIRATORIOS"),
                new MedicamentoSeed("MED-024", "Atorvastatina 20mg", "Atorvastatina", "20 mg", "Tableta", "unidad", 45, 500, 100, "ANTIHIPERTENSIVOS"),
                new MedicamentoSeed("MED-025", "Aspirina 100mg", "Acido acetilsalicilico", "100 mg", "Tableta", "unidad", 35, 550, 85, "ANALGESICOS")
        );

        List<Medicamento> medicamentos = new ArrayList<>();
        for (MedicamentoSeed seed : seeds) {
            Medicamento medicamento = new Medicamento();
            medicamento.setCodigo(seed.codigo());
            medicamento.setNombre(seed.nombre());
            medicamento.setDescripcion("Demo " + seed.nombre() + " para pruebas operativas");
            medicamento.setPrincipioActivo(seed.principioActivo());
            medicamento.setConcentracion(seed.concentracion());
            medicamento.setPresentacion(seed.presentacion());
            medicamento.setUnidadMedida(seed.unidadMedida());
            medicamento.setStockMinimo(seed.stockMinimo());
            medicamento.setStockMaximo(seed.stockMaximo());
            medicamento.setPuntoReorden(seed.puntoReorden());
            medicamento.setActivo(true);
            medicamento.setCategoriaMedicamento(categorias.get(seed.categoriaKey()));
            medicamentos.add(medicamentoRepository.save(medicamento));
        }
        return medicamentos;
    }

    private List<Inventario> crearInventarios(List<Medicamento> medicamentos, List<CentroDistribucion> centros) {
        int[] stockPrincipal = {
                180, 45, 0, 70, 25, 210, 35, 90, 0, 55,
                120, 30, 75, 20, 0, 60, 15, 42, 0, 88,
                34, 160, 50, 110, 22
        };

        List<Inventario> inventariosPrincipal = new ArrayList<>();
        CentroDistribucion principal = centros.get(0);
        CentroDistribucion norte = centros.get(1);
        CentroDistribucion sur = centros.get(2);

        for (int i = 0; i < medicamentos.size(); i++) {
            inventariosPrincipal.add(crearInventario(medicamentos.get(i), principal, stockPrincipal[i], 0));
        }

        for (int i = 0; i < 8; i++) {
            crearInventario(medicamentos.get(i), norte, 35 + (i * 12), 0);
        }

        for (int i = 8; i < 16; i++) {
            crearInventario(medicamentos.get(i), sur, 28 + (i * 7), 0);
        }

        return inventariosPrincipal;
    }

    private Inventario crearInventario(
            Medicamento medicamento,
            CentroDistribucion centro,
            Integer stockActual,
            Integer stockReservado
    ) {
        Inventario inventario = new Inventario();
        inventario.setMedicamento(medicamento);
        inventario.setCentroDistribucion(centro);
        inventario.setStockActual(stockActual);
        inventario.setStockReservado(stockReservado);
        inventario.setStockDisponible(stockActual - stockReservado);
        inventario.setFechaUltimaActualizacion(LocalDateTime.now().minusHours(2));
        return inventarioRepository.save(inventario);
    }

    private List<LoteMedicamento> crearLotes(List<Medicamento> medicamentos) {
        List<LoteMedicamento> lotesPrincipales = new ArrayList<>();
        for (int i = 0; i < medicamentos.size(); i++) {
            Medicamento medicamento = medicamentos.get(i);
            int cantidadActual = Math.max(0, 80 + (i * 9));
            lotesPrincipales.add(crearLote(
                    "LOTE-" + medicamento.getCodigo() + "-A",
                    LocalDate.now().minusMonths(6),
                    LocalDate.now().plusMonths(10 + (i % 8)),
                    cantidadActual + 60,
                    cantidadActual,
                    EstadoLote.ACTIVO,
                    medicamento
            ));
        }

        for (int i = 0; i < 6; i++) {
            Medicamento medicamento = medicamentos.get(i);
            crearLote(
                    "LOTE-" + medicamento.getCodigo() + "-PV",
                    LocalDate.now().minusMonths(14),
                    LocalDate.now().plusDays(12 + i),
                    45,
                    25 + i,
                    EstadoLote.ACTIVO,
                    medicamento
            );
        }

        for (int i = 6; i < 10; i++) {
            Medicamento medicamento = medicamentos.get(i);
            crearLote(
                    "LOTE-" + medicamento.getCodigo() + "-V",
                    LocalDate.now().minusMonths(20),
                    LocalDate.now().minusDays(10 + i),
                    35,
                    8,
                    EstadoLote.VENCIDO,
                    medicamento
            );
        }

        return lotesPrincipales;
    }

    private LoteMedicamento crearLote(
            String numeroLote,
            LocalDate fechaFabricacion,
            LocalDate fechaVencimiento,
            Integer cantidadInicial,
            Integer cantidadActual,
            EstadoLote estado,
            Medicamento medicamento
    ) {
        LoteMedicamento lote = new LoteMedicamento();
        lote.setNumeroLote(numeroLote);
        lote.setFechaFabricacion(fechaFabricacion);
        lote.setFechaVencimiento(fechaVencimiento);
        lote.setCantidadInicial(cantidadInicial);
        lote.setCantidadActual(cantidadActual);
        lote.setEstado(estado);
        lote.setMedicamento(medicamento);
        return loteMedicamentoRepository.save(lote);
    }

    private void crearMovimientos(List<Inventario> inventariosPrincipal, List<LoteMedicamento> lotesPrincipales) {
        for (int i = 0; i < 15; i++) {
            Inventario inventario = inventariosPrincipal.get(i);
            LoteMedicamento lote = lotesPrincipales.get(i);
            int stockFinal = inventario.getStockActual();
            int salida = stockFinal == 0 ? 25 : Math.min(35, Math.max(12, stockFinal / 4));
            int entrada = stockFinal + salida;

            crearMovimiento(
                    TipoMovimiento.ENTRADA,
                    entrada,
                    0,
                    entrada,
                    "Carga inicial demo",
                    "Entrada inicial para poblar kardex",
                    "admin",
                    LocalDateTime.now().minusDays(18 - i),
                    inventario,
                    lote
            );

            crearMovimiento(
                    TipoMovimiento.SALIDA,
                    salida,
                    entrada,
                    stockFinal,
                    "Despacho operativo demo",
                    "Salida demo para rotacion y kardex",
                    "auxiliar",
                    LocalDateTime.now().minusDays(10 - (i / 2)).minusHours(i),
                    inventario,
                    lote
            );
        }
    }

    private void crearMovimiento(
            TipoMovimiento tipoMovimiento,
            Integer cantidad,
            Integer stockAntes,
            Integer stockDespues,
            String motivo,
            String observacion,
            String responsable,
            LocalDateTime fechaMovimiento,
            Inventario inventario,
            LoteMedicamento lote
    ) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAntes(stockAntes);
        movimiento.setStockDespues(stockDespues);
        movimiento.setMotivo(motivo);
        movimiento.setObservacion(observacion);
        movimiento.setUsuarioResponsable(responsable);
        movimiento.setFechaMovimiento(fechaMovimiento);
        movimiento.setInventario(inventario);
        movimiento.setLoteMedicamento(lote);
        movimientoInventarioRepository.save(movimiento);
    }

    private void crearAlertas(
            List<Inventario> inventariosPrincipal,
            CentroDistribucion centroPrincipal
    ) {
        for (Inventario inventario : inventariosPrincipal) {
            Medicamento medicamento = inventario.getMedicamento();
            int stockActual = inventario.getStockActual();
            if (stockActual <= medicamento.getPuntoReorden()) {
                String mensaje = stockActual <= 0
                        ? "Quiebre de stock demo para " + medicamento.getNombre()
                        : "Stock en umbral critico o punto de reorden para " + medicamento.getNombre();
                crearAlerta(TipoAlerta.STOCK_CRITICO, mensaje, medicamento, centroPrincipal, null);
            }
        }

        for (LoteMedicamento lote : loteMedicamentoRepository.findByFechaVencimientoBefore(LocalDate.now().plusDays(30))) {
            if (lote.getEstado() != EstadoLote.VENCIDO) {
                crearAlerta(
                        TipoAlerta.PROXIMO_VENCIMIENTO,
                        "Lote proximo a vencer: " + lote.getNumeroLote(),
                        lote.getMedicamento(),
                        centroPrincipal,
                        lote
                );
            }
        }

        loteMedicamentoRepository.findByEstado(EstadoLote.VENCIDO).forEach(lote -> crearAlerta(
                TipoAlerta.LOTE_VENCIDO,
                "Lote vencido: " + lote.getNumeroLote(),
                lote.getMedicamento(),
                centroPrincipal,
                lote
        ));
    }

    private void crearAlerta(
            TipoAlerta tipoAlerta,
            String mensaje,
            Medicamento medicamento,
            CentroDistribucion centro,
            LoteMedicamento lote
    ) {
        AlertaStock alerta = new AlertaStock();
        alerta.setTipoAlerta(tipoAlerta);
        alerta.setEstadoAlerta(EstadoAlerta.PENDIENTE);
        alerta.setMensaje(mensaje);
        alerta.setFechaGeneracion(LocalDateTime.now().minusHours(1));
        alerta.setMedicamento(medicamento);
        alerta.setCentroDistribucion(centro);
        alerta.setLoteMedicamento(lote);
        alertaStockRepository.save(alerta);
    }

    private List<Proveedor> crearProveedores() {
        return List.of(
                crearProveedor("900100001-1", "Distribuidora Salud Total", "6015551001", "contacto@saludtotal.demo"),
                crearProveedor("900100002-2", "FarmaSuministros Colombia", "6015551002", "ventas@farmasuministros.demo"),
                crearProveedor("900100003-3", "MedPlus Distribuciones", "6015551003", "operaciones@medplus.demo"),
                crearProveedor("900100004-4", "Drogueria Mayorista Andina", "6015551004", "compras@andina.demo"),
                crearProveedor("900100005-5", "BioPharma Logistics", "6015551005", "logistica@biopharma.demo")
        );
    }

    private Proveedor crearProveedor(String nit, String nombre, String telefono, String correo) {
        Proveedor proveedor = new Proveedor();
        proveedor.setNit(nit);
        proveedor.setNombre(nombre);
        proveedor.setTelefono(telefono);
        proveedor.setCorreo(correo);
        proveedor.setDireccion("Zona industrial demo");
        proveedor.setActivo(true);
        return proveedorRepository.save(proveedor);
    }

    private void crearMedicamentosProveedor(List<Medicamento> medicamentos, List<Proveedor> proveedores) {
        for (int i = 0; i < medicamentos.size(); i++) {
            Proveedor proveedor = proveedores.get(i % proveedores.size());
            crearMedicamentoProveedor(
                    medicamentos.get(i),
                    proveedor,
                    BigDecimal.valueOf(1200L + (i * 350L)),
                    2 + (i % 6)
            );
        }

        for (int i = 0; i < 10; i++) {
            crearMedicamentoProveedor(
                    medicamentos.get(i),
                    proveedores.get((i + 2) % proveedores.size()),
                    BigDecimal.valueOf(1500L + (i * 275L)),
                    4 + (i % 5)
            );
        }
    }

    private void crearMedicamentoProveedor(
            Medicamento medicamento,
            Proveedor proveedor,
            BigDecimal precioReferencia,
            Integer tiempoEntregaDias
    ) {
        MedicamentoProveedor medicamentoProveedor = new MedicamentoProveedor();
        medicamentoProveedor.setMedicamento(medicamento);
        medicamentoProveedor.setProveedor(proveedor);
        medicamentoProveedor.setPrecioReferencia(precioReferencia);
        medicamentoProveedor.setTiempoEntregaDias(tiempoEntregaDias);
        medicamentoProveedor.setActivo(true);
        medicamentoProveedorRepository.save(medicamentoProveedor);
    }

    private void crearOrdenesCompra(List<Medicamento> medicamentos, List<Proveedor> proveedores) {
        crearOrden(
                "OC-DEMO-001",
                EstadoOrdenCompra.PENDIENTE,
                LocalDate.now().minusDays(6),
                LocalDate.now().plusDays(4),
                null,
                "Reposicion analgesicos",
                proveedores.get(0),
                List.of(
                        new OrdenDetalleSeed(medicamentos.get(0), 120, BigDecimal.valueOf(1400)),
                        new OrdenDetalleSeed(medicamentos.get(1), 80, BigDecimal.valueOf(1850))
                )
        );
        crearOrden(
                "OC-DEMO-002",
                EstadoOrdenCompra.APROBADA,
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(6),
                null,
                "Antibioticos para demanda semanal",
                proveedores.get(1),
                List.of(
                        new OrdenDetalleSeed(medicamentos.get(3), 90, BigDecimal.valueOf(2200)),
                        new OrdenDetalleSeed(medicamentos.get(4), 60, BigDecimal.valueOf(3100))
                )
        );
        crearOrden(
                "OC-DEMO-003",
                EstadoOrdenCompra.RECIBIDA,
                LocalDate.now().minusDays(14),
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                "Orden recibida para medicamentos cronicos",
                proveedores.get(2),
                List.of(
                        new OrdenDetalleSeed(medicamentos.get(5), 150, BigDecimal.valueOf(950)),
                        new OrdenDetalleSeed(medicamentos.get(7), 130, BigDecimal.valueOf(1250))
                )
        );
        crearOrden(
                "OC-DEMO-004",
                EstadoOrdenCompra.CANCELADA,
                LocalDate.now().minusDays(11),
                LocalDate.now().plusDays(1),
                null,
                "Cancelada por cambio de proveedor",
                proveedores.get(3),
                List.of(
                        new OrdenDetalleSeed(medicamentos.get(10), 40, BigDecimal.valueOf(16800)),
                        new OrdenDetalleSeed(medicamentos.get(22), 35, BigDecimal.valueOf(22400))
                )
        );
        crearOrden(
                "OC-DEMO-005",
                EstadoOrdenCompra.PENDIENTE,
                LocalDate.now().minusDays(3),
                LocalDate.now().plusDays(8),
                null,
                "Reposicion gastrointestinales y alergias",
                proveedores.get(4),
                List.of(
                        new OrdenDetalleSeed(medicamentos.get(9), 100, BigDecimal.valueOf(980)),
                        new OrdenDetalleSeed(medicamentos.get(11), 110, BigDecimal.valueOf(760)),
                        new OrdenDetalleSeed(medicamentos.get(19), 75, BigDecimal.valueOf(1450))
                )
        );
    }

    private void crearOrden(
            String codigo,
            EstadoOrdenCompra estado,
            LocalDate fechaOrden,
            LocalDate fechaEstimadaEntrega,
            LocalDate fechaRecepcion,
            String observacion,
            Proveedor proveedor,
            List<OrdenDetalleSeed> detalles
    ) {
        OrdenCompra orden = new OrdenCompra();
        orden.setCodigo(codigo);
        orden.setFechaOrden(fechaOrden);
        orden.setFechaEstimadaEntrega(fechaEstimadaEntrega);
        orden.setFechaRecepcion(fechaRecepcion);
        orden.setEstado(estado);
        orden.setObservacion(observacion);
        orden.setProveedor(proveedor);
        orden.setTotal(detalles.stream()
                .map(detalle -> detalle.precioUnitario().multiply(BigDecimal.valueOf(detalle.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        OrdenCompra ordenGuardada = ordenCompraRepository.save(orden);

        for (OrdenDetalleSeed detalleSeed : detalles) {
            DetalleOrden detalle = new DetalleOrden();
            detalle.setOrdenCompra(ordenGuardada);
            detalle.setMedicamento(detalleSeed.medicamento());
            detalle.setCantidad(detalleSeed.cantidad());
            detalle.setPrecioUnitario(detalleSeed.precioUnitario());
            detalle.setSubtotal(detalleSeed.precioUnitario().multiply(BigDecimal.valueOf(detalleSeed.cantidad())));
            detalleOrdenRepository.save(detalle);
        }
    }

    private void crearMetricasIniciales(List<Inventario> inventariosPrincipal) {
        long stockCritico = inventariosPrincipal.stream()
                .filter(inventario -> inventario.getStockActual() <= inventario.getMedicamento().getStockMinimo())
                .count();
        long proximosVencer = loteMedicamentoRepository
                .findByFechaVencimientoBefore(LocalDate.now().plusDays(30))
                .size();
        long ordenesPendientes = ordenCompraRepository.countByEstado(EstadoOrdenCompra.PENDIENTE);
        long salidas = movimientoInventarioRepository.findByTipoMovimiento(TipoMovimiento.SALIDA)
                .stream()
                .mapToLong(MovimientoInventario::getCantidad)
                .sum();

        crearMetrica(TipoMetricaDashboard.STOCK_CRITICO, BigDecimal.valueOf(stockCritico),
                "inventarios", "Inventarios demo en stock critico o quiebre");
        crearMetrica(TipoMetricaDashboard.PROXIMO_VENCIMIENTO, BigDecimal.valueOf(proximosVencer),
                "lotes", "Lotes demo proximos a vencer o vencidos");
        crearMetrica(TipoMetricaDashboard.ORDENES_PENDIENTES, BigDecimal.valueOf(ordenesPendientes),
                "ordenes", "Ordenes demo pendientes por gestionar");
        crearMetrica(TipoMetricaDashboard.ROTACION_INVENTARIO, BigDecimal.valueOf(salidas),
                "unidades", "Unidades demo despachadas para rotacion");
    }

    private void crearMetrica(
            TipoMetricaDashboard tipoMetrica,
            BigDecimal valor,
            String unidad,
            String descripcion
    ) {
        DashboardMetrica metrica = new DashboardMetrica();
        metrica.setTipoMetrica(tipoMetrica);
        metrica.setValor(valor);
        metrica.setUnidad(unidad);
        metrica.setDescripcion(descripcion);
        metrica.setFechaCalculo(LocalDateTime.now());
        dashboardMetricaRepository.save(metrica);
    }

    private record MedicamentoSeed(
            String codigo,
            String nombre,
            String principioActivo,
            String concentracion,
            String presentacion,
            String unidadMedida,
            Integer stockMinimo,
            Integer stockMaximo,
            Integer puntoReorden,
            String categoriaKey
    ) {
    }

    private record OrdenDetalleSeed(
            Medicamento medicamento,
            Integer cantidad,
            BigDecimal precioUnitario
    ) {
    }
}
