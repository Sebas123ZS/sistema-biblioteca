-- =====================================================================
--  Sistema de Gestión de Biblioteca · Script de base de datos (MySQL 8)
-- ---------------------------------------------------------------------
--  Este script es OPCIONAL: al arrancar, el backend crea la base
--  (createDatabaseIfNotExist=true) y las tablas (Hibernate, ddl-auto=update)
--  a partir de las entidades JPA. Se incluye para:
--    · documentar el modelo físico (8 tablas, claves y restricciones);
--    · crear la base a mano en MySQL Workbench si se prefiere.
--
--  Uso: ejecutar ANTES del primer arranque del backend, con la base vacía.
--  Los datos de ejemplo (usuarios con contraseña cifrada en BCrypt, libros,
--  préstamos, etc.) los carga DataInitializer al iniciar la aplicación.
-- =====================================================================

-- DROP DATABASE IF EXISTS biblioteca;   -- (descomentar solo para empezar de cero)
CREATE DATABASE IF NOT EXISTS biblioteca
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE biblioteca;

-- ---------------------------------------------------------------------
-- Catálogo
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categorias (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  nombre      VARCHAR(60)  NOT NULL,
  descripcion VARCHAR(255) NULL,
  CONSTRAINT pk_categorias PRIMARY KEY (id),
  CONSTRAINT uk_categorias_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS autores (
  id           BIGINT        NOT NULL AUTO_INCREMENT,
  nombres      VARCHAR(80)   NOT NULL,
  apellidos    VARCHAR(80)   NOT NULL,
  nacionalidad VARCHAR(60)   NULL,
  biografia    VARCHAR(1000) NULL,
  CONSTRAINT pk_autores PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS libros (
  id                  BIGINT       NOT NULL AUTO_INCREMENT,
  isbn                VARCHAR(20)  NOT NULL,
  titulo              VARCHAR(200) NOT NULL,
  editorial           VARCHAR(120) NULL,
  anio_publicacion    INT          NULL,
  categoria_id        BIGINT       NOT NULL,
  cantidad_total      INT          NOT NULL,
  cantidad_disponible INT          NOT NULL,
  portada_url         VARCHAR(500) NULL,
  activo              BOOLEAN      NOT NULL DEFAULT TRUE,     -- baja lógica (RN-11)
  fecha_registro      DATETIME(6)  NOT NULL,
  version             BIGINT       NOT NULL DEFAULT 0,        -- bloqueo optimista (@Version)
  CONSTRAINT pk_libros PRIMARY KEY (id),
  CONSTRAINT uk_libros_isbn UNIQUE (isbn),
  CONSTRAINT fk_libros_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id),
  CONSTRAINT ck_libros_cantidades CHECK (cantidad_disponible >= 0 AND cantidad_disponible <= cantidad_total)
) ENGINE = InnoDB;

-- Relación N:M libro ↔ autor (RF-05)
CREATE TABLE IF NOT EXISTS libro_autor (
  libro_id BIGINT NOT NULL,
  autor_id BIGINT NOT NULL,
  CONSTRAINT pk_libro_autor PRIMARY KEY (libro_id, autor_id),
  CONSTRAINT fk_libro_autor_libro FOREIGN KEY (libro_id) REFERENCES libros (id),
  CONSTRAINT fk_libro_autor_autor FOREIGN KEY (autor_id) REFERENCES autores (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Usuarios
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
  id             BIGINT       NOT NULL AUTO_INCREMENT,
  nombres        VARCHAR(80)  NOT NULL,
  apellidos      VARCHAR(80)  NOT NULL,
  documento      VARCHAR(15)  NOT NULL,
  correo         VARCHAR(120) NOT NULL,
  telefono       VARCHAR(15)  NULL,
  direccion      VARCHAR(200) NULL,
  username       VARCHAR(50)  NOT NULL,
  password       VARCHAR(100) NOT NULL,                       -- hash BCrypt, nunca texto plano
  rol            VARCHAR(20)  NOT NULL,
  estado         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
  fecha_registro DATETIME(6)  NOT NULL,
  CONSTRAINT pk_usuarios PRIMARY KEY (id),
  CONSTRAINT uk_usuarios_documento UNIQUE (documento),
  CONSTRAINT uk_usuarios_correo UNIQUE (correo),
  CONSTRAINT uk_usuarios_username UNIQUE (username),
  CONSTRAINT ck_usuarios_rol CHECK (rol IN ('BIBLIOTECARIO', 'LECTOR')),
  CONSTRAINT ck_usuarios_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO'))
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Circulación
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservas (
  id               BIGINT      NOT NULL AUTO_INCREMENT,
  usuario_id       BIGINT      NOT NULL,
  libro_id         BIGINT      NOT NULL,
  fecha_reserva    DATETIME(6) NOT NULL,                      -- define el orden de llegada (FIFO)
  fecha_disponible DATE        NULL,
  fecha_expiracion DATE        NULL,
  estado           VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
  CONSTRAINT pk_reservas PRIMARY KEY (id),
  CONSTRAINT fk_reservas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
  CONSTRAINT fk_reservas_libro FOREIGN KEY (libro_id) REFERENCES libros (id),
  CONSTRAINT ck_reservas_estado CHECK (estado IN ('PENDIENTE', 'DISPONIBLE', 'ATENDIDA', 'CANCELADA', 'EXPIRADA'))
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS prestamos (
  id                BIGINT      NOT NULL AUTO_INCREMENT,
  usuario_id        BIGINT      NOT NULL,
  libro_id          BIGINT      NOT NULL,
  reserva_id        BIGINT      NULL,                         -- reserva que originó el préstamo (opcional)
  fecha_prestamo    DATE        NOT NULL,
  fecha_vencimiento DATE        NOT NULL,
  estado            VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
  CONSTRAINT pk_prestamos PRIMARY KEY (id),
  CONSTRAINT uk_prestamos_reserva UNIQUE (reserva_id),
  CONSTRAINT fk_prestamos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
  CONSTRAINT fk_prestamos_libro FOREIGN KEY (libro_id) REFERENCES libros (id),
  CONSTRAINT fk_prestamos_reserva FOREIGN KEY (reserva_id) REFERENCES reservas (id),
  CONSTRAINT ck_prestamos_estado CHECK (estado IN ('ACTIVO', 'VENCIDO', 'DEVUELTO')),
  CONSTRAINT ck_prestamos_fechas CHECK (fecha_vencimiento > fecha_prestamo)
) ENGINE = InnoDB;

-- Cada préstamo tiene como máximo una devolución (1:1)
CREATE TABLE IF NOT EXISTS devoluciones (
  id               BIGINT       NOT NULL AUTO_INCREMENT,
  prestamo_id      BIGINT       NOT NULL,
  fecha_devolucion DATE         NOT NULL,
  dias_retraso     INT          NOT NULL DEFAULT 0,
  estado_libro     VARCHAR(20)  NOT NULL DEFAULT 'BUENO',
  observaciones    VARCHAR(500) NULL,
  CONSTRAINT pk_devoluciones PRIMARY KEY (id),
  CONSTRAINT uk_devoluciones_prestamo UNIQUE (prestamo_id),
  CONSTRAINT fk_devoluciones_prestamo FOREIGN KEY (prestamo_id) REFERENCES prestamos (id),
  CONSTRAINT ck_devoluciones_estado CHECK (estado_libro IN ('BUENO', 'DANADO')),
  CONSTRAINT ck_devoluciones_retraso CHECK (dias_retraso >= 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Índices para las búsquedas más frecuentes (consultas JPQL)
-- ---------------------------------------------------------------------
CREATE INDEX idx_libros_titulo        ON libros (titulo);
CREATE INDEX idx_prestamos_estado     ON prestamos (estado, fecha_vencimiento);
CREATE INDEX idx_reservas_libro_cola  ON reservas (libro_id, estado, fecha_reserva);
CREATE INDEX idx_reservas_usuario     ON reservas (usuario_id, estado);

-- Verificación
SHOW TABLES;
