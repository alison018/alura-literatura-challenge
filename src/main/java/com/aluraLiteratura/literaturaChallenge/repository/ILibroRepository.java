package com.aluraLiteratura.literaturaChallenge.repository;

import com.aluraLiteratura.literaturaChallenge.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ILibroRepository extends JpaRepository<Libro, Long> {

    // Método para buscar libros por idioma, incluyendo autores e idiomas en la carga
    @Query("SELECT l FROM Libro l JOIN l.idiomas i WHERE i.codigo = :codigo")
    List<Libro> findByIdiomaCodigo(@Param("codigo") String codigo);

    // Método para buscar libros por título
    List<Libro> findByTitulo(String titulo);
}
