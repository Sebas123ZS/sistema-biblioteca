package com.biblioteca.config;

import com.biblioteca.model.*;
import com.biblioteca.model.enums.*;
import com.biblioteca.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Carga datos de ejemplo la primera vez que se ejecuta el sistema (base vacía).
 * Credenciales:  admin / Admin123   (BIBLIOTECARIO)
 *                jperez / Lector123 (LECTOR)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final AutorRepository autorRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final DevolucionRepository devolucionRepository;
    private final ReservaRepository reservaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository,
                           AutorRepository autorRepository, LibroRepository libroRepository,
                           PrestamoRepository prestamoRepository, DevolucionRepository devolucionRepository,
                           ReservaRepository reservaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.autorRepository = autorRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.devolucionRepository = devolucionRepository;
        this.reservaRepository = reservaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        log.info("Base de datos vacía: cargando datos de ejemplo...");

        // ---------- Usuarios ----------
        Usuario admin = usuario("María", "Torres Quispe", "70000001", "admin@biblioteca.pe", "987654321",
                "Jr. Ica 245, Huancayo", "admin", "Admin123", Rol.BIBLIOTECARIO);
        Usuario jperez = usuario("Juan Carlos", "Pérez Rojas", "45678912", "juan.perez@correo.com", "987654322",
                "Av. Los Rosales 123, Huancayo", "jperez", "Lector123", Rol.LECTOR);
        Usuario lmartinez = usuario("Lucía", "Martínez Soto", "46781234", "lucia.martinez@correo.com", "912345678",
                "Calle Real 540, El Tambo", "lmartinez", "Lector123", Rol.LECTOR);
        Usuario arodriguez = usuario("Andrés", "Rodríguez Paz", "47891234", "andres.rodriguez@correo.com", "934567890",
                "Jr. Puno 88, Huancayo", "arodriguez", "Lector123", Rol.LECTOR);
        Usuario vperez = usuario("Valentina", "Pérez Huamán", "48912345", "valentina.perez@correo.com", "987123456",
                "Av. Huancavelica 1020, El Tambo", "vperez", "Lector123", Rol.LECTOR);
        Usuario smorales = usuario("Sofía", "Morales Vega", "49123456", "sofia.morales@correo.com", "915678234",
                "Psje. Las Flores 12, Chilca", "smorales", "Lector123", Rol.LECTOR);
        Usuario dgarcia = usuario("Diego", "García León", "41234567", "diego.garcia@correo.com", "901234567",
                "Av. Ferrocarril 300, Huancayo", "dgarcia", "Lector123", Rol.LECTOR);
        dgarcia.setEstado(EstadoUsuario.SUSPENDIDO);

        // ---------- Categorías ----------
        Map<String, Categoria> cat = new HashMap<>();
        String[][] categorias = {
                {"Literatura", "Novela y narrativa en general"}, {"Clásicos", "Obras clásicas universales"},
                {"Infantil", "Literatura para niños"}, {"Historia", "Historia y ensayo histórico"},
                {"Ciencia ficción", "Ficción especulativa y futurista"}, {"Romance", "Novela romántica"},
                {"Misterio", "Suspenso y misterio"}, {"Thriller", "Novela de intriga"},
                {"Literatura peruana", "Autores peruanos"}, {"Tecnología", "Informática y programación"}};
        for (String[] c : categorias) {
            cat.put(c[0], categoriaRepository.save(new Categoria(c[0], c[1])));
        }

        // ---------- Autores ----------
        Map<String, Autor> au = new HashMap<>();
        String[][] autores = {
                {"Gabriel", "García Márquez", "Colombiana"}, {"Miguel", "de Cervantes", "Española"},
                {"Antoine", "de Saint-Exupéry", "Francesa"}, {"Yuval Noah", "Harari", "Israelí"},
                {"Markus", "Zusak", "Australiana"}, {"George", "Orwell", "Británica"},
                {"Jane", "Austen", "Británica"}, {"Carlos", "Ruiz Zafón", "Española"},
                {"Dan", "Brown", "Estadounidense"}, {"Frank", "Herbert", "Estadounidense"},
                {"Mario", "Vargas Llosa", "Peruana"}, {"José María", "Arguedas", "Peruana"},
                {"César", "Vallejo", "Peruana"}, {"Paulo", "Coelho", "Brasileña"},
                {"Robert C.", "Martin", "Estadounidense"}};
        for (String[] a : autores) {
            au.put(a[1], autorRepository.save(new Autor(a[0], a[1], a[2])));
        }

        // ---------- Libros ----------
        Libro cien = libro("978-0307474728", "Cien años de soledad", "Vintage Español", 1967, cat.get("Literatura"), 5, au.get("García Márquez"));
        Libro quijote = libro("978-8424116583", "Don Quijote de la Mancha", "Espasa", 1605, cat.get("Clásicos"), 4, au.get("de Cervantes"));
        Libro principito = libro("978-0156012195", "El principito", "Harcourt", 1943, cat.get("Infantil"), 2, au.get("de Saint-Exupéry"));
        Libro sapiens = libro("978-0062316097", "Sapiens: De animales a dioses", "Debate", 2011, cat.get("Historia"), 3, au.get("Harari"));
        Libro ladrona = libro("978-0375842207", "La ladrona de libros", "Lumen", 2005, cat.get("Literatura"), 4, au.get("Zusak"));
        Libro orwell = libro("978-0451524935", "1984", "Debolsillo", 1949, cat.get("Ciencia ficción"), 3, au.get("Orwell"));
        libro("978-0141439518", "Orgullo y prejuicio", "Penguin Clásicos", 1813, cat.get("Romance"), 3, au.get("Austen"));
        libro("978-0143034902", "La sombra del viento", "Planeta", 2001, cat.get("Misterio"), 4, au.get("Ruiz Zafón"));
        libro("978-0307474278", "El código Da Vinci", "Umbriel", 2003, cat.get("Thriller"), 6, au.get("Brown"));
        libro("978-0441013593", "Dune", "Debolsillo", 1965, cat.get("Ciencia ficción"), 4, au.get("Herbert"));
        libro("978-8420412146", "La ciudad y los perros", "Alfaguara", 1963, cat.get("Literatura peruana"), 5, au.get("Vargas Llosa"));
        libro("978-6124212183", "Los ríos profundos", "Peisa", 1958, cat.get("Literatura peruana"), 3, au.get("Arguedas"));
        libro("978-8437604688", "Los heraldos negros", "Cátedra", 1918, cat.get("Literatura peruana"), 2, au.get("Vallejo"));
        libro("978-0062315007", "El alquimista", "Planeta", 1988, cat.get("Literatura"), 5, au.get("Coelho"));
        libro("978-0132350884", "Código limpio", "Anaya", 2008, cat.get("Tecnología"), 3, au.get("Martin"));

        LocalDate hoy = LocalDate.now();

        // ---------- Préstamos y devoluciones de ejemplo ----------
        prestamo(jperez, principito, hoy.minusDays(4), hoy.plusDays(10), EstadoPrestamo.ACTIVO);
        prestamo(lmartinez, principito, hoy.minusDays(2), hoy.plusDays(12), EstadoPrestamo.ACTIVO);
        principito.setCantidadDisponible(0);

        prestamo(vperez, cien, hoy.minusDays(3), hoy.plusDays(11), EstadoPrestamo.ACTIVO);
        cien.setCantidadDisponible(cien.getCantidadDisponible() - 1);

        // Vence en el pasado: el sistema lo marcará como VENCIDO al iniciar.
        prestamo(arodriguez, orwell, hoy.minusDays(20), hoy.minusDays(6), EstadoPrestamo.ACTIVO);
        orwell.setCantidadDisponible(orwell.getCantidadDisponible() - 1);

        Prestamo devuelto1 = prestamo(smorales, sapiens, hoy.minusDays(25), hoy.minusDays(11), EstadoPrestamo.DEVUELTO);
        devolucion(devuelto1, hoy.minusDays(12), 0);
        Prestamo devuelto2 = prestamo(jperez, quijote, hoy.minusDays(30), hoy.minusDays(16), EstadoPrestamo.DEVUELTO);
        devolucion(devuelto2, hoy.minusDays(14), 2);
        Prestamo devuelto3 = prestamo(lmartinez, ladrona, hoy.minusDays(18), hoy.minusDays(4), EstadoPrestamo.DEVUELTO);
        devolucion(devuelto3, hoy.minusDays(5), 0);

        // ---------- Reserva en espera (El principito no tiene ejemplares) ----------
        Reserva r = new Reserva();
        r.setUsuario(smorales);
        r.setLibro(principito);
        r.setEstado(EstadoReserva.PENDIENTE);
        r.setFechaReserva(LocalDateTime.now().minusDays(1));
        reservaRepository.save(r);

        log.info("Datos de ejemplo cargados. Ingrese con admin / Admin123 o jperez / Lector123");
    }

    private Usuario usuario(String nombres, String apellidos, String doc, String correo, String tel, String dir,
                            String username, String password, Rol rol) {
        Usuario u = new Usuario();
        u.setNombres(nombres);
        u.setApellidos(apellidos);
        u.setDocumento(doc);
        u.setCorreo(correo);
        u.setTelefono(tel);
        u.setDireccion(dir);
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setRol(rol);
        u.setEstado(EstadoUsuario.ACTIVO);
        return usuarioRepository.save(u);
    }

    private Libro libro(String isbn, String titulo, String editorial, int anio, Categoria categoria, int cantidad,
                        Autor autor) {
        Libro l = new Libro();
        l.setIsbn(isbn);
        l.setTitulo(titulo);
        l.setEditorial(editorial);
        l.setAnioPublicacion(anio);
        l.setCategoria(categoria);
        l.setAutores(new java.util.HashSet<>(Set.of(autor)));
        l.setCantidadTotal(cantidad);
        l.setCantidadDisponible(cantidad);
        l.setPortadaUrl("https://covers.openlibrary.org/b/isbn/" + isbn.replace("-", "") + "-M.jpg");
        l.setActivo(true);
        return libroRepository.save(l);
    }

    private Prestamo prestamo(Usuario u, Libro l, LocalDate desde, LocalDate hasta, EstadoPrestamo estado) {
        Prestamo p = new Prestamo();
        p.setUsuario(u);
        p.setLibro(l);
        p.setFechaPrestamo(desde);
        p.setFechaVencimiento(hasta);
        p.setEstado(estado);
        return prestamoRepository.save(p);
    }

    private void devolucion(Prestamo p, LocalDate fecha, int retraso) {
        Devolucion d = new Devolucion();
        d.setPrestamo(p);
        d.setFechaDevolucion(fecha);
        d.setDiasRetraso(retraso);
        d.setEstadoLibro(EstadoLibroDevuelto.BUENO);
        devolucionRepository.save(d);
        p.setDevolucion(d);
    }
}
