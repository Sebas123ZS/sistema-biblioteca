import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Categoria } from '../../core/models/models';
import { CategoriaService } from '../../core/services/api.services';
import { ConfirmService } from '../../core/services/confirm.service';
import { mensajeError } from '../../core/services/error-util';
import { ToastService } from '../../core/services/toast.service';
import { EmptyState } from '../../shared/empty-state/empty-state';
import { Modal } from '../../shared/modal/modal';

/** RF-17: gestión de categorías del catálogo. */
@Component({
  selector: 'app-categorias',
  imports: [LucideAngularModule, ReactiveFormsModule, Modal, EmptyState],
  templateUrl: './categorias.html',
  styleUrl: './categorias.scss',
})
export class CategoriasPage implements OnInit {
  private categoriaService = inject(CategoriaService);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);
  private fb = inject(FormBuilder);

  protected categorias = signal<Categoria[]>([]);
  protected formAbierto = signal(false);
  protected editando = signal<Categoria | null>(null);
  protected form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(60)]],
    descripcion: ['', [Validators.maxLength(255)]],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar() {
    // El backend devuelve cada categoría con su cantidad de títulos activos (cantidadLibros).
    this.categoriaService.listar().subscribe({
      next: (c) => this.categorias.set(c),
      error: (e) => this.toast.error('No se pudo cargar', mensajeError(e)),
    });
  }

  abrir(c?: Categoria) {
    this.editando.set(c ?? null);
    this.form.reset({ nombre: c?.nombre ?? '', descripcion: c?.descripcion ?? '' });
    this.formAbierto.set(true);
  }

  guardar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const dto = { nombre: v.nombre!, descripcion: v.descripcion || null };
    const e = this.editando();
    (e ? this.categoriaService.actualizar(e.id, dto) : this.categoriaService.crear(dto)).subscribe({
      next: () => {
        this.formAbierto.set(false);
        this.toast.exito(e ? 'Categoría actualizada' : 'Categoría registrada');
        this.cargar();
      },
      error: (err) => this.toast.error('No se pudo guardar', mensajeError(err)),
    });
  }

  async eliminar(c: Categoria) {
    const ok = await this.confirm.preguntar({
      titulo: 'Eliminar categoría', mensaje: `¿Eliminar "${c.nombre}"?`, textoConfirmar: 'Eliminar', peligro: true,
    });
    if (!ok) return;
    this.categoriaService.eliminar(c.id).subscribe({
      next: () => {
        this.toast.exito('Categoría eliminada');
        this.cargar();
      },
      error: (err) => this.toast.error('No se pudo eliminar', mensajeError(err)),
    });
  }
}
