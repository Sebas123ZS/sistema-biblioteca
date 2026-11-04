package com.biblioteca.service;

import com.biblioteca.dto.ReservaRequest;
import com.biblioteca.exception.ReglaNegocioException;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Reserva;
import com.biblioteca.model.Usuario;
import com.biblioteca.model.enums.EstadoReserva;
import com.biblioteca.model.enums.EstadoUsuario;
import com.biblioteca.model.enums.Rol;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.ReservaRepository;
import com.biblioteca.security.SecurityUtils;
import com.biblioteca.security.UsuarioPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock ReservaRepository reservaRepository;
    @Mock LibroRepository libroRepository;
    @Mock PrestamoRepository prestamoRepository;
    @Mock UsuarioService usuarioService;
    @Mock SecurityUtils securityUtils;

    ReservaService service;
    Libro libro;

    @BeforeEach
    void setUp() {
        service = new ReservaService(reservaRepository, libroRepository, prestamoRepository,
                usuarioService, securityUtils, 3);
        libro = new Libro();
        libro.setId(20L);
        libro.setTitulo("El principito");
        libro.setCantidadTotal(2);
        libro.setCantidadDisponible(0);
        libro.setActivo(true);
    }

    private Usuario lector() {
        Usuario u = new Usuario();
        u.setId(7L);
        u.setNombres("Sofía");
        u.setApellidos("Morales");
        u.setUsername("smorales");
        u.setPassword("x");
        u.setRol(Rol.LECTOR);
        u.setEstado(EstadoUsuario.ACTIVO);
        return u;
    }

    @Test
    @DisplayName("RF-12: al liberarse una unidad se aparta para la primera reserva pendiente")
    void liberarUnidadAsignaReservaPendiente() {
        Reserva pendiente = new Reserva();
        pendiente.setId(1L);
        pendiente.setUsuario(lector());
        pendiente.setLibro(libro);
        pendiente.setEstado(EstadoReserva.PENDIENTE);
        when(reservaRepository.findFirstByLibroIdAndEstadoOrderByFechaReservaAsc(20L, EstadoReserva.PENDIENTE))
                .thenReturn(Optional.of(pendiente));

        Optional<Reserva> asignada = service.liberarUnidad(libro);

        assertThat(asignada).contains(pendiente);
        assertThat(pendiente.getEstado()).isEqualTo(EstadoReserva.DISPONIBLE);
        assertThat(pendiente.getFechaExpiracion()).isEqualTo(LocalDate.now().plusDays(3));
        assertThat(libro.getCantidadDisponible()).isZero();
    }

    @Test
    @DisplayName("Sin reservas en espera, la unidad vuelve al stock disponible")
    void liberarUnidadSinReservasAumentaStock() {
        when(reservaRepository.findFirstByLibroIdAndEstadoOrderByFechaReservaAsc(20L, EstadoReserva.PENDIENTE))
                .thenReturn(Optional.empty());

        assertThat(service.liberarUnidad(libro)).isEmpty();
        assertThat(libro.getCantidadDisponible()).isEqualTo(1);
    }

    @Test
    @DisplayName("RF-11: no se puede reservar un libro que tiene ejemplares disponibles")
    void noReservaLibroConStock() {
        Usuario u = lector();
        libro.setCantidadDisponible(1);
        when(securityUtils.usuarioActual()).thenReturn(new UsuarioPrincipal(u));
        when(usuarioService.buscarEntidad(7L)).thenReturn(u);
        when(libroRepository.findById(20L)).thenReturn(Optional.of(libro));

        assertThatThrownBy(() -> service.registrar(new ReservaRequest(20L, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("tiene ejemplares disponibles");
        verify(reservaRepository, never()).save(any());
    }
}
