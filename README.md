# Alura Literatura Challenge

Este proyecto es una aplicación de consola que permite buscar y gestionar libros y autores utilizando la API de Gutendex y una base de datos local. La aplicación permite buscar libros por título, mostrar libros buscados, mostrar autores buscados, listar autores vivos en un año determinado, listar libros por idioma, generar estadísticas, mostrar el top 10 de libros más descargados y buscar autores por nombre.

## Tabla de Contenidos

- [Instalación](#instalación)
- [Uso](#uso)
- [Dependencias](#dependencias)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Contribución](#contribución)
- [Licencia](#licencia)

## Instalación

1. Clona el repositorio:
    ```bash
    git clone https://github.com/tu-usuario/alura-literatura-challenge.git
    ```
2. Navega al directorio del proyecto:
    ```bash
    cd alura-literatura-challenge
    ```
3. Configura tu entorno de desarrollo y asegúrate de tener las dependencias necesarias instaladas.

## Uso

1. Inicia la aplicación:
    ```bash
    ./mvnw spring-boot:run
    ```
2. Sigue las instrucciones en la consola para interactuar con la aplicación.

## Dependencias

- Java 17
- Spring Boot
- Hibernate
- H2 Database (o tu base de datos preferida)
- Maven

## Estructura del Proyecto

```alura-literatura-challenge
│
├── src
│ ├── main
│ │ ├── java
│ │ │ └── com
│ │ │ └── aluraLiteratura
│ │ │ └── literaturaChallenge
│ │ │ ├── exception
│ │ │ │ └── LibroNoEncontradoException.java
│ │ │ ├── model
│ │ │ │ ├── Autor.java
│ │ │ │ ├── Idioma.java
│ │ │ │ ├── Libro.java
│ │ │ │ └── DatosLibro.java
│ │ │ ├── repository
│ │ │ │ ├── IAutorRepository.java
│ │ │ │ ├── IIdiomaRepository.java
│ │ │ │ └── ILibroRepository.java
│ │ │ ├── service
│ │ │ │ ├── ConsumoAPI.java
│ │ │ │ └── ConvierteDatos.java
│ │ │ └── principal
│ │ │ └── Principal.java
│ │ └── resources
│ │ └── application.properties
│ └── test
│ └── java
│ └── com
│ └── aluraLiteratura
│ └── literaturaChallenge
│ └── PrincipalTests.java
│
├── .gitignore
├── README.md
└── pom.xml
```

## Contribución

1. Haz un fork del proyecto.
2. Crea una nueva rama (`git checkout -b feature/nueva-caracteristica`).
3. Realiza tus cambios y haz commit (`git commit -m 'Añadir nueva característica'`).
4. Sube tus cambios (`git push origin feature/nueva-caracteristica`).
5. Abre un Pull Request.

## Licencia

Este proyecto está licenciado bajo la Licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.
