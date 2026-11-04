import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, RegistroRequest, Rol, Usuario } from '../models/models';

const TOKEN_KEY = 'biblioteca_token';
const USER_KEY = 'biblioteca_usuario';
const EXP_KEY = 'biblioteca_expira';

/** Maneja el inicio de sesión, el JWT y el usuario actual. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly _usuario = signal<Usuario | null>(this.leerUsuario());

  readonly usuario = this._usuario.asReadonly();
  readonly esBibliotecario = computed(() => this._usuario()?.rol === 'BIBLIOTECARIO');

  get token(): string | null {
    const expira = Number(localStorage.getItem(EXP_KEY) ?? 0);
    if (expira && Date.now() > expira) {
      this.limpiar();
      return null;
    }
    return localStorage.getItem(TOKEN_KEY);
  }

  /** Hay sesión si existe usuario y el token no expiró. */
  autenticado(): boolean {
    return this._usuario() !== null && !!this.token;
  }

  login(datos: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, datos)
      .pipe(tap((res) => this.guardarSesion(res)));
  }

  /** Crea una cuenta de lector y deja la sesión iniciada. */
  registrar(datos: RegistroRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/registro`, datos)
      .pipe(tap((res) => this.guardarSesion(res)));
  }

  private guardarSesion(res: LoginResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    localStorage.setItem(USER_KEY, JSON.stringify(res.usuario));
    localStorage.setItem(EXP_KEY, String(Date.now() + res.expiraEn));
    this._usuario.set(res.usuario);
  }

  logout(): void {
    this.limpiar();
    this.router.navigate(['/login']);
  }

  tieneRol(...roles: Rol[]): boolean {
    const rol = this._usuario()?.rol;
    return !!rol && roles.includes(rol);
  }

  /** Ruta de inicio según el rol. */
  rutaInicio(): string {
    return this.esBibliotecario() ? '/dashboard' : '/catalogo';
  }

  private limpiar(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(EXP_KEY);
    this._usuario?.set(null);
  }

  private leerUsuario(): Usuario | null {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? (JSON.parse(raw) as Usuario) : null;
    } catch {
      return null;
    }
  }
}
