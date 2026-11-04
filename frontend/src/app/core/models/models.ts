// ===== Tipos compartidos con la API REST (mismos nombres que los DTO de Spring Boot) =====

export type Rol = 'BIBLIOTECARIO' | 'LECTOR';
export type EstadoUsuario = 'ACTIVO' | 'INACTIVO' | 'SUSPENDIDO';
export type EstadoPrestamo = 'ACTIVO' | 'VENCIDO' | 'DEVUELTO';
/** Filtro de préstamos: un estado o varios separados por coma (ej. 'ACTIVO,VENCIDO'). */
export type FiltroEstadoPrestamo = EstadoPrestamo | 'ACTIVO,VENCIDO' | '';
export type EstadoReserva = 'PENDIENTE' | 'DISPONIBLE' | 'ATENDIDA' | 'CANCELADA' | 'EXPIRADA';
export type EstadoLibroDevuelto = 'BUENO' | 'DANADO';

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  mensaje: string;
  path: string;
  errores?: Record<string, string>;
}

// ----- Auth -----
export interface LoginRequest {
  login: string;
  password: string;
}

/** Autorregistro de lectores (POST /api/auth/registro). */
export interface RegistroRequest {
  nombres: string;
  apellidos: string;
  documento: string;
  correo: string;
  telefono?: string | null;
  direccion?: string | null;
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  expiraEn: number;
  usuario: Usuario;
}

// ----- Catálogo -----
export interface Categoria {
  id: number;
  nombre: string;
  descripcion?: string | null;
  /** Títulos activos de la categoría (lo calcula el backend). */
  cantidadLibros?: number;
}

export interface CategoriaRequest {
  nombre: string;
  descripcion?: string | null;
}

export interface Autor {
  id: number;
  codigo: string;
  nombres: string;
  apellidos: string;
  nombreCompleto: string;
  nacionalidad?: string | null;
  biografia?: string | null;
  cantidadLibros: number;
}

export interface AutorRequest {
  nombres: string;
  apellidos: string;
  nacionalidad?: string | null;
  biografia?: string | null;
}

export interface AutorResumen {
  id: number;
  nombreCompleto: string;
}

export interface Libro {
  id: number;
  codigo: string;
  isbn: string;
  titulo: string;
  editorial?: string | null;
  anioPublicacion?: number | null;
  categoriaId: number;
  categoriaNombre: string;
  autores: AutorResumen[];
  cantidadTotal: number;
  cantidadDisponible: number;
  disponible: boolean;
  portadaUrl?: string | null;
  fechaRegistro: string;
}

export interface LibroRequest {
  isbn: string;
  titulo: string;
  editorial?: string | null;
  anioPublicacion?: number | null;
  categoriaId: number;
  autorIds: number[];
  cantidadTotal: number;
  portadaUrl?: string | null;
}

export interface Disponibilidad {
  libroId: number;
  titulo: string;
  cantidadTotal: number;
  cantidadDisponible: number;
  cantidadPrestada: number;
  disponible: boolean;
  reservasEnEspera: number;
}

// ----- Usuarios -----
export interface Usuario {
  id: number;
  codigo: string;
  nombres: string;
  apellidos: string;
  nombreCompleto: string;
  documento: string;
  correo: string;
  telefono?: string | null;
  direccion?: string | null;
  username: string;
  rol: Rol;
  estado: EstadoUsuario;
  fechaRegistro: string;
}

export interface UsuarioRequest {
  nombres: string;
  apellidos: string;
  documento: string;
  correo: string;
  telefono?: string | null;
  direccion?: string | null;
  username: string;
  password?: string | null;
  rol: Rol;
}

export interface UsuarioResumen {
  usuario: Usuario;
  prestamosActivos: number;
  prestamosVencidos: number;
  prestamosDevueltos: number;
  reservasActivas: number;
}

// ----- Circulación -----
export interface Prestamo {
  id: number;
  codigo: string;
  usuarioId: number;
  usuarioNombre: string;
  usuarioDocumento: string;
  libroId: number;
  libroTitulo: string;
  libroIsbn: string;
  libroPortadaUrl?: string | null;
  fechaPrestamo: string;
  fechaVencimiento: string;
  estado: EstadoPrestamo;
  diasRestantes: number;
  fechaDevolucion?: string | null;
}

export interface PrestamoRequest {
  usuarioId: number;
  libroId: number;
  fechaVencimiento?: string | null;
}

export interface Devolucion {
  id: number;
  codigo: string;
  prestamoId: number;
  prestamoCodigo: string;
  usuarioId: number;
  usuarioNombre: string;
  libroId: number;
  libroTitulo: string;
  fechaPrestamo: string;
  fechaVencimiento: string;
  fechaDevolucion: string;
  diasRetraso: number;
  estadoLibro: EstadoLibroDevuelto;
  observaciones?: string | null;
  reservaAsignadaA?: string | null;
}

export interface DevolucionRequest {
  prestamoId: number;
  estadoLibro: EstadoLibroDevuelto;
  observaciones?: string | null;
}

export interface Reserva {
  id: number;
  codigo: string;
  usuarioId: number;
  usuarioNombre: string;
  usuarioCorreo: string;
  libroId: number;
  libroTitulo: string;
  libroPortadaUrl?: string | null;
  fechaReserva: string;
  fechaDisponible?: string | null;
  fechaExpiracion?: string | null;
  estado: EstadoReserva;
}

export interface ReservaRequest {
  libroId: number;
  usuarioId?: number | null;
}

// ----- Dashboard -----
export interface Dashboard {
  totalTitulos: number;
  totalEjemplares: number;
  ejemplaresDisponibles: number;
  usuariosRegistrados: number;
  usuariosNuevosMes: number;
  prestamosActivos: number;
  prestamosVencidos: number;
  devolucionesHoy: number;
  devolucionesMes: number;
  reservasPendientes: number;
  reservasDisponibles: number;
  categorias: { nombre: string; total: number }[];
  ultimosLibros: Libro[];
}
