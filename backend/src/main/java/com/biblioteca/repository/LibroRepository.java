package com.biblioteca.repository;

import com.biblioteca.model.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    boolean existsByCategoriaId(Long categoriaId);

    long countByCategoriaIdAndActivoTrue(Long categoriaId);

    /**
     * Búsqueda del catálogo (RF-03): por texto (título, ISBN o autor), categoría,
     * autor y disponibilidad. Solo devuelve libros activos.
     * El filtro de disponibilidad se expresa como rango de unidades disponibles
     * (disponibles: 1..MAX, agotados: 0..0, todos: 0..MAX).
     */
    @Query(value = "SELECT DISTINCT l FROM Libro l LEFT JOIN l.autores a " +
           "WHERE l.activo = true " +
           "AND (:q IS NULL OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "     OR l.isbn LIKE CONCAT('%', :q, '%') " +
           "     OR LOWER(CONCAT(a.nombres, ' ', a.apellidos)) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:categoriaId IS NULL OR l.categoria.id = :categoriaId) " +
           "AND (:autorId IS NULL OR a.id = :autorId) " +
           "AND l.cantidadDisponible BETWEEN :minDisponible AND :maxDisponible",
           countQuery = "SELECT COUNT(DISTINCT l) FROM Libro l LEFT JOIN l.autores a " +
           "WHERE l.activo = true " +
           "AND (:q IS NULL OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "     OR l.isbn LIKE CONCAT('%', :q, '%') " +
           "     OR LOWER(CONCAT(a.nombres, ' ', a.apellidos)) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:categoriaId IS NULL OR l.categoria.id = :categoriaId) " +
           "AND (:autorId IS NULL OR a.id = :autorId) " +
           "AND l.cantidadDisponible BETWEEN :minDisponible AND :maxDisponible")
    Page<Libro> buscar(@Param("q") String q,
                       @Param("categoriaId") Long categoriaId,
                       @Param("autorId") Long autorId,
                       @Param("minDisponible") Integer minDisponible,
                       @Param("maxDisponible") Integer maxDisponible,
                       Pageable pageable);

    @Query("SELECT COUNT(l) FROM Libro l WHERE l.activo = true")
    long contarActivos();

    @Query("SELECT COALESCE(SUM(l.cantidadTotal), 0) FROM Libro l WHERE l.activo = true")
    long sumarEjemplares();

    @Query("SELECT COALESCE(SUM(l.cantidadDisponible), 0) FROM Libro l WHERE l.activo = true")
    long sumarDisponibles();

    List<Libro> findTop5ByActivoTrueOrderByFechaRegistroDesc();
}
