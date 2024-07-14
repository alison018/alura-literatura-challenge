package com.aluraLiteratura.literaturaChallenge.repository;

import com.aluraLiteratura.literaturaChallenge.model.Idioma;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IIdiomaRepository extends JpaRepository<Idioma, Long> {
    Idioma findByCodigo(String codigo);

}