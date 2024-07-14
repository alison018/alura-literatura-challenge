package com.aluraLiteratura.literaturaChallenge.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "idiomas")
@Access(AccessType.PROPERTY)
public class Idioma {
    private Long id;
    private String codigo;
    private String nombre;
    private Set<Libro> libros = new HashSet<>();

    public Idioma() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @ManyToMany(mappedBy = "idiomas", fetch = FetchType.LAZY)
    public Set<Libro> getLibros() {
        return libros;
    }

    public void setLibros(Set<Libro> libros) {
        this.libros = libros;
    }

    public void addLibro(Libro libro) {
        this.libros.add(libro);
        libro.getIdiomas().add(this);
    }

    public void removeLibro(Libro libro) {
        this.libros.remove(libro);
        libro.getIdiomas().remove(this);
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", libros=" + libros ;
    }
}
