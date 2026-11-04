import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Autor, AutorRequest, Categoria, CategoriaRequest, Dashboard, Devolucion, DevolucionRequest,
  Disponibilidad, EstadoReserva, FiltroEstadoPrestamo, EstadoUsuario, Libro, LibroRequest, Page, Prestamo,
  PrestamoRequest, Reserva, ReservaRequest, Rol, Usuario, UsuarioRequest, UsuarioResumen,
} from '../models/models';
import { toParams } from './http-params';

const API = environment.apiUrl;

export interface Paginacion {
  page?: number;
  size?: number;
  sort?: string;
}

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private http = inject(HttpClient);
  listar(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${API}/categorias`);
  }
  crear(dto: CategoriaRequest): Observable<Categoria> {
    return this.http.post<Categoria>(`${API}/categorias`, dto);
  }
  actualizar(id: number, dto: CategoriaRequest): Observable<Categoria> {
    return this.http.put<Categoria>(`${API}/categorias/${id}`, dto);
  }
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/categorias/${id}`);
  }
}

@Injectable({ providedIn: 'root' })
export class AutorService {
  private http = inject(HttpClient);
  buscar(f: { q?: string } & Paginacion = {}): Observable<Page<Autor>> {
    return this.http.get<Page<Autor>>(`${API}/autores`, { params: toParams(f) });
  }
  todos(): Observable<Page<Autor>> {
    return this.buscar({ size: 500, sort: 'apellidos' });
  }
  crear(dto: AutorRequest): Observable<Autor> {
    return this.http.post<Autor>(`${API}/autores`, dto);
  }
  actualizar(id: number, dto: AutorRequest): Observable<Autor> {
    return this.http.put<Autor>(`${API}/autores/${id}`, dto);
  }
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/autores/${id}`);
  }
}

@Injectable({ providedIn: 'root' })
export class LibroService {
  private http = inject(HttpClient);
  buscar(f: { q?: string; categoriaId?: number | null; autorId?: number | null; disponible?: boolean | null } & Paginacion = {}): Observable<Page<Libro>> {
    return this.http.get<Page<Libro>>(`${API}/libros`, { params: toParams(f) });
  }
  obtener(id: number): Observable<Libro> {
    return this.http.get<Libro>(`${API}/libros/${id}`);
  }
  disponibilidad(id: number): Observable<Disponibilidad> {
    return this.http.get<Disponibilidad>(`${API}/libros/${id}/disponibilidad`);
  }
  crear(dto: LibroRequest): Observable<Libro> {
    return this.http.post<Libro>(`${API}/libros`, dto);
  }
  actualizar(id: number, dto: LibroRequest): Observable<Libro> {
    return this.http.put<Libro>(`${API}/libros/${id}`, dto);
  }
  asignarAutores(id: number, autorIds: number[]): Observable<Libro> {
    return this.http.put<Libro>(`${API}/libros/${id}/autores`, { autorIds });
  }
  darDeBaja(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/libros/${id}`);
  }
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private http = inject(HttpClient);
  buscar(f: { q?: string; estado?: EstadoUsuario | ''; rol?: Rol | '' } & Paginacion = {}): Observable<Page<Usuario>> {
    return this.http.get<Page<Usuario>>(`${API}/usuarios`, { params: toParams(f) });
  }
  obtener(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${API}/usuarios/${id}`);
  }
  resumen(id: number): Observable<UsuarioResumen> {
    return this.http.get<UsuarioResumen>(`${API}/usuarios/${id}/resumen`);
  }
  historial(id: number, p: Paginacion = {}): Observable<Page<Prestamo>> {
    return this.http.get<Page<Prestamo>>(`${API}/usuarios/${id}/prestamos`, { params: toParams({ ...p }) });
  }
  crear(dto: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(`${API}/usuarios`, dto);
  }
  actualizar(id: number, dto: UsuarioRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`${API}/usuarios/${id}`, dto);
  }
  cambiarEstado(id: number, estado: EstadoUsuario): Observable<Usuario> {
    return this.http.patch<Usuario>(`${API}/usuarios/${id}/estado`, { estado });
  }
}

@Injectable({ providedIn: 'root' })
export class PrestamoService {
  private http = inject(HttpClient);
  buscar(f: { estado?: FiltroEstadoPrestamo; usuarioId?: number | null; q?: string } & Paginacion = {}): Observable<Page<Prestamo>> {
    return this.http.get<Page<Prestamo>>(`${API}/prestamos`, { params: toParams(f) });
  }
  obtener(id: number): Observable<Prestamo> {
    return this.http.get<Prestamo>(`${API}/prestamos/${id}`);
  }
  registrar(dto: PrestamoRequest): Observable<Prestamo> {
    return this.http.post<Prestamo>(`${API}/prestamos`, dto);
  }
}

@Injectable({ providedIn: 'root' })
export class DevolucionService {
  private http = inject(HttpClient);
  buscar(f: { q?: string } & Paginacion = {}): Observable<Page<Devolucion>> {
    return this.http.get<Page<Devolucion>>(`${API}/devoluciones`, { params: toParams(f) });
  }
  registrar(dto: DevolucionRequest): Observable<Devolucion> {
    return this.http.post<Devolucion>(`${API}/devoluciones`, dto);
  }
}

@Injectable({ providedIn: 'root' })
export class ReservaService {
  private http = inject(HttpClient);
  buscar(f: { estado?: EstadoReserva | ''; usuarioId?: number | null; libroId?: number | null; q?: string } & Paginacion = {}): Observable<Page<Reserva>> {
    return this.http.get<Page<Reserva>>(`${API}/reservas`, { params: toParams(f) });
  }
  registrar(dto: ReservaRequest): Observable<Reserva> {
    return this.http.post<Reserva>(`${API}/reservas`, dto);
  }
  cancelar(id: number): Observable<Reserva> {
    return this.http.patch<Reserva>(`${API}/reservas/${id}/cancelar`, {});
  }
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  resumen(): Observable<Dashboard> {
    return this.http.get<Dashboard>(`${API}/dashboard`);
  }
}
