-- =====================================================================
--  Consultas para las EVIDENCIAS de la Fase 5 (ejecutar en MySQL Workbench)
--  Requisito: el backend ya se inició una vez (tablas creadas y datos de ejemplo).
--  Cada bloque sirve para una captura del informe.
-- =====================================================================
USE biblioteca;

-- ---------------------------------------------------------------------
-- 1) Estructura: las 8 tablas y el detalle de una de ellas
-- ---------------------------------------------------------------------
SHOW TABLES;
DESCRIBE libros;

-- ---------------------------------------------------------------------
-- 2) Tablas con registros (capturas de datos)
-- ---------------------------------------------------------------------
SELECT id, nombre, descripcion FROM categorias;
SELECT id, nombres, apellidos, nacionalidad FROM autores;
SELECT id, isbn, titulo, cantidad_total, cantidad_disponible, activo FROM libros;
SELECT libro_id, autor_id FROM libro_autor;
SELECT id, username, nombres, apellidos, rol, estado,
       LEFT(password, 20) AS password_bcrypt   -- se ve que está cifrada
FROM usuarios;
SELECT id, usuario_id, libro_id, fecha_prestamo, fecha_vencimiento, estado FROM prestamos;
SELECT id, prestamo_id, fecha_devolucion, dias_retraso, estado_libro FROM devoluciones;
SELECT id, usuario_id, libro_id, fecha_reserva, estado FROM reservas;

-- Vista combinada (relaciones entre tablas): préstamos con usuario y libro
SELECT p.id AS prestamo, CONCAT(u.nombres, ' ', u.apellidos) AS usuario,
       l.titulo, p.fecha_prestamo, p.fecha_vencimiento, p.estado
FROM prestamos p
JOIN usuarios u ON u.id = p.usuario_id
JOIN libros   l ON l.id = p.libro_id
ORDER BY p.fecha_vencimiento;

-- ---------------------------------------------------------------------
-- 3) CRUD directo en SQL (equivale a lo que hace la API con JPA)
--    En el informe se prefieren las pruebas por Postman, que pasan por la API.
-- ---------------------------------------------------------------------
-- Create
INSERT INTO categorias (nombre, descripcion) VALUES ('Poesía', 'Prueba de CRUD');
-- Read
SELECT * FROM categorias WHERE nombre = 'Poesía';
-- Update
UPDATE categorias SET descripcion = 'Prueba de CRUD (actualizada)' WHERE nombre = 'Poesía';
SELECT * FROM categorias WHERE nombre = 'Poesía';
-- Delete
DELETE FROM categorias WHERE nombre = 'Poesía';
SELECT COUNT(*) AS quedan FROM categorias WHERE nombre = 'Poesía';

-- ---------------------------------------------------------------------
-- 4) SQL equivalente a las consultas JPQL del proyecto
-- ---------------------------------------------------------------------
-- LibroRepository.buscar (catálogo con texto, categoría y disponibilidad)
SELECT DISTINCT l.id, l.titulo, l.isbn, l.cantidad_disponible
FROM libros l
LEFT JOIN libro_autor la ON la.libro_id = l.id
LEFT JOIN autores a      ON a.id = la.autor_id
WHERE l.activo = TRUE
  AND (LOWER(l.titulo) LIKE '%soledad%' OR l.isbn LIKE '%soledad%'
       OR LOWER(CONCAT(a.nombres, ' ', a.apellidos)) LIKE '%soledad%')
  AND l.cantidad_disponible BETWEEN 1 AND 2147483647;

-- CategoriaRepository.contarLibrosActivosPorCategoria
SELECT c.id, c.nombre, COUNT(l.id) AS titulos
FROM categorias c
LEFT JOIN libros l ON l.categoria_id = c.id AND l.activo = TRUE
GROUP BY c.id, c.nombre
ORDER BY titulos DESC;

-- PrestamoRepository.marcarVencidos (UPDATE con @Modifying)
SELECT id, fecha_vencimiento, estado FROM prestamos
WHERE estado = 'ACTIVO' AND fecha_vencimiento < CURDATE();

-- PrestamoRepository.buscar con estado = ACTIVO,VENCIDO (préstamos pendientes)
SELECT id, estado FROM prestamos WHERE estado IN ('ACTIVO', 'VENCIDO');

-- ---------------------------------------------------------------------
-- 5) Transacciones: todo o nada
--    (la prueba principal se hace por la API: ver README, "Prueba transaccional")
-- ---------------------------------------------------------------------
START TRANSACTION;
  UPDATE libros SET cantidad_disponible = cantidad_disponible - 1 WHERE id = 1;
  SELECT id, titulo, cantidad_disponible FROM libros WHERE id = 1;   -- se ve el descuento
ROLLBACK;
SELECT id, titulo, cantidad_disponible FROM libros WHERE id = 1;     -- el valor original volvió

-- Bloqueo optimista: cada modificación de un libro incrementa su versión
SELECT id, titulo, version FROM libros ORDER BY version DESC;
