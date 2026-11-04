import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { forkJoin, map } from 'rxjs';
import { EstadoReserva, Libro, Page, Reserva, Usuario } from '../../core/models/models';
import { LibroService, ReservaService, UsuarioService } from '../../core/services/api.services';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { mensajeError } from '../../core/services/error-util';
import { ToastService } from '../../core/services/toast.service';
import { BookCover } from '../../shared/book-cover/book-cover';
import { EmptyState } from '../../shared/empty-state/empty-state';
import { EstadoBadge } from '../../shared/estado-badge/estado-badge';
import { Pagination } from '../../shared/pagination/pagination';
import { SearchSelect } from '../../shared/search-select/search-select';
import { StatCard } from '../../shared/stat-card/stat-card';

/** RF-11 (registrar reserva), RF-12 (estado automático) y RF-15 (cancelar). */
@Component({
  selector: 'app-reservas',
  imports: [LucideAngularModule, DatePipe, RouterLink, BookCover, EmptyState, EstadoBadge, Pagination, SearchSelect, StatCard],
  templateUrl: './reservas.html',
  styleUrl: './reservas.scss',
})
export class ReservasPage implements OnInit {
  private reservaService = inject(ReservaService);
  private usuarioService = inject(UsuarioService);
  private libroService = inject(LibroService);
  private confirm = inject(ConfirmService);
  private toast = inject(ToastService);
  protected auth = inject(AuthService);
  protected admin = this.auth.esBibliotecario();

  protected pagina = signal<Page<Reserva> | null>(null);
  protected estado = signal<EstadoReserva | ''>('');
  protected conteos = signal<Partial<Record<string, number>>>({});
  protected disponibles = signal<Reserva[]>([]);
  private paginaActual = 0;

  protected usuarioSel = signal<Usuario | null>(null);
  protected libroSel = signal<Libro | null>(null);
  protected intento = signal(false);
  protected registrando = signal(false);

  protected buscarUsuarios = (q: string) => this.usuarioService.buscar({ q, size: 8 }).pipe(map((p) => p.content));
  protected buscarLibros = (q: string) => this.libroService.buscar({ q, size: 10 }).pipe(map((p) => p.content));
  protected libroConStock = (l: Libro) => l.disponible;
  protected usuarioNoHabilitado = (u: Usuario) => u.estado !== 'ACTIVO';

  protected filtros: { valor: EstadoReserva | ''; texto: string }[] = [
    { valor: '', texto: 'Todas' }, { valor: 'PENDIENTE', texto: 'Pendientes' }, { valor: 'DISPONIBLE', texto: 'Disponibles' },
    { valor: 'ATENDIDA', texto: 'Atendidas' }, { valor: 'CANCELADA', texto: 'Canceladas' }, { valor: 'EXPIRADA', texto: 'Expiradas' },
  ];
  protected puedeRegistrar = computed(() => !!this.libroSel() && (!this.admin || !!this.usuarioSel()));

  ngOnInit(): void {
    this.cargar();
    this.cargarConteos();
  }

  cargar(p = this.paginaActual) {
    this.paginaActual = p;
    this.reservaService.buscar({ estado: this.estado(), page: p, size: 10 }).subscribe({
      next: (r) => this.pagina.set(r),
      error: (e) => this.toast.error('No se pudo cargar', mensajeError(e)),
    });
    this.reservaService.buscar({ estado: 'DISPONIBLE', size: 3 }).subscribe((r) => this.disponibles.set(r.content));
  }

  cargarConteos() {
    const c = (e: EstadoReserva | '') => this.reservaService.buscar({ estado: e, size: 1 }).pipe(map((p) => p.totalElements));
    forkJoin({ '': c(''), PENDIENTE: c('PENDIENTE'), DISPONIBLE: c('DISPONIBLE'), ATENDIDA: c('ATENDIDA'), CANCELADA: c('CANCELADA'), EXPIRADA: c('EXPIRADA') })
      .subscribe((r) => this.conteos.set(r));
  }

  filtrar(e: EstadoReserva | '') {
    this.estado.set(e);
    this.cargar(0);
  }

  registrar() {
    this.intento.set(true);
    if (!this.puedeRegistrar()) return;
    this.registrando.set(true);
    this.reservaService.registrar({ libroId: this.libroSel()!.id, usuarioId: this.admin ? this.usuarioSel()!.id : null }).subscribe({
      next: (r) => {
        this.registrando.set(false);
        this.toast.exito('Reserva registrada', `${r.codigo}: "${r.libroTitulo}" quedó en lista de espera`);
        this.libroSel.set(null);
        this.usuarioSel.set(null);
        this.intento.set(false);
        this.cargar(0);
        this.cargarConteos();
      },
      error: (e) => {
        this.registrando.set(false);
        this.toast.error('No se pudo registrar la reserva', mensajeError(e));
      },
    });
  }

  async cancelar(r: Reserva) {
    const ok = await this.confirm.preguntar({
      titulo: 'Cancelar reserva',
      mensaje: `¿Cancelar la reserva de "${r.libroTitulo}"` + (this.admin ? ` de ${r.usuarioNombre}?` : '?'),
      textoConfirmar: 'Cancelar reserva',
      peligro: true,
    });
    if (!ok) return;
    this.reservaService.cancelar(r.id).subscribe({
      next: () => {
        this.toast.exito('Reserva cancelada');
        this.cargar();
        this.cargarConteos();
      },
      error: (e) => this.toast.error('No se pudo cancelar', mensajeError(e)),
    });
  }
}
