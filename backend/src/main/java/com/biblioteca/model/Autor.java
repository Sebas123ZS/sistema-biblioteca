package com.biblioteca.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "autores")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombres;

    @Column(nullable = false, length = 80)
    private String apellidos;

    @Column(length = 60)
    private String nacionalidad;

    @Column(length = 1000)
    private String biografia;

    /** Lado inverso de la relación N:M (el dueño es Libro). */
    @ManyToMany(mappedBy = "autores")
    private Set<Libro> libros = new HashSet<>();

    public Autor() {
    }

    public Autor(String nombres, String apellidos, String nacionalidad) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.nacionalidad = nacionalidad;
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }
    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }
    public Set<Libro> getLibros() { return libros; }
    public void setLibros(Set<Libro> libros) { this.libros = libros; }
}
