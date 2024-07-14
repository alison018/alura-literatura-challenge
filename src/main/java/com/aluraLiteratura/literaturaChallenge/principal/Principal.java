package com.aluraLiteratura.literaturaChallenge.principal;

import com.aluraLiteratura.literaturaChallenge.exception.LibroNoEncontradoException;
import com.aluraLiteratura.literaturaChallenge.model.*;
import com.aluraLiteratura.literaturaChallenge.repository.IAutorRepository;
import com.aluraLiteratura.literaturaChallenge.repository.IIdiomaRepository;
import com.aluraLiteratura.literaturaChallenge.repository.ILibroRepository;
import com.aluraLiteratura.literaturaChallenge.service.ConsumoAPI;
import com.aluraLiteratura.literaturaChallenge.service.ConvierteDatos;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.DoubleSummaryStatistics;

@Component
public class Principal {
    private static final Logger logger = Logger.getLogger(Principal.class.getName());
    private final Scanner teclado = new Scanner(System.in);
    private final ConsumoAPI consumoApi;
    private final ConvierteDatos conversor;
    private final List<DatosLibro> datosLibros = new ArrayList<>();
    private final ILibroRepository libroRepository;
    private final IAutorRepository autorRepository;
    private final IIdiomaRepository idiomaRepository;

    @Autowired
    public Principal(ILibroRepository libroRepository, IAutorRepository autorRepository, IIdiomaRepository idiomaRepository, ConsumoAPI consumoApi, ConvierteDatos conversor) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
        this.idiomaRepository = idiomaRepository;
        this.consumoApi = consumoApi;
        this.conversor = conversor;
    }

    @PostConstruct
    @Transactional
    public void init() {
        // Cargar datos desde la base de datos dentro de una transacción
        List<Libro> libros = libroRepository.findAll();
        datosLibros.addAll(libros.stream().map(libro -> new DatosLibro(
                libro.getTitulo(),
                libro.getAutores().stream().map(autor -> new DatosAutor(autor.getNombre(), autor.getFechaNacimiento(), autor.getFechaFallecimiento())).collect(Collectors.toList()),
                libro.getIdiomas().stream().map(Idioma::getCodigo).collect(Collectors.toList()),
                libro.getNumeroDescargas()
        )).toList());
    }

    public void muestraElMenu() {
        int opcion = -1;
        while (opcion != 0) {
            String menu = """
                1 - Buscar libro por título
                2 - Mostrar libros buscados
                3 - Mostrar autores buscados
                4 - Listar autores vivos en un año determinado
                5 - Listar libros por idioma
                6 - Generar estadísticas
                7 - Mostrar top 10 libros más descargados
                8 - Buscar autor por nombre
                0 - Salir
                """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1 -> buscarLibroPorTitulo();
                case 2 -> mostrarLibrosBuscados();
                case 3 -> mostrarAutoresBuscados();
                case 4 -> listarAutoresVivosEnAnoDeterminado();
                case 5 -> listarLibrosPorIdioma();
                case 6 -> generarEstadisticas();
                case 7 -> mostrarTop10Libros();
                case 8 -> buscarAutorPorNombre();
                case 0 -> System.out.println("Cerrando la aplicación...");
                default -> System.out.println("Opción inválida");
            }
        }
    }

    private List<DatosLibro> getDatosLibro() {
        System.out.println("Escribe el título del libro que deseas buscar:");
        String tituloLibro = teclado.nextLine();
        List<DatosLibro> datosLibros = consumoApi.obtenerDatosLibro(tituloLibro);

        if (datosLibros == null || datosLibros.isEmpty()) {
            throw new LibroNoEncontradoException("No se recibieron datos de la API.");
        }

        return datosLibros;
    }

    @Transactional
    public void buscarLibroPorTitulo() {
        try {
            List<DatosLibro> datosLibros = getDatosLibro();
            for (DatosLibro datos : datosLibros) {
                List<Libro> librosExistentes = libroRepository.findByTitulo(datos.titulo());
                if (!librosExistentes.isEmpty()) {
                    System.out.println("El libro \"" + datos.titulo() + "\" ya ha sido buscado anteriormente.");
                    continue; // Saltar al siguiente libro si ya existe
                }

                Set<Autor> autores = new HashSet<>();
                for (DatosAutor datoAutor : datos.autores()) {
                    Autor autor = autorRepository.findByNombre(datoAutor.nombre())
                            .stream()
                            .findFirst()
                            .orElseGet(() -> {
                                Autor nuevoAutor = new Autor();
                                nuevoAutor.setNombre(datoAutor.nombre());
                                nuevoAutor.setFechaNacimiento(datoAutor.fechaNacimiento());
                                nuevoAutor.setFechaFallecimiento(datoAutor.fechaFallecimiento());
                                return nuevoAutor;
                            });
                    if (autor.getId() == null) {
                        autorRepository.saveAndFlush(autor);
                    }
                    autores.add(autor);
                }

                Set<Idioma> idiomas = new HashSet<>();
                for (String datoIdioma : datos.idiomas()) {
                    Idioma idioma = idiomaRepository.findByCodigo(datoIdioma);
                    if (idioma == null) {
                        idioma = new Idioma();
                        idioma.setCodigo(datoIdioma);
                        idioma.setNombre(datoIdioma);
                        idiomaRepository.saveAndFlush(idioma);
                    }
                    idiomas.add(idioma);
                }

                Libro libro = new Libro(datos);
                for (Autor autor : autores) {
                    libro.addAutor(autor);
                }
                for (Idioma idioma : idiomas) {
                    libro.addIdioma(idioma);
                }
                libro.setTitulo(truncar(datos.titulo(), 255));

                libroRepository.save(libro);

                this.datosLibros.add(datos);
                System.out.println("Libro añadido a datosLibros: " + datos.titulo());
                System.out.println("Libros buscados hasta ahora: " + this.datosLibros.size());
            }
        } catch (LibroNoEncontradoException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Ocurrió un error al buscar el libro: " + e.getMessage());
        }
    }


    private String truncar(String texto, int longitudMaxima) {
        if (texto.length() > longitudMaxima) {
            return texto.substring(0, longitudMaxima);
        }
        return texto;
    }

    private void mostrarAutoresBuscados() {
        List<Autor> autores = autorRepository.findAllWithLibros();
        if (autores.isEmpty()) {
            System.out.println("No se encontraron autores.");
            return;
        }

        for (Autor autor : autores) {
            System.out.println("Nombre: " + autor.getNombre());
            System.out.println("Fecha de nacimiento: " + autor.getFechaNacimiento());
            System.out.println("Fecha de fallecimiento: " + autor.getFechaFallecimiento());
            List<String> libros = autor.getLibros().stream()
                    .map(Libro::getTitulo)
                    .collect(Collectors.toList());
            String librosConcatenados = String.join(", ", libros);
            System.out.println("Libros: " + librosConcatenados);
            System.out.println("-------------");
        }
    }

    private void listarAutoresVivosEnAnoDeterminado() {
        System.out.println("Introduce el año para listar autores vivos:");
        int ano = teclado.nextInt();
        teclado.nextLine();
        List<Autor> autores = autorRepository.findByAnoVivo(ano); // Buscar autores vivos en el año especificado

        if (autores.isEmpty()) {
            System.out.println("No se encontraron autores vivos en el año especificado.");
            return;
        }

        // Inicializar listas para almacenar los datos
        List<String> nombres = new ArrayList<>();
        List<String> fechasNacimiento = new ArrayList<>();
        List<String> fechasFallecimiento = new ArrayList<>();
        List<String> librosList = new ArrayList<>();

        // Llenar las listas con los datos de los autores
        for (Autor autor : autores) {
            nombres.add(autor.getNombre());
            fechasNacimiento.add(String.valueOf(autor.getFechaNacimiento()));
            fechasFallecimiento.add(autor.getFechaFallecimiento() != null ? String.valueOf(autor.getFechaFallecimiento()) : "N/A");
            librosList.add(autor.getLibros().stream().map(Libro::getTitulo).collect(Collectors.joining(", ")));
        }

        // Encontrar la longitud máxima de cada columna
        int maxNombreLength = Math.max("Autor".length(), nombres.stream().mapToInt(String::length).max().orElse(0));
        int maxFechaNacimientoLength = Math.max("Fecha de Nacimiento".length(), fechasNacimiento.stream().mapToInt(String::length).max().orElse(0));
        int maxFechaFallecimientoLength = Math.max("Fecha de Fallecimiento".length(), fechasFallecimiento.stream().mapToInt(String::length).max().orElse(0));
        int maxLibrosLength = Math.max("Libros".length(), librosList.stream().mapToInt(String::length).max().orElse(0));

        // Construir la tabla Markdown
        StringBuilder markdownTable = new StringBuilder();
        markdownTable.append(String.format("| %-" + maxNombreLength + "s | %-" + maxFechaNacimientoLength + "s | %-" + maxFechaFallecimientoLength + "s | %-" + maxLibrosLength + "s |\n",
                "Autor", "Fecha de Nacimiento", "Fecha de Fallecimiento", "Libros"));
        markdownTable.append(String.format("|-%s-|-%s-|-%s-|-%s-|\n",
                "-".repeat(maxNombreLength), "-".repeat(maxFechaNacimientoLength), "-".repeat(maxFechaFallecimientoLength), "-".repeat(maxLibrosLength)));

        for (int i = 0; i < autores.size(); i++) {
            markdownTable.append(String.format("| %-" + maxNombreLength + "s | %-" + maxFechaNacimientoLength + "s | %-" + maxFechaFallecimientoLength + "s | %-" + maxLibrosLength + "s |\n",
                    nombres.get(i), fechasNacimiento.get(i), fechasFallecimiento.get(i), librosList.get(i)));
        }

        // Imprimir la tabla Markdown
        System.out.println(markdownTable.toString());
    }

    private void listarLibrosPorIdioma() {
        // Mapa de códigos de idioma a nombres completos
        Map<String, String> nombresIdiomas = inicializarNombresIdiomas();

        // Mostrar el menú de selección de idiomas
        System.out.println("Seleccione un idioma:");
        List<String> codigosIdiomas = new ArrayList<>(nombresIdiomas.keySet());
        for (int i = 0; i < codigosIdiomas.size(); i++) {
            String codigo = codigosIdiomas.get(i);
            System.out.println((i + 1) + " - " + nombresIdiomas.get(codigo));
        }
        int opcionIdioma = teclado.nextInt();
        teclado.nextLine();

        if (opcionIdioma < 1 || opcionIdioma > codigosIdiomas.size()) {
            System.out.println("Opción inválida.");
            return;
        }

        String codigoIdioma = codigosIdiomas.get(opcionIdioma - 1);

        // Buscar libros que tienen el idioma especificado
        List<Libro> librosFiltrados = libroRepository.findByIdiomaCodigo(codigoIdioma);

        if (librosFiltrados.isEmpty()) {
            System.out.println("No se encontraron libros en el idioma especificado.");
            return;
        }

        // Contar la frecuencia de libros por idioma
        Map<String, Long> frecuenciaPorIdioma = librosFiltrados.stream()
                .flatMap(libro -> libro.getIdiomas().stream())
                .collect(Collectors.groupingBy(Idioma::getCodigo, Collectors.counting()));

        // Ordenar por frecuencia
        List<Map.Entry<String, Long>> sortedIdiomas = frecuenciaPorIdioma.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        // Calcular la longitud máxima de cada columna
        int maxIdiomaLength = Math.max("Idioma".length(), sortedIdiomas.stream().map(entry -> nombresIdiomas.getOrDefault(entry.getKey(), entry.getKey()).length()).max(Integer::compare).orElse(0));
        int maxNumeroDeLibrosLength = Math.max("Número de Libros".length(), sortedIdiomas.stream().map(entry -> entry.getValue().toString().length()).max(Integer::compare).orElse(0));

        // Construir la tabla Markdown
        StringBuilder markdownTable = new StringBuilder();
        markdownTable.append(String.format("| %-" + maxIdiomaLength + "s | %-" + maxNumeroDeLibrosLength + "s |\n", "Idioma", "Número de Libros"));
        markdownTable.append(String.format("|-%s-|-%s-|\n", "-".repeat(maxIdiomaLength), "-".repeat(maxNumeroDeLibrosLength)));

        for (Map.Entry<String, Long> entry : sortedIdiomas) {
            String idioma = entry.getKey();
            long count = entry.getValue();
            String nombreIdioma = nombresIdiomas.getOrDefault(idioma, idioma);

            markdownTable.append(String.format("| %-" + maxIdiomaLength + "s | %-" + maxNumeroDeLibrosLength + "d |\n", nombreIdioma, count));
        }

        // Mostrar la tabla Markdown
        System.out.println(markdownTable.toString());

        // Mostrar detalles de los libros
        for (Map.Entry<String, Long> entry : sortedIdiomas) {
            String idioma = entry.getKey();
            String nombreIdioma = nombresIdiomas.getOrDefault(idioma, idioma);

            System.out.println("### " + nombreIdioma + " (" + entry.getValue() + " libros)");

            librosFiltrados.stream()
                    .filter(libro -> libro.getIdiomas().stream().anyMatch(id -> id.getCodigo().equals(idioma)))
                    .forEach(libro -> {
                        System.out.println("Título: " + libro.getTitulo());
                        libro.getAutores().forEach(autor -> {
                            String[] nombrePartes = autor.getNombre().split(" ");
                            String apellido = nombrePartes[nombrePartes.length - 1];
                            String nombre = String.join(" ", Arrays.copyOfRange(nombrePartes, 0, nombrePartes.length - 1));
                            System.out.println("Autor: " + apellido + ", " + nombre);
                        });
                        System.out.println("Número de descargas: " + libro.getNumeroDescargas());
                        System.out.println("-------------");
                    });
        }
    }

    private Map<String, String> inicializarNombresIdiomas() {
        Map<String, String> nombresIdiomas = new HashMap<>();
        nombresIdiomas.put("en", "Inglés");
        nombresIdiomas.put("es", "Español");
        nombresIdiomas.put("fr", "Francés");
        nombresIdiomas.put("de", "Alemán");
        nombresIdiomas.put("it", "Italiano");
        nombresIdiomas.put("pt", "Portugués");
        nombresIdiomas.put("ar", "Árabe");
        nombresIdiomas.put("ru", "Ruso");
        nombresIdiomas.put("ja", "Japonés");
        nombresIdiomas.put("el", "Griego");
        nombresIdiomas.put("nl", "Holandés");
        nombresIdiomas.put("la", "Latín");
        nombresIdiomas.put("fi", "Finlandés");
        nombresIdiomas.put("sv", "Sueco");
        nombresIdiomas.put("pl", "Polaco");
        nombresIdiomas.put("tl", "Tagalo");
        return nombresIdiomas;
    }

    private void mostrarLibrosBuscados() {
        System.out.println("Mostrando libros buscados...");
        System.out.println("Tamaño de datosLibros: " + datosLibros.size());

        if (datosLibros.isEmpty()) {
            System.out.println("No se han buscado libros aún.");
            return;
        }

        // Inicializar listas para almacenar los datos
        List<String> titulos = new ArrayList<>();
        List<String> autoresList = new ArrayList<>();
        List<String> idiomasList = new ArrayList<>();
        List<String> descargasList = new ArrayList<>();

        // Llenar las listas con los datos de los libros
        for (DatosLibro datos : datosLibros) {
            titulos.add(datos.titulo());
            autoresList.add(datos.autores().stream().map(DatosAutor::nombre).collect(Collectors.joining(", ")));
            idiomasList.add(String.join(", ", datos.idiomas()));
            descargasList.add(String.valueOf(datos.numeroDescargas()));
        }

        // Encontrar la longitud máxima de cada columna
        int maxTituloLength = Math.max("Título".length(), titulos.stream().mapToInt(String::length).max().orElse(0));
        int maxAutoresLength = Math.max("Autores".length(), autoresList.stream().mapToInt(String::length).max().orElse(0));
        int maxIdiomasLength = Math.max("Idiomas".length(), idiomasList.stream().mapToInt(String::length).max().orElse(0));
        int maxDescargasLength = Math.max("Descargas".length(), descargasList.stream().mapToInt(String::length).max().orElse(0));

        // Construir la tabla Markdown
        StringBuilder markdownTable = new StringBuilder();
        markdownTable.append(String.format("| %-" + maxTituloLength + "s | %-" + maxAutoresLength + "s | %-" + maxIdiomasLength + "s | %-" + maxDescargasLength + "s |\n",
                "Título", "Autores", "Idiomas", "Descargas"));
        markdownTable.append(String.format("|-%s-|-%s-|-%s-|-%s-|\n",
                "-".repeat(maxTituloLength), "-".repeat(maxAutoresLength), "-".repeat(maxIdiomasLength), "-".repeat(maxDescargasLength)));

        for (int i = 0; i < datosLibros.size(); i++) {
            markdownTable.append(String.format("| %-" + maxTituloLength + "s | %-" + maxAutoresLength + "s | %-" + maxIdiomasLength + "s | %-" + maxDescargasLength + "s |\n",
                    titulos.get(i), autoresList.get(i), idiomasList.get(i), descargasList.get(i)));
        }

        // Imprimir la tabla Markdown
        System.out.println(markdownTable.toString());
    }

    public void generarEstadisticas() {
        DoubleSummaryStatistics estadisticasDescargas = datosLibros.stream()
                .mapToDouble(DatosLibro::numeroDescargas)
                .summaryStatistics();

        System.out.println("Estadísticas de descargas de libros:");
        System.out.println("Número de libros: " + estadisticasDescargas.getCount());
        System.out.println("Número total de descargas: " + estadisticasDescargas.getSum());
        System.out.println("Promedio de descargas: " + estadisticasDescargas.getAverage());
        System.out.println("Máximo de descargas: " + estadisticasDescargas.getMax());
        System.out.println("Mínimo de descargas: " + estadisticasDescargas.getMin());
    }

    public void mostrarTop10Libros() {
        // Utilizar un conjunto para eliminar duplicados
        Set<DatosLibro> librosUnicos = new HashSet<>(datosLibros);

        // Ordenar los libros por número de descargas en orden descendente
        List<DatosLibro> top10Libros = librosUnicos.stream()
                .sorted(Comparator.comparingInt(DatosLibro::numeroDescargas).reversed())
                .limit(10)
                .collect(Collectors.toList());

        System.out.println("Top 10 libros más descargados:");
        for (DatosLibro libro : top10Libros) {
            System.out.println("Título: " + libro.titulo() + " - Descargas: " + libro.numeroDescargas());
        }
    }

    public void buscarAutorPorNombre() {
        System.out.println("Escribe el nombre del autor que deseas buscar:");
        String nombreAutor = teclado.nextLine();
        List<Autor> autoresEncontrados = autorRepository.findByNombreContainsIgnoreCase(nombreAutor);

        if (autoresEncontrados.isEmpty()) {
            System.out.println("No se encontraron autores con el nombre especificado.");
            return;
        }

        for (Autor autor : autoresEncontrados) {
            System.out.println("Nombre: " + autor.getNombre());
            System.out.println("Fecha de nacimiento: " + autor.getFechaNacimiento());
            System.out.println("Fecha de fallecimiento: " + autor.getFechaFallecimiento());
            List<String> libros = autor.getLibros().stream()
                    .map(Libro::getTitulo)
                    .collect(Collectors.toList());
            String librosConcatenados = String.join(", ", libros);
            System.out.println("Libros: " + librosConcatenados);
            System.out.println("-------------");
        }
    }

    @Transactional(readOnly = true)
    public void probarRepositorios() {
        // Prueba obtener todos los libros directamente desde el repositorio
        List<Libro> libros = libroRepository.findAll();
        if (libros.isEmpty()) {
            logger.severe("No se han encontrado libros en la base de datos.");
        } else {
            System.out.println("Número total de libros en la base de datos: " + libros.size());
            libros.forEach(libro -> {
                System.out.println("Título: " + libro.getTitulo());
                libro.getAutores().forEach(autor -> {
                    String[] nombrePartes = autor.getNombre().split(" ");
                    String apellido = nombrePartes[nombrePartes.length - 1];
                    String nombre = String.join(" ", Arrays.copyOfRange(nombrePartes, 0, nombrePartes.length - 1));
                    System.out.println("Autor: " + apellido + ", " + nombre);
                });
                libro.getIdiomas().forEach(id -> System.out.println("Idioma: " + id.getNombre()));
                System.out.println("Número de descargas: " + libro.getNumeroDescargas());
                System.out.println("-------------");
            });
        }
    }
}
