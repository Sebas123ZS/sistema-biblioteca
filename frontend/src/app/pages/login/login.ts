import { Location } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../core/services/auth.service';
import { aplicarErroresServidor, mensajeError } from '../../core/services/error-util';
import { ToastService } from '../../core/services/toast.service';
import { Showcase } from './showcase/showcase';

type Modo = 'login' | 'registro';

/** Las dos contraseñas deben coincidir. */
function clavesIguales(grupo: AbstractControl): ValidationErrors | null {
  const a = grupo.get('password')?.value;
  const b = grupo.get('confirmar')?.value;
  return a && b && a !== b ? { noCoinciden: true } : null;
}

const CAMPOS_PASO_1 = ['nombres', 'apellidos', 'documento', 'telefono', 'direccion'];

/**
 * Pantalla de acceso: iniciar sesión (RF-13) y crear cuenta de lector (RF-19).
 */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, LucideAngularModule, Showcase],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginPage {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private location = inject(Location);
  private toast = inject(ToastService);

  protected modo = signal<Modo>((inject(ActivatedRoute).snapshot.data['modo'] as Modo) ?? 'login');
  protected verClave = signal(false);
  protected cargando = signal(false);
  protected error = signal<string | null>(null);
  protected exito = signal(false);
  protected paso = signal<1 | 2>(1);

  // Luz que sigue al mouse sobre la tarjeta
  protected luzX = signal(50);
  protected luzY = signal(0);

  // ----- Login -----
  protected form = this.fb.nonNullable.group({
    login: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });

  // ----- Registro -----
  protected reg = this.fb.nonNullable.group({
    nombres: ['', [Validators.required, Validators.maxLength(80)]],
    apellidos: ['', [Validators.required, Validators.maxLength(80)]],
    documento: ['', [Validators.required, Validators.pattern(/^[0-9]{8,12}$/)]],
    telefono: ['', [Validators.pattern(/^$|^[0-9 +]{6,15}$/)]],
    direccion: ['', [Validators.maxLength(200)]],
    correo: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    username: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9._-]{4,50}$/)]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(64)]],
    confirmar: ['', [Validators.required]],
  }, { validators: clavesIguales });

  private clave = toSignal(this.reg.controls.password.valueChanges, { initialValue: '' });

  /** Fuerza de la contraseña: 0 a 4. */
  protected fuerza = computed(() => {
    const c = this.clave() ?? '';
    let puntos = 0;
    if (c.length >= 6) puntos++;
    if (c.length >= 10) puntos++;
    if (/[A-Z]/.test(c) && /[a-z]/.test(c)) puntos++;
    if (/\d/.test(c) || /[^A-Za-z0-9]/.test(c)) puntos++;
    return puntos;
  });
  protected textoFuerza = computed(() => ['Muy débil', 'Débil', 'Aceptable', 'Buena', 'Excelente'][this.fuerza()]);

  cambiarModo(m: Modo) {
    if (this.modo() === m) return;
    this.modo.set(m);
    this.error.set(null);
    this.paso.set(1);
    this.location.replaceState(m === 'login' ? '/login' : '/registro');
  }

  moverLuz(ev: MouseEvent) {
    const r = (ev.currentTarget as HTMLElement).getBoundingClientRect();
    this.luzX.set(((ev.clientX - r.left) / r.width) * 100);
    this.luzY.set(((ev.clientY - r.top) / r.height) * 100);
  }

  invalido(campo: 'login' | 'password'): boolean {
    const c = this.form.controls[campo];
    return c.invalid && (c.touched || c.dirty);
  }

  invalidoReg(campo: string): boolean {
    const c = this.reg.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  errReg(campo: string, defecto: string): string {
    return this.reg.get(campo)?.errors?.['servidor'] ?? defecto;
  }

  usarDemo(login: string, password: string) {
    this.form.setValue({ login, password });
    this.error.set(null);
  }

  ingresar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.cargando.set(true);
    this.error.set(null);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: (res) => this.entrar(`¡Bienvenido(a), ${res.usuario.nombres}!`, 'Sesión iniciada correctamente'),
      error: (err) => {
        this.error.set(mensajeError(err));
        this.cargando.set(false);
      },
    });
  }

  siguientePaso() {
    const invalidos = CAMPOS_PASO_1.filter((c) => this.reg.get(c)?.invalid);
    if (invalidos.length) {
      invalidos.forEach((c) => this.reg.get(c)?.markAsTouched());
      return;
    }
    this.paso.set(2);
  }

  registrar() {
    if (this.reg.invalid) {
      this.reg.markAllAsTouched();
      return;
    }
    const v = this.reg.getRawValue();
    this.cargando.set(true);
    this.error.set(null);
    this.auth.registrar({
      nombres: v.nombres.trim(), apellidos: v.apellidos.trim(), documento: v.documento.trim(),
      correo: v.correo.trim(), telefono: v.telefono || null, direccion: v.direccion || null,
      username: v.username.trim(), password: v.password,
    }).subscribe({
      next: (res) => this.entrar(`¡Cuenta creada, ${res.usuario.nombres}!`, 'Ya puedes explorar el catálogo y reservar libros'),
      error: (err) => {
        this.cargando.set(false);
        aplicarErroresServidor(this.reg, err);
        if (CAMPOS_PASO_1.some((c) => this.reg.get(c)?.errors?.['servidor'])) this.paso.set(1);
        this.error.set(mensajeError(err));
      },
    });
  }

  /** Animación de éxito y luego entra al sistema. */
  private entrar(titulo: string, mensaje: string) {
    this.exito.set(true);
    this.toast.exito(titulo, mensaje);
    setTimeout(() => this.router.navigateByUrl(this.auth.rutaInicio()), 900);
  }
}
