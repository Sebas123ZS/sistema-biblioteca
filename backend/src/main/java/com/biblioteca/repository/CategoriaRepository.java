package com.biblioteca.repository;

import com.biblioteca.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Categoria> findAllByOrderByNombreAsc();

    /** Cantidad de títulos activos por categoría: [id de la categoría, total]. */
    @Query("SELECT c.id, COUNT(l) FROM Libro l JOIN l.categoria c WHERE l.activo = true GROUP BY c.id")
    List<Object[]> contarLibrosActivosPorCategoria();

    /** Cantidad de títulos activos por categoría: [nombre, total]. */
    @Query("SELECT c.nombre, COUNT(l) FROM Libro l JOIN l.categoria c " +
           "WHERE l.activo = true GROUP BY c.nombre ORDER BY COUNT(l) DESC")
    List<Object[]> contarLibrosPorCategoria();
}
