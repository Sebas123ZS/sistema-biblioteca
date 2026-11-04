package com.biblioteca.service;

import com.biblioteca.dto.CategoriaRequest;
import com.biblioteca.dto.CategoriaResponse;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.ReglaNegocioException;
import com.biblioteca.model.Categoria;
import com.biblioteca.repository.CategoriaRepository;
import com.biblioteca.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final LibroRepository libroRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, LibroRepository libroRepository) {
        this.categoriaRepository = categoriaRepository;
        this.libroRepository = libroRepository;
    }

    /** RF-17: lista las categorías con la cantidad de títulos activos de cada una. */
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        Map<Long, Long> conteo = new HashMap<>();
        for (Object[] fila : categoriaRepository.contarLibrosActivosPorCategoria()) {
            conteo.put(((Number) fila[0]).longValue(), ((Number) fila[1]).longValue());
        }
        return categoriaRepository.findAllByOrderByNombreAsc().stream()
                .map(c -> CategoriaResponse.from(c, conteo.getOrDefault(c.getId(), 0L)))
                .toList();
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest req) {
        String nombre = req.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe la categoría " + nombre);
        }
        return CategoriaResponse.from(categoriaRepository.save(new Categoria(nombre, req.descripcion())), 0);
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest req) {
        Categoria c = buscarEntidad(id);
        String nombre = req.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe la categoría " + nombre);
        }
        c.setNombre(nombre);
        c.setDescripcion(req.descripcion());
        return CategoriaResponse.from(c, libroRepository.countByCategoriaIdAndActivoTrue(id));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria c = buscarEntidad(id);
        if (libroRepository.existsByCategoriaId(id)) {
            throw new ReglaNegocioException("No se puede eliminar: hay libros registrados en esta categoría");
        }
        categoriaRepository.delete(c);
    }

    Categoria buscarEntidad(Long id) {
        return categoriaRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Categoría", id));
    }
}
