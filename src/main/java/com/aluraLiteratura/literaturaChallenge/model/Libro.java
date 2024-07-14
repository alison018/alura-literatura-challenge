package com.aluraLiteratura.literaturaChallenge.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "libros")
@Access(AccessType.PROPERTY)
public class Libro {
    private Long id;
    private String titulo;
    private Integer numeroDescargas;
    private Set<Autor> autores = new HashSet<>();
    private Set<Idioma> idiomas = new HashSet<>();

    public Libro() {}

    public Libro(DatosLibro datos) {
        this.titulo = datos.titulo();
        this.numeroDescargas = datos.numeroDescargas();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getNumeroDescargas() {
        return numeroDescargas;
    }

    public void setNumeroDescargas(Integer numeroDescargas) {
        this.numeroDescargas = numeroDescargas;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "libro_autor",
            joinColumns = @JoinColumn(name = "libro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    public Set<Autor> getAutores() {
        return autores;
    }

    public void setAutores(Set<Autor> autores) {
        this.autores = autores;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "libro_idioma",
            joinColumns = @JoinColumn(name = "libro_id"),
            inverseJoinColumns = @JoinColumn(name = "idioma_id")
    )
    public Set<Idioma> getIdiomas() {
        return idiomas;
    }

    public void setIdiomas(Set<Idioma> idiomas) {
        this.idiomas = idiomas;
    }

    public void addAutor(Autor autor) {
        this.autores.add(autor);
        autor.getLibros().add(this);
    }

    public void removeAutor(Autor autor) {
        this.autores.remove(autor);
        autor.getLibros().remove(this);
    }

    public void addIdioma(Idioma idioma) {
        this.idiomas.add(idioma);
        idioma.getLibros().add(this);
    }

    public void removeIdioma(Idioma idioma) {
        this.idiomas.remove(idioma);
        idioma.getLibros().remove(this);
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", numeroDescargas=" + numeroDescargas +
                ", autores=" + autores +
                ", idiomas=" + idiomas;
    }
}
