package com.biblioteca.service;

import com.biblioteca.dto.*;
import com.biblioteca.event.UsuarioDeshabilitadoEvent;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.ReglaNegocioException;
import com.biblioteca.model.Usuario;
import com.biblioteca.model.enums.EstadoPrestamo;
import com.biblioteca.model.enums.EstadoReserva;
import com.biblioteca.model.enums.EstadoUsuario;
import com.biblioteca.model.enums.Rol;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.ReservaRepository;
import com.biblioteca.repository.UsuarioRepository;
import com.biblioteca.security.SecurityUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UsuarioService {

    private static final List<EstadoPrestamo> PRESTAMOS_ABIERTOS = List.of(EstadoPrestamo.ACTIVO, EstadoPrestamo.VENCIDO);
    private static final List<EstadoReserva> RESERVAS_ABIERTAS = List.of(EstadoReserva.PENDIENTE, EstadoReserva.DISPONIBLE);

    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;
    private final ReservaRepository reservaRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final ApplicationEventPublisher eventos;

    public UsuarioService(UsuarioRepository usuarioRepository, PrestamoRepository prestamoRepository,
                          ReservaRepository reservaRepository, PasswordEncoder passwordEncoder,
                          SecurityUtils securityUtils, ApplicationEventPublisher eventos) {
        this.usuarioRepository = usuarioRepository;
        this.prestamoRepository = prestamoRepository;
        this.reservaRepository = reservaRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityUtils = securityUtils;
        this.eventos = eventos;
    }

    /** RF-06: consultar usuarios con filtros. */
    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> buscar(String q, EstadoUsuario estado, Rol rol, Pageable pageable) {
        String filtro = StringUtils.hasText(q) ? q.trim() : null;
        return PageResponse.of(usuarioRepository.buscar(filtro, estado, rol, pageable), UsuarioResponse::from);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        securityUtils.verificarPropietario(id);
        return UsuarioResponse.from(buscarEntidad(id));
    }

    /** Perfil con totales (préstamos activos, vencidos, devueltos y reservas). */
    @Transactional(readOnly = true)
    public UsuarioResumenResponse resumen(Long id) {
        securityUtils.verificarPropietario(id);
        Usuario u = buscarEntidad(id);
        return new UsuarioResumenResponse(UsuarioResponse.from(u),
                prestamoRepository.countByUsuarioIdAndEstado(id, EstadoPrestamo.ACTIVO),
                prestamoRepository.countByUsuarioIdAndEstado(id, EstadoPrestamo.VENCIDO),
                prestamoRepository.countByUsuarioIdAndEstado(id, EstadoPrestamo.DEVUELTO),
                reservaRepository.countByUsuarioIdAndEstadoIn(id, RESERVAS_ABIERTAS));
    }

    /** RF-07: historial de préstamos de un usuario. */
    @Transactional(readOnly = true)
    public PageResponse<PrestamoResponse> historial(Long id, Pageable pageable) {
        securityUtils.verificarPropietario(id);
        buscarEntidad(id);
        return PageResponse.of(prestamoRepository.buscar(List.of(EstadoPrestamo.values()), id, null, pageable), PrestamoResponse::from);
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest req) {
        return UsuarioResponse.from(crearEntidad(req));
    }

    /** Crea el usuario validando datos únicos y cifrando la contraseña (lo usa también el autorregistro). */
    @Transactional
    Usuario crearEntidad(UsuarioRequest req) {
        validarUnicos(req, null);
        if (!StringUtils.hasText(req.password()) || req.password().length() < 6) {
            throw new ReglaNegocioException("La contraseña debe tener al menos 6 caracteres");
        }
        Usuario u = new Usuario();
        copiar(req, u);
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setEstado(EstadoUsuario.ACTIVO);
        return usuarioRepository.save(u);
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest req) {
        Usuario u = buscarEntidad(id);
        validarUnicos(req, id);
        if (id.equals(securityUtils.usuarioActual().getId()) && req.rol() != u.getRol()) {
            throw new ReglaNegocioException("No puede cambiar su propio rol");
        }
        copiar(req, u);
        if (StringUtils.hasText(req.password())) {
            if (req.password().length() < 6) {
                throw new ReglaNegocioException("La contraseña debe tener al menos 6 caracteres");
            }
            u.setPassword(passwordEncoder.encode(req.password()));
        }
        return UsuarioResponse.from(u);
    }

    /**
     * Activar, suspender o desactivar. No se desactiva a quien tiene libros sin devolver.
     * RN-13: al suspender o desactivar, sus reservas abiertas se cancelan (en la misma
     * transacción) y las unidades que tenía apartadas pasan a la siguiente reserva.
     */
    @Transactional
    public UsuarioResponse cambiarEstado(Long id, EstadoUsuario estado) {
        Usuario u = buscarEntidad(id);
        if (id.equals(securityUtils.usuarioActual().getId())) {
            throw new ReglaNegocioException("No puede cambiar el estado de su propia cuenta");
        }
        if (estado == EstadoUsuario.INACTIVO
                && prestamoRepository.countByUsuarioIdAndEstadoIn(id, PRESTAMOS_ABIERTOS) > 0) {
            throw new ReglaNegocioException("No se puede desactivar: el usuario tiene libros sin devolver");
        }
        u.setEstado(estado);
        if (estado != EstadoUsuario.ACTIVO) {
            eventos.publishEvent(new UsuarioDeshabilitadoEvent(id));
        }
        return UsuarioResponse.from(u);
    }

    /** "Eliminar" = baja lógica (estado INACTIVO) para conservar el historial. */
    @Transactional
    public void eliminar(Long id) {
        cambiarEstado(id, EstadoUsuario.INACTIVO);
    }

    /**
     * RN-03: para pedir préstamos o reservas el usuario debe estar ACTIVO
     * y no tener préstamos vencidos.
     */
    void validarHabilitado(Usuario u) {
        if (u.getEstado() != EstadoUsuario.ACTIVO) {
            throw new ReglaNegocioException("El usuario " + u.getNombreCompleto() + " está "
                    + u.getEstado().name().toLowerCase() + " y no puede realizar préstamos ni reservas");
        }
        if (prestamoRepository.countByUsuarioIdAndEstado(u.getId(), EstadoPrestamo.VENCIDO) > 0) {
            throw new ReglaNegocioException("El usuario tiene préstamos vencidos. Debe devolverlos primero");
        }
    }

    Usuario buscarEntidad(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    private void validarUnicos(UsuarioRequest req, Long id) {
        boolean docDup = id == null ? usuarioRepository.existsByDocumento(req.documento())
                : usuarioRepository.existsByDocumentoAndIdNot(req.documento(), id);
        if (docDup) throw new ReglaNegocioException("Ya existe un usuario con el documento " + req.documento());

        boolean correoDup = id == null ? usuarioRepository.existsByCorreoIgnoreCase(req.correo())
                : usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(req.correo(), id);
        if (correoDup) throw new ReglaNegocioException("Ya existe un usuario con el correo " + req.correo());

        boolean userDup = id == null ? usuarioRepository.existsByUsernameIgnoreCase(req.username())
                : usuarioRepository.existsByUsernameIgnoreCaseAndIdNot(req.username(), id);
        if (userDup) throw new ReglaNegocioException("El nombre de usuario " + req.username() + " ya está en uso");
    }

    private void copiar(UsuarioRequest req, Usuario u) {
        u.setNombres(req.nombres().trim());
        u.setApellidos(req.apellidos().trim());
        u.setDocumento(req.documento().trim());
        u.setCorreo(req.correo().trim().toLowerCase());
        u.setTelefono(req.telefono());
        u.setDireccion(req.direccion());
        u.setUsername(req.username().trim());
        u.setRol(req.rol());
    }
}
