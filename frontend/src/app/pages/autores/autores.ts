import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Autor, Page } from '../../core/models/models';
import { AutorService } from '../../core/services/api.services';
import { ConfirmService } from '../../core/services/confirm.service';
import { aplicarErroresServidor, mensajeError } from '../../core/services/error-util';
import { ToastService } from '../../core/services/toast.service';
import { EmptyState } from '../../shared/empty-state/empty-state';
import { Modal } from '../../shared/modal/modal';
import { Pagination } from '../../shared/pagination/pagination';

/** RF-04: registrar, actualizar, eliminar y consultar autores. */
@Component({
  selector: 'app-autores',
  imports: [LucideAngularModule, FormsModule, ReactiveFormsModule, Modal, Pagination, EmptyState],
  templateUrl: './autores.html',
  styleUrl: './autores.scss',
})
export class AutoresPage implements OnInit {
  private autorService = inject(AutorService);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);
  private fb = inject(FormBuilder);

  protected pagina = signal<Page<Autor> | null>(null);
  protected q = '';
  private paginaActual = 0;
  private temporizador?: ReturnType<typeof setTimeout>;

  protected formAbierto = signal(false);
  protected editando = signal<Autor | null>(null);
  protected guardando = signal(false);
  protected form = this.fb.group({
    nombres: ['', [Validators.required, Validators.maxLength(80)]],
    apellidos: ['', [Validators.required, Validators.maxLength(80)]],
    nacionalidad: ['', [Validators.maxLength(60)]],
    biografia: ['', [Validators.maxLength(1000)]],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(p = this.paginaActual) {
    this.paginaActual = p;
    this.autorService.buscar({ q: this.q, page: p, size: 10 }).subscribe({
      next: (res) => this.pagina.set(res),
      error: (e) => this.toast.error('No se pudo cargar', mensajeError(e)),
    });
  }

  buscarConRetraso() {
    clearTimeout(this.temporizador);
    this.temporizador = setTimeout(() => this.cargar(0), 300);
  }

  iniciales(a: Autor) {
    return (a.nombres[0] + a.apellidos[0]).toUpperCase();
  }

  abrir(a?: Autor) {
    this.editando.set(a ?? null);
    this.form.reset({
      nombres: a?.nombres ?? '', apellidos: a?.apellidos ?? '',
      nacionalidad: a?.nacionalidad ?? '', biografia: a?.biografia ?? '',
    });
    this.formAbierto.set(true);
  }

  invalido(c: string) {
    const ctrl = this.form.get(c);
    return !!ctrl && ctrl.invalid && (ctrl.touched || ctrl.dirty);
  }

  guardar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const dto = { nombres: v.nombres!, apellidos: v.apellidos!, nacionalidad: v.nacionalidad || null, biografia: v.biografia || null };
    const e = this.editando();
    this.guardando.set(true);
    (e ? this.autorService.actualizar(e.id, dto) : this.autorService.crear(dto)).subscribe({
      next: (a) => {
        this.guardando.set(false);
        this.formAbierto.set(false);
        this.toast.exito(e ? 'Autor actualizado' : 'Autor registrado', a.nombreCompleto);
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        aplicarErroresServidor(this.form, err);
        this.toast.error('No se pudo guardar', mensajeError(err));
      },
    });
  }

  async eliminar(a: Autor) {
    const ok = await this.confirm.preguntar({
      titulo: 'Eliminar autor',
      mensaje: `¿Eliminar a ${a.nombreCompleto}? Solo es posible si no tiene libros activos.`,
      textoConfirmar: 'Eliminar',
      peligro: true,
    });
    if (!ok) return;
    this.autorService.eliminar(a.id).subscribe({
      next: () => {
        this.toast.exito('Autor eliminado');
        this.cargar();
      },
      error: (e) => this.toast.error('No se pudo eliminar', mensajeError(e)),
    });
  }
}
