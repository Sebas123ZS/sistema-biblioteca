import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { map } from 'rxjs';
import { Autor, AutorResumen, Categoria, Dashboard, Disponibilidad, Libro, Page } from '../../core/models/models';
import {
  AutorService, CategoriaService, DashboardService, LibroService, ReservaService,
} from '../../core/services/api.services';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { aplicarErroresServidor, mensajeError } from '../../core/services/error-util';
import { ToastService } from '../../core/services/toast.service';
import { BookCover } from '../../shared/book-cover/book-cover';
import { EmptyState } from '../../shared/empty-state/empty-state';
import { Modal } from '../../shared/modal/modal';
import { Pagination } from '../../shared/pagination/pagination';
import { SearchSelect } from '../../shared/search-select/search-select';
import { StatCard } from '../../shared/stat-card/stat-card';

type Vista = 'grid' | 'tabla';

/**
 * Bibliotecario: gestión completa del catálogo (RF-01, RF-02, RF-03, RF-05).
 * Lector: catálogo de consulta con disponibilidad y botón de reserva (RF-03, RF-11).
 */
@Component({
  selector: 'app-libros',
  imports: [
    LucideAngularModule, FormsModule, ReactiveFormsModule, DatePipe, BookCover, EmptyState, Modal,
    Pagination, SearchSelect, StatCard,
  ],
  templateUrl: './libros.html',
  styleUrl: './libros.scss',
})
export class LibrosPage implements OnInit {
  private libroService = inject(LibroService);
  private autorService = inject(AutorService);
  private categoriaService = inject(CategoriaService);
  private reservaService = inject(ReservaService);
  private dashboardService = inject(DashboardService);
  private toast = inject(ToastService);
  private confirm = inject(ConfirmService);
  private fb = inject(FormBuilder);
  protected auth = inject(AuthService);

  protected admin = this.auth.esBibliotecario();

  // ----- Listado -----
  protected pagina = signal<Page<Libro> | null>(null);
  protected cargando = signal(false);
  protected categorias = signal<Categoria[]>([]);
  protected resumen = signal<Dashboard | null>(null);
  protected vista = signal<Vista>(this.admin ? 'tabla' : 'grid');
  protected filtros = { q: '', categoriaId: null as number | null, disponible: '' as '' | 'true' | 'false' };
  private paginaActual = 0;
  private temporizador?: ReturnType<typeof setTimeout>;

  // ----- Detalle -----
  protected detalle = signal<Libro | null>(null);
  protected disponibilidad = signal<Disponibilidad | null>(null);

  // ----- Formulario -----
  protected formAbierto = signal(false);
  protected editando = signal<Libro | null>(null);
  protected guardando = signal(false);
  protected autoresSel = signal<AutorResumen[]>([]);
  protected form = this.fb.group({
    isbn: ['', [Validators.required, Validators.pattern(/^[0-9Xx-]{10,17}$/)]],
    titulo: ['', [Validators.required, Validators.maxLength(200)]],
    editorial: ['', [Validators.maxLength(120)]],
    anioPublicacion: [null as number | null, [Validators.min(1000), Validators.max(2100)]],
    categoriaId: [null as number | null, [Validators.required]],
    cantidadTotal: [1, [Validators.required, Validators.min(1), Validators.max(500)]],
    portadaUrl: ['', [Validators.maxLength(500)]],
  });
  protected autoresInvalidos = signal(false);

  protected buscarAutores = (q: string) =>
    this.autorService.buscar({ q, size: 8 }).pipe(map((p: Page<Autor>) => p.content));
  protected autorYaElegido = (a: Autor) => this.autoresSel().some((x) => x.id === a.id);

  protected autoresTexto = (l: Libro) => l.autores.map((a) => a.nombreCompleto).join(', ');
  protected porcentaje = (l: Libro) => (l.cantidadTotal ? (l.cantidadDisponible / l.cantidadTotal) * 100 : 0);
  protected agotados = computed(() => {
    const r = this.resumen();
    return r ? r.totalEjemplares - r.ejemplaresDisponibles : 0;
  });

  ngOnInit(): void {
    this.categoriaService.listar().subscribe((c) => this.categorias.set(c));
    this.cargar();
    this.cargarResumen();
  }

  cargarResumen() {
    if (this.admin) this.dashboardService.resumen().subscribe((r) => this.resumen.set(r));
  }

  cargar(pagina = this.paginaActual) {
    this.paginaActual = pagina;
    this.cargando.set(true);
    this.libroService.buscar({
      q: this.filtros.q,
      categoriaId: this.filtros.categoriaId,
      disponible: this.filtros.disponible === '' ? null : this.filtros.disponible === 'true',
      page: pagina,
      size: this.vista() === 'grid' ? 12 : 10,
    }).subscribe({
      next: (p) => {
        this.pagina.set(p);
        this.cargando.set(false);
      },
      error: (e) => {
        this.cargando.set(false);
        this.toast.error('No se pudo cargar el catálogo', mensajeError(e));
      },
    });
  }

  buscarConRetraso() {
    clearTimeout(this.temporizador);
    this.temporizador = setTimeout(() => this.cargar(0), 300);
  }

  limpiarFiltros() {
    this.filtros = { q: '', categoriaId: null, disponible: '' };
    this.cargar(0);
  }

  cambiarVista(v: Vista) {
    this.vista.set(v);
    this.cargar(0);
  }

  // ----- Detalle -----
  verDetalle(l: Libro) {
    this.detalle.set(l);
    this.disponibilidad.set(null);
    this.libroService.disponibilidad(l.id).subscribe((d) => this.disponibilidad.set(d));
  }

  // ----- Reserva (lector) -----
  async reservar(l: Libro) {
    const ok = await this.confirm.preguntar({
      titulo: 'Reservar libro',
      mensaje: `Se registrará una reserva de "${l.titulo}". Cuando se devuelva un ejemplar se le apartará por 3 días.`,
      textoConfirmar: 'Reservar',
    });
    if (!ok) return;
    this.reservaService.registrar({ libroId: l.id }).subscribe({
      next: () => {
        this.toast.exito('Reserva registrada', 'Le avisaremos cuando el libro esté disponible');
        this.detalle.set(null);
      },
      error: (e) => this.toast.error('No se pudo reservar', mensajeError(e)),
    });
  }

  // ----- Formulario -----
  abrirNuevo() {
    this.editando.set(null);
    this.form.reset({ cantidadTotal: 1, anioPublicacion: null, categoriaId: null, isbn: '', titulo: '', editorial: '', portadaUrl: '' });
    this.autoresSel.set([]);
    this.autoresInvalidos.set(false);
    this.formAbierto.set(true);
  }

  abrirEditar(l: Libro) {
    this.editando.set(l);
    this.form.reset({
      isbn: l.isbn, titulo: l.titulo, editorial: l.editorial ?? '', anioPublicacion: l.anioPublicacion ?? null,
      categoriaId: l.categoriaId, cantidadTotal: l.cantidadTotal, portadaUrl: l.portadaUrl ?? '',
    });
    this.autoresSel.set([...l.autores]);
    this.autoresInvalidos.set(false);
    this.detalle.set(null);
    this.formAbierto.set(true);
  }

  agregarAutor(a: Autor) {
    this.autoresSel.update((lista) => [...lista, { id: a.id, nombreCompleto: a.nombreCompleto }]);
    this.autoresInvalidos.set(false);
  }

  quitarAutor(id: number) {
    this.autoresSel.update((lista) => lista.filter((a) => a.id !== id));
  }

  invalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  guardar() {
    this.autoresInvalidos.set(this.autoresSel().length === 0);
    if (this.form.invalid || this.autoresInvalidos()) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const dto = {
      isbn: v.isbn!.trim(),
      titulo: v.titulo!.trim(),
      editorial: v.editorial || null,
      anioPublicacion: v.anioPublicacion || null,
      categoriaId: Number(v.categoriaId),
      autorIds: this.autoresSel().map((a) => a.id),
      cantidadTotal: Number(v.cantidadTotal),
      portadaUrl: v.portadaUrl || null,
    };
    this.guardando.set(true);
    const editando = this.editando();
    const peticion = editando ? this.libroService.actualizar(editando.id, dto) : this.libroService.crear(dto);
    peticion.subscribe({
      next: (l) => {
        this.guardando.set(false);
        this.formAbierto.set(false);
        this.toast.exito(editando ? 'Libro actualizado' : 'Libro registrado', `"${l.titulo}" se guardó correctamente`);
        this.cargar();
        this.cargarResumen();
      },
      error: (e) => {
        this.guardando.set(false);
        aplicarErroresServidor(this.form, e);
        this.toast.error('No se pudo guardar', mensajeError(e));
      },
    });
  }

  async darDeBaja(l: Libro) {
    const ok = await this.confirm.preguntar({
      titulo: 'Dar de baja el libro',
      mensaje: `"${l.titulo}" dejará de aparecer en el catálogo. Sus reservas en espera se cancelarán.`,
      textoConfirmar: 'Dar de baja',
      peligro: true,
    });
    if (!ok) return;
    this.libroService.darDeBaja(l.id).subscribe({
      next: () => {
        this.toast.exito('Libro dado de baja');
        this.detalle.set(null);
        this.cargar();
        this.cargarResumen();
      },
      error: (e) => this.toast.error('No se pudo dar de baja', mensajeError(e)),
    });
  }
}
