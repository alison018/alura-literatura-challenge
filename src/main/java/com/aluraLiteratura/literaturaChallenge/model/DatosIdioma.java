package com.aluraLiteratura.literaturaChallenge.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public record DatosIdioma(
    String codigo,
    String nombre
) {
}
