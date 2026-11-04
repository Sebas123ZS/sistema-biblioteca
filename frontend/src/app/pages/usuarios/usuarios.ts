import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { EstadoUsuario, Page, Prestamo, Rol, Usuario, UsuarioResumen } from '../../core/models/models';
import { UsuarioService } from '../../core/services/api.services';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { aplicarErroresServidor, mensajeError } from '../../core/services/error-util';
import { ToastService } from '../../core/services/toast.service';
import { EmptyState } from '../../shared/empty-state/empty-state';
import { EstadoBadge } from '../../shared/estado-badge/estado-badge';
import { Modal } from '../../shared/modal/modal';
import { Pagination } from '../../shared/pagination/pagination';

/** RF-06 y RF-07: gestión de usuarios e historial de préstamos. */
@Component({
  selector: 'app-usuarios',
  imports: [LucideAngularModule, FormsModule, ReactiveFormsModule, DatePipe, Modal, Pagination, EmptyState, EstadoBadge],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.scss',
})
export class UsuariosPage implements OnInit {
  private usuarioService = inject(UsuarioService);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);
  private fb = inject(FormBuilder);
  protected auth = inject(AuthService);

  protected pagina = signal<Page<Usuario> | null>(null);
  protected filtros = { q: '', estado: '' as EstadoUsuario | '', rol: '' as Rol | '' };
  private paginaActual = 0;
  private temporizador?: ReturnType<typeof setTimeout>;

  protected seleccionado = signal<UsuarioResumen | null>(null);
  protected historial = signal<Prestamo[]>([]);

  protected formAbierto = signal(false);
  protected editando = signal<Usuario | null>(null);
  protected guardando = signal(false);
  protected form = this.fb.group({
    nombres: ['', [Validators.required, Validators.maxLength(80)]],
    apellidos: ['', [Validators.required, Validators.maxLength(80)]],
    documento: ['', [Validators.required, Validators.pattern(/^[0-9]{8,12}$/)]],
    correo: ['', [Validators.required, Validators.email]],
    telefono: ['', [Validators.pattern(/^$|^[0-9 +]{6,15}$/)]],
    direccion: ['', [Validators.maxLength(200)]],
    username: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9._-]{4,50}$/)]],
    password: [''],
    rol: ['LECTOR' as Rol, [Validators.required]],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(p = this.paginaActual) {
    this.paginaActual = p;
    this.usuarioService.buscar({ ...this.filtros, page: p, size: 10 }).subscribe({
      next: (res) => {
        this.pagina.set(res);
        if (!this.seleccionado() && res.content.length) this.seleccionar(res.content[0]);
      },
      error: (e) => this.toast.error('No se pudo cargar', mensajeError(e)),
    });
  }

  buscarConRetraso() {
    clearTimeout(this.temporizador);
    this.temporizador = setTimeout(() => this.cargar(0), 300);
  }

  iniciales(u: Usuario) {
    return (u.nombres[0] + u.apellidos[0]).toUpperCase();
  }

  seleccionar(u: Usuario) {
    this.usuarioService.resumen(u.id).subscribe((r) => this.seleccionado.set(r));
    this.usuarioService.historial(u.id, { size: 6 }).subscribe((h) => this.historial.set(h.content));
  }

  abrir(u?: Usuario) {
    this.editando.set(u ?? null);
    this.form.reset({
      nombres: u?.nombres ?? '', apellidos: u?.apellidos ?? '', documento: u?.documento ?? '',
      correo: u?.correo ?? '', telefono: u?.telefono ?? '', direccion: u?.direccion ?? '',
      username: u?.username ?? '', password: '', rol: u?.rol ?? 'LECTOR',
    });
    const pass = this.form.controls.password;
    pass.setValidators(u ? [Validators.minLength(6)] : [Validators.required, Validators.minLength(6)]);
    pass.updateValueAndValidity();
    this.formAbierto.set(true);
  }

  invalido(c: string) {
    const ctrl = this.form.get(c);
    return !!ctrl && ctrl.invalid && (ctrl.touched || ctrl.dirty);
  }

  err(c: string, defecto: string) {
    return this.form.get(c)?.errors?.['servidor'] ?? defecto;
  }

  guardar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const dto = {
      nombres: v.nombres!, apellidos: v.apellidos!, documento: v.documento!, correo: v.correo!,
      telefono: v.telefono || null, direccion: v.direccion || null, username: v.username!,
      password: v.password || null, rol: v.rol as Rol,
    };
    const e = this.editando();
    this.guardando.set(true);
    (e ? this.usuarioService.actualizar(e.id, dto) : this.usuarioService.crear(dto)).subscribe({
      next: (u) => {
        this.guardando.set(false);
        this.formAbierto.set(false);
        this.toast.exito(e ? 'Usuario actualizado' : 'Usuario registrado', u.nombreCompleto);
        this.cargar();
        this.seleccionar(u);
      },
      error: (err) => {
        this.guardando.set(false);
        aplicarErroresServidor(this.form, err);
        this.toast.error('No se pudo guardar', mensajeError(err));
      },
    });
  }

  async cambiarEstado(u: Usuario, estado: EstadoUsuario) {
    const textos: Record<EstadoUsuario, string> = {
      ACTIVO: 'activar', SUSPENDIDO: 'suspender', INACTIVO: 'desactivar',
    };
    const ok = await this.confirm.preguntar({
      titulo: `¿${textos[estado][0].toUpperCase() + textos[estado].slice(1)} usuario?`,
      mensaje: estado === 'ACTIVO'
        ? `${u.nombreCompleto} podrá volver a pedir préstamos y reservas.`
        : `${u.nombreCompleto} no podrá pedir préstamos ni reservas.`,
      textoConfirmar: textos[estado][0].toUpperCase() + textos[estado].slice(1),
      peligro: estado !== 'ACTIVO',
    });
    if (!ok) return;
    this.usuarioService.cambiarEstado(u.id, estado).subscribe({
      next: (act) => {
        this.toast.exito('Estado actualizado', `${act.nombreCompleto}: ${act.estado.toLowerCase()}`);
        this.cargar();
        this.seleccionar(act);
      },
      error: (e) => this.toast.error('No se pudo cambiar el estado', mensajeError(e)),
    });
  }
}
