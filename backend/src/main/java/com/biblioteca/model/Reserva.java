package com.biblioteca.model;

import com.biblioteca.model.enums.EstadoReserva;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** La reserva se hace sobre el título (libro), no sobre una copia concreta. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    /** Fecha y hora exacta: define el orden de la lista de espera (FIFO). */
    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    /** Día en que se le apartó una unidad devuelta. */
    @Column(name = "fecha_disponible")
    private LocalDate fechaDisponible;

    /** Último día para retirar el libro apartado. */
    @Column(name = "fecha_expiracion")
    private LocalDate fechaExpiracion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @PrePersist
    void alCrear() {
        if (fechaReserva == null) {
            fechaReserva = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }
    public LocalDateTime getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(LocalDateTime fechaReserva) { this.fechaReserva = fechaReserva; }
    public LocalDate getFechaDisponible() { return fechaDisponible; }
    public void setFechaDisponible(LocalDate fechaDisponible) { this.fechaDisponible = fechaDisponible; }
    public LocalDate getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDate fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
}
