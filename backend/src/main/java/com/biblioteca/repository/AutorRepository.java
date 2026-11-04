package com.biblioteca.repository;

import com.biblioteca.model.Autor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AutorRepository extends JpaRepository<Autor, Long> {

    @Query("SELECT a FROM Autor a WHERE :q IS NULL OR " +
           "LOWER(CONCAT(a.nombres, ' ', a.apellidos)) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(a.nacionalidad) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Autor> buscar(@Param("q") String q, Pageable pageable);

    @Query("SELECT COUNT(l) FROM Libro l JOIN l.autores a WHERE a.id = :autorId AND l.activo = true")
    long contarLibrosActivos(@Param("autorId") Long autorId);
}
