package com.biblioteca.service;

import com.biblioteca.dto.PrestamoRequest;
import com.biblioteca.dto.PrestamoResponse;
import com.biblioteca.exception.ReglaNegocioException;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Reserva;
import com.biblioteca.model.Usuario;
import com.biblioteca.model.enums.EstadoPrestamo;
import com.biblioteca.model.enums.EstadoReserva;
import com.biblioteca.model.enums.EstadoUsuario;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.ReservaRepository;
import com.biblioteca.security.SecurityUtils;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {

    @Mock PrestamoRepository prestamoRepository;
    @Mock LibroRepository libroRepository;
    @Mock ReservaRepository reservaRepository;
    @Mock UsuarioService usuarioService;
    @Mock SecurityUtils securityUtils;

    PrestamoService service;
    Usuario usuario;
    Libro libro;

    @BeforeEach
    void setUp() {
        service = new PrestamoService(prestamoRepository, libroRepository, reservaRepository,
                usuarioService, securityUtils, 14, 3);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombres("Juan");
        usuario.setApellidos("Pérez");
        usuario.setDocumento("45678912");
        usuario.setEstado(EstadoUsuario.ACTIVO);

        libro = new Libro();
        libro.setId(10L);
        libro.setTitulo("Cien años de soledad");
        libro.setIsbn("978-0307474728");
        libro.setCantidadTotal(3);
        libro.setCantidadDisponible(2);
        libro.setActivo(true);

        when(usuarioService.buscarEntidad(1L)).thenReturn(usuario);
    }

    private void libroExiste() {
        when(libroRepository.findById(10L)).thenReturn(Optional.of(libro));
    }

    @Test
    @DisplayName("RF-08: registra el préstamo, descuenta un ejemplar y vence en 14 días")
    void registrarPrestamoCorrecto() {
        libroExiste();
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> {
            Prestamo p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        PrestamoResponse r = service.registrar(new PrestamoRequest(1L, 10L, null));

        assertThat(r.estado()).isEqualTo(EstadoPrestamo.ACTIVO);
        assertThat(r.fechaVencimiento()).isEqualTo(LocalDate.now().plusDays(14));
        assertThat(libro.getCantidadDisponible()).isEqualTo(1);
        verify(usuarioService).validarHabilitado(usuario);
    }

    @Test
    @DisplayName("RN-04: sin ejemplares disponibles no se presta")
    void sinEjemplaresLanzaExcepcion() {
        libroExiste();
        libro.setCantidadDisponible(0);

        assertThatThrownBy(() -> service.registrar(new PrestamoRequest(1L, 10L, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("No hay ejemplares disponibles");
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN-02: no puede superar 3 préstamos activos")
    void superaMaximoDePrestamos() {
        when(prestamoRepository.countByUsuarioIdAndEstadoIn(eq(1L), anyCollection())).thenReturn(3L);

        assertThatThrownBy(() -> service.registrar(new PrestamoRequest(1L, 10L, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("máximo 3");
    }

    @Test
    @DisplayName("RN-03: un usuario suspendido no puede pedir préstamos")
    void usuarioSuspendido() {
        doThrow(new ReglaNegocioException("El usuario está suspendido"))
                .when(usuarioService).validarHabilitado(usuario);

        assertThatThrownBy(() -> service.registrar(new PrestamoRequest(1L, 10L, null)))
                .isInstanceOf(ReglaNegocioException.class);
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN-04: con una reserva DISPONIBLE usa la unidad apartada y la reserva queda ATENDIDA")
    void usaUnidadApartadaPorReserva() {
        libroExiste();
        libro.setCantidadDisponible(0);
        Reserva reserva = new Reserva();
        reserva.setId(5L);
        reserva.setEstado(EstadoReserva.DISPONIBLE);
        when(reservaRepository.findFirstByUsuarioIdAndLibroIdAndEstado(1L, 10L, EstadoReserva.DISPONIBLE))
                .thenReturn(Optional.of(reserva));
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> {
            Prestamo p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });

        service.registrar(new PrestamoRequest(1L, 10L, null));

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.ATENDIDA);
        assertThat(libro.getCantidadDisponible()).isZero();
    }
}
