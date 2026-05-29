package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.Inventario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByMedicamentoIdAndCentroDistribucionId(
            Long medicamentoId,
            Long centroDistribucionId
    );

    List<Inventario> findByMedicamentoId(Long medicamentoId);

    List<Inventario> findByCentroDistribucionId(Long centroDistribucionId);

    List<Inventario> findByStockActualLessThanEqual(Integer stockActual);

    @EntityGraph(attributePaths = {"medicamento", "medicamento.categoriaMedicamento", "centroDistribucion"})
    @Query("""
            select i
            from Inventario i
            """)
    List<Inventario> findAllWithDetails();

    @EntityGraph(attributePaths = {"medicamento", "medicamento.categoriaMedicamento", "centroDistribucion"})
    @Query("""
            select i
            from Inventario i
            where i.id = :id
            """)
    Optional<Inventario> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"medicamento", "medicamento.categoriaMedicamento", "centroDistribucion"})
    @Query("""
            select i
            from Inventario i
            where i.stockActual <= 0
            order by i.stockActual asc
            """)
    List<Inventario> findInventariosEnQuiebre();

    @EntityGraph(attributePaths = {"medicamento", "medicamento.categoriaMedicamento", "centroDistribucion"})
    @Query("""
            select i
            from Inventario i
            where i.stockActual <= i.medicamento.stockMinimo
            order by i.stockActual asc
            """)
    List<Inventario> findInventariosStockCritico();

    @EntityGraph(attributePaths = {"medicamento", "medicamento.categoriaMedicamento", "centroDistribucion"})
    @Query("""
            select i
            from Inventario i
            where i.stockActual > i.medicamento.stockMinimo
              and i.stockActual <= i.medicamento.puntoReorden
            order by i.stockActual asc
            """)
    List<Inventario> findInventariosProximosAgotarse();

    @Query("""
            select m.nombre, coalesce(sum(i.stockActual), 0)
            from Inventario i
            join i.medicamento m
            group by m.nombre
            order by m.nombre asc
            """)
    List<Object[]> sumStockByMedicamento();

    @Query("""
            select c.nombre, coalesce(sum(i.stockActual), 0)
            from Inventario i
            join i.centroDistribucion c
            group by c.nombre
            order by c.nombre asc
            """)
    List<Object[]> sumStockByCentro();

    @Query("""
            select cat.nombre, coalesce(sum(i.stockActual), 0)
            from Inventario i
            join i.medicamento m
            join m.categoriaMedicamento cat
            group by cat.nombre
            order by cat.nombre asc
            """)
    List<Object[]> sumStockByCategoria();

    @Query("""
            select count(i)
            from Inventario i
            where i.stockActual <= i.medicamento.stockMinimo
            """)
    long countStockCritico();
}
