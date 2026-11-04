import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Dashboard, Prestamo, Reserva } from '../../core/models/models';
import { DashboardService, PrestamoService, ReservaService } from '../../core/services/api.services';
import { AuthService } from '../../core/services/auth.service';
import { mensajeError } from '../../core/services/error-util';
import { ToastService } from '../../core/services/toast.service';
import { BookCover } from '../../shared/book-cover/book-cover';
import { EmptyState } from '../../shared/empty-state/empty-state';
import { StatCard } from '../../shared/stat-card/stat-card';

@Component({
  selector: 'app-dashboard',
  imports: [StatCard, LucideAngularModule, RouterLink, DatePipe, BookCover, EmptyState],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class DashboardPage implements OnInit {
  private dashboardService = inject(DashboardService);
  private prestamoService = inject(PrestamoService);
  private reservaService = inject(ReservaService);
  private toast = inject(ToastService);
  protected auth = inject(AuthService);

  protected datos = signal<Dashboard | null>(null);
  protected vencidos = signal<Prestamo[]>([]);
  protected reservas = signal<Reserva[]>([]);
  protected cargando = signal(true);

  /** Porcentaje de ejemplares disponibles para el gráfico circular. */
  protected porcentaje = computed(() => {
    const d = this.datos();
    if (!d || d.totalEjemplares === 0) return 0;
    return Math.round((d.ejemplaresDisponibles / d.totalEjemplares) * 100);
  });
  protected maxCategoria = computed(() => Math.max(1, ...(this.datos()?.categorias.map((c) => c.total) ?? [1])));

  protected saludo = computed(() => {
    const h = new Date().getHours();
    return h < 12 ? 'Buenos días' : h < 19 ? 'Buenas tardes' : 'Buenas noches';
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar() {
    this.cargando.set(true);
    this.dashboardService.resumen().subscribe({
      next: (d) => {
        this.datos.set(d);
        this.cargando.set(false);
      },
      error: (e) => {
        this.cargando.set(false);
        this.toast.error('No se pudo cargar el panel', mensajeError(e));
      },
    });
    this.prestamoService.buscar({ estado: 'VENCIDO', size: 5, sort: 'fechaVencimiento,asc' })
      .subscribe((p) => this.vencidos.set(p.content));
    this.reservaService.buscar({ estado: 'PENDIENTE', size: 5, sort: 'fechaReserva,asc' })
      .subscribe((p) => this.reservas.set(p.content));
  }

  diasAtraso(p: Prestamo): number {
    return Math.abs(p.diasRestantes);
  }
}
