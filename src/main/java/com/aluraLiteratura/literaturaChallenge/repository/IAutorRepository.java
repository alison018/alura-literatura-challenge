package com.aluraLiteratura.literaturaChallenge.repository;

import com.aluraLiteratura.literaturaChallenge.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IAutorRepository extends JpaRepository<Autor, Long> {
    List<Autor> findByNombre(String nombre);

    @Query("SELECT a FROM Autor a JOIN FETCH a.libros WHERE UPPER(a.nombre) LIKE UPPER(CONCAT('%', :nombre, '%'))")
    List<Autor> findByNombreContainsIgnoreCase(@Param("nombre") String nombre);

    @Query("SELECT a FROM Autor a LEFT JOIN FETCH a.libros WHERE a.fechaNacimiento <= :ano AND (a.fechaFallecimiento IS NULL OR a.fechaFallecimiento >= :ano)")
    List<Autor> findByAnoVivo(@Param("ano") int ano);

    @Query("SELECT DISTINCT a FROM Autor a LEFT JOIN FETCH a.libros")
    List<Autor> findAllWithLibros();
}
