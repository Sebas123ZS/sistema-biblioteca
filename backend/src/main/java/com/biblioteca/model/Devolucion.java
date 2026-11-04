package com.biblioteca.model;

import com.biblioteca.model.enums.EstadoLibroDevuelto;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "devoluciones")
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cada préstamo tiene como máximo una devolución (1:1). */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prestamo_id", nullable = false, unique = true)
    private Prestamo prestamo;

    @Column(name = "fecha_devolucion", nullable = false)
    private LocalDate fechaDevolucion;

    @Column(name = "dias_retraso", nullable = false)
    private Integer diasRetraso = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_libro", nullable = false, length = 20)
    private EstadoLibroDevuelto estadoLibro = EstadoLibroDevuelto.BUENO;

    @Column(length = 500)
    private String observaciones;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Prestamo getPrestamo() { return prestamo; }
    public void setPrestamo(Prestamo prestamo) { this.prestamo = prestamo; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
    public Integer getDiasRetraso() { return diasRetraso; }
    public void setDiasRetraso(Integer diasRetraso) { this.diasRetraso = diasRetraso; }
    public EstadoLibroDevuelto getEstadoLibro() { return estadoLibro; }
    public void setEstadoLibro(EstadoLibroDevuelto estadoLibro) { this.estadoLibro = estadoLibro; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
