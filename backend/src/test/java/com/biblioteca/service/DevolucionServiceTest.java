package com.biblioteca.service;

import com.biblioteca.dto.DevolucionRequest;
import com.biblioteca.dto.DevolucionResponse;
import com.biblioteca.exception.ReglaNegocioException;
import com.biblioteca.model.Devolucion;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Usuario;
import com.biblioteca.model.enums.EstadoLibroDevuelto;
import com.biblioteca.model.enums.EstadoPrestamo;
import com.biblioteca.repository.DevolucionRepository;
import com.biblioteca.repository.PrestamoRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevolucionServiceTest {

    @Mock DevolucionRepository devolucionRepository;
    @Mock PrestamoRepository prestamoRepository;
    @Mock ReservaService reservaService;
    @Mock SecurityUtils securityUtils;

    DevolucionService service;
    Prestamo prestamo;
    Libro libro;

    @BeforeEach
    void setUp() {
        service = new DevolucionService(devolucionRepository, prestamoRepository, reservaService, securityUtils);

        Usuario u = new Usuario();
        u.setId(1L);
        u.setNombres("Andrés");
        u.setApellidos("Rodríguez");

        libro = new Libro();
        libro.setId(3L);
        libro.setTitulo("1984");
        libro.setCantidadTotal(3);
        libro.setCantidadDisponible(2);

        prestamo = new Prestamo();
        prestamo.setId(9L);
        prestamo.setUsuario(u);
        prestamo.setLibro(libro);
        prestamo.setFechaPrestamo(LocalDate.now().minusDays(20));
        prestamo.setFechaVencimiento(LocalDate.now().minusDays(6));
        prestamo.setEstado(EstadoPrestamo.VENCIDO);

        when(prestamoRepository.findById(9L)).thenReturn(Optional.of(prestamo));
    }

    private void guardarDevuelveLoMismo() {
        when(devolucionRepository.save(any(Devolucion.class))).thenAnswer(inv -> {
            Devolucion d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });
    }

    @Test
    @DisplayName("RF-10: cierra el préstamo, calcula 6 días de retraso y libera la unidad")
    void registrarDevolucionConRetraso() {
        guardarDevuelveLoMismo();
        when(reservaService.liberarUnidad(libro)).thenReturn(Optional.empty());

        DevolucionResponse r = service.registrar(new DevolucionRequest(9L, EstadoLibroDevuelto.BUENO, null));

        assertThat(prestamo.getEstado()).isEqualTo(EstadoPrestamo.DEVUELTO);
        assertThat(r.diasRetraso()).isEqualTo(6);
        assertThat(r.fechaDevolucion()).isEqualTo(LocalDate.now());
        verify(reservaService).liberarUnidad(libro);
    }

    @Test
    @DisplayName("Un libro devuelto dañado se retira del inventario y no se libera")
    void libroDanadoReduceTotal() {
        guardarDevuelveLoMismo();

        service.registrar(new DevolucionRequest(9L, EstadoLibroDevuelto.DANADO, "Hojas rotas"));

        assertThat(libro.getCantidadTotal()).isEqualTo(2);
        assertThat(libro.getCantidadDisponible()).isEqualTo(2);
        verify(reservaService, never()).liberarUnidad(any());
    }

    @Test
    @DisplayName("No se puede devolver dos veces el mismo préstamo")
    void prestamoYaDevuelto() {
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);

        assertThatThrownBy(() -> service.registrar(new DevolucionRequest(9L, null, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya fue devuelto");
    }
}
