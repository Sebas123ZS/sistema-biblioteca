import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Devolucion, EstadoLibroDevuelto, Page, Prestamo } from '../../core/models/models';
import { DashboardService, DevolucionService, PrestamoService } from '../../core/services/api.services';
import { ConfirmService } from '../../core/services/confirm.service';
import { mensajeError } from '../../core/services/error-util';
import { ToastService } from '../../core/services/toast.service';
import { BookCover } from '../../shared/book-cover/book-cover';
import { EmptyState } from '../../shared/empty-state/empty-state';
import { EstadoBadge } from '../../shared/estado-badge/estado-badge';
import { Pagination } from '../../shared/pagination/pagination';
import { StatCard } from '../../shared/stat-card/stat-card';

/** RF-10 (registrar devolución) y RF-18 (historial de devoluciones). */
@Component({
  selector: 'app-devoluciones',
  imports: [LucideAngularModule, FormsModule, DatePipe, BookCover, EmptyState, EstadoBadge, Pagination, StatCard],
  templateUrl: './devoluciones.html',
  styleUrl: './devoluciones.scss',
})
export class DevolucionesPage implements OnInit {
  private devolucionService = inject(DevolucionService);
  private prestamoService = inject(PrestamoService);
  private dashboardService = inject(DashboardService);
  private confirm = inject(ConfirmService);
  private toast = inject(ToastService);

  protected q = '';
  protected buscando = signal(false);
  protected pendientes = signal<Prestamo[]>([]);
  protected totalActivos = signal(0);
  protected totalVencidos = signal(0);
  protected seleccionado = signal<Prestamo | null>(null);
  protected estadoLibro: EstadoLibroDevuelto = 'BUENO';
  protected observaciones = '';
  protected registrando = signal(false);

  protected historial = signal<Page<Devolucion> | null>(null);
  protected hoyCount = signal(0);

  protected atraso = computed(() => {
    const p = this.seleccionado();
    return p && p.diasRestantes < 0 ? Math.abs(p.diasRestantes) : 0;
  });

  ngOnInit(): void {
    this.cargarIndicadores();
    this.buscarPrestamos();
    this.cargarHistorial(0);
  }

  cargarIndicadores() {
    this.dashboardService.resumen().subscribe((d) => {
      this.hoyCount.set(d.devolucionesHoy);
      this.totalActivos.set(d.prestamosActivos);
      this.totalVencidos.set(d.prestamosVencidos);
    });
  }

  /**
   * Préstamos sin devolver (activos y vencidos) que coinciden con la búsqueda.
   * El filtro se aplica en el backend (estado=ACTIVO,VENCIDO), así los préstamos
   * ya devueltos nunca ocupan los resultados aunque el historial crezca.
   */
  buscarPrestamos() {
    this.buscando.set(true);
    this.prestamoService.buscar({ estado: 'ACTIVO,VENCIDO', q: this.q, size: 30, sort: 'fechaVencimiento,asc' }).subscribe({
      next: (p) => {
        this.pendientes.set(p.content);
        this.buscando.set(false);
      },
      error: (e) => {
        this.buscando.set(false);
        this.toast.error('No se pudo buscar', mensajeError(e));
      },
    });
  }

  cargarHistorial(p: number) {
    this.devolucionService.buscar({ page: p, size: 8 }).subscribe((h) => this.historial.set(h));
  }

  elegir(p: Prestamo) {
    this.seleccionado.set(p);
    this.estadoLibro = 'BUENO';
    this.observaciones = '';
  }

  async registrar() {
    const p = this.seleccionado();
    if (!p) return;
    const ok = await this.confirm.preguntar({
      titulo: '¿Confirmar devolución de este libro?',
      mensaje: `Se registrará la devolución de "${p.libroTitulo}" por parte de ${p.usuarioNombre}.` +
        (this.estadoLibro === 'DANADO' ? ' El ejemplar dañado se retirará del inventario.' : ' El ejemplar volverá a estar disponible (o se apartará para la siguiente reserva).'),
      textoConfirmar: 'Confirmar',
    });
    if (!ok) return;
    this.registrando.set(true);
    this.devolucionService.registrar({ prestamoId: p.id, estadoLibro: this.estadoLibro, observaciones: this.observaciones || null }).subscribe({
      next: (d) => {
        this.registrando.set(false);
        this.toast.exito('Devolución registrada correctamente',
          d.diasRetraso > 0 ? `Se registraron ${d.diasRetraso} días de retraso` : 'Entregado a tiempo');
        if (d.reservaAsignadaA) {
          this.toast.info('Libro apartado por reserva', `El ejemplar quedó reservado para ${d.reservaAsignadaA}`);
        }
        this.seleccionado.set(null);
        this.cargarIndicadores();
        this.buscarPrestamos();
        this.cargarHistorial(0);
      },
      error: (e) => {
        this.registrando.set(false);
        this.toast.error('No se pudo registrar', mensajeError(e));
      },
    });
  }
}
