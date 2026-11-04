package com.biblioteca.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 120)
    private String editorial;

    @Column(name = "anio_publicacion")
    private Integer anioPublicacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    /** Relación N:M con tabla intermedia libro_autor (RF-05). */
    @ManyToMany
    @JoinTable(name = "libro_autor",
            joinColumns = @JoinColumn(name = "libro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id"))
    private Set<Autor> autores = new HashSet<>();

    /** Número total de ejemplares físicos que posee la biblioteca. */
    @Column(name = "cantidad_total", nullable = false)
    private Integer cantidadTotal;

    /** Ejemplares libres para préstamo inmediato (no prestados ni apartados por reserva). */
    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible;

    @Column(name = "portada_url", length = 500)
    private String portadaUrl;

    /** Baja lógica: un libro con historial no se borra físicamente. */
    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    /**
     * Control de concurrencia optimista: si dos operaciones modifican el mismo libro
     * al mismo tiempo (ej. dos préstamos del último ejemplar), la segunda falla
     * en lugar de dejar el stock en un valor incorrecto.
     */
    @Version
    @Column(name = "version", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private long version;

    @PrePersist
    void alCrear() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    public boolean estaDisponible() {
        return activo && cantidadDisponible != null && cantidadDisponible > 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getEditorial() { return editorial; }
    public void setEditorial(String editorial) { this.editorial = editorial; }
    public Integer getAnioPublicacion() { return anioPublicacion; }
    public void setAnioPublicacion(Integer anioPublicacion) { this.anioPublicacion = anioPublicacion; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public Set<Autor> getAutores() { return autores; }
    public void setAutores(Set<Autor> autores) { this.autores = autores; }
    public Integer getCantidadTotal() { return cantidadTotal; }
    public void setCantidadTotal(Integer cantidadTotal) { this.cantidadTotal = cantidadTotal; }
    public Integer getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(Integer cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
    public String getPortadaUrl() { return portadaUrl; }
    public void setPortadaUrl(String portadaUrl) { this.portadaUrl = portadaUrl; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public long getVersion() { return version; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
