package com.biblioteca.service;

import com.biblioteca.dto.AutorRequest;
import com.biblioteca.dto.AutorResponse;
import com.biblioteca.dto.PageResponse;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.ReglaNegocioException;
import com.biblioteca.model.Autor;
import com.biblioteca.model.Libro;
import com.biblioteca.repository.AutorRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AutorService {

    private final AutorRepository autorRepository;

    public AutorService(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }

    /** RF-04: consultar autores (con búsqueda por nombre o nacionalidad). */
    @Transactional(readOnly = true)
    public PageResponse<AutorResponse> buscar(String q, Pageable pageable) {
        String filtro = StringUtils.hasText(q) ? q.trim() : null;
        return PageResponse.of(autorRepository.buscar(filtro, pageable), this::aResponse);
    }

    @Transactional(readOnly = true)
    public AutorResponse obtener(Long id) {
        return aResponse(buscarEntidad(id));
    }

    @Transactional
    public AutorResponse crear(AutorRequest req) {
        Autor a = new Autor();
        copiar(req, a);
        return aResponse(autorRepository.save(a));
    }

    @Transactional
    public AutorResponse actualizar(Long id, AutorRequest req) {
        Autor a = buscarEntidad(id);
        copiar(req, a);
        return aResponse(a);
    }

    /** Solo se elimina si no está asociado a libros activos. */
    @Transactional
    public void eliminar(Long id) {
        Autor a = buscarEntidad(id);
        if (autorRepository.contarLibrosActivos(id) > 0) {
            throw new ReglaNegocioException("No se puede eliminar: el autor tiene libros activos en el catálogo");
        }
        // Se quita la relación con libros dados de baja para no romper la tabla libro_autor.
        for (Libro libro : new ArrayList<>(a.getLibros())) {
            libro.getAutores().remove(a);
        }
        a.getLibros().clear();
        autorRepository.delete(a);
    }

    /** Devuelve las entidades Autor de los ids indicados; falla si alguno no existe. */
    Set<Autor> buscarVarios(Collection<Long> ids) {
        List<Autor> encontrados = autorRepository.findAllById(ids);
        if (encontrados.size() != new HashSet<>(ids).size()) {
            throw new RecursoNoEncontradoException("Uno o más autores seleccionados no existen");
        }
        return new HashSet<>(encontrados);
    }

    Autor buscarEntidad(Long id) {
        return autorRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Autor", id));
    }

    private AutorResponse aResponse(Autor a) {
        return AutorResponse.from(a, autorRepository.contarLibrosActivos(a.getId()));
    }

    private void copiar(AutorRequest req, Autor a) {
        a.setNombres(req.nombres().trim());
        a.setApellidos(req.apellidos().trim());
        a.setNacionalidad(req.nacionalidad());
        a.setBiografia(req.biografia());
    }
}
