import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { forkJoin, map } from 'rxjs';
import { EstadoPrestamo, Libro, Page, Prestamo, Usuario } from '../../core/models/models';
import { LibroService, PrestamoService, UsuarioService } from '../../core/services/api.services';
import { AuthService } from '../../core/services/auth.service';
import { mensajeError } from '../../core/services/error-util';
import { ToastService } from '../../core/services/toast.service';
import { BookCover } from '../../shared/book-cover/book-cover';
import { EmptyState } from '../../shared/empty-state/empty-state';
import { EstadoBadge } from '../../shared/estado-badge/estado-badge';
import { Pagination } from '../../shared/pagination/pagination';
import { SearchSelect } from '../../shared/search-select/search-select';

const DIAS_PRESTAMO = 14;

function isoLocal(d: Date): string {
  const z = new Date(d.getTime() - d.getTimezoneOffset() * 60000);
  return z.toISOString().slice(0, 10);
}

/** RF-08 (registrar préstamo) y RF-09 (consultar activos/vencidos). */
@Component({
  selector: 'app-prestamos',
  imports: [LucideAngularModule, FormsModule, DatePipe, RouterLink, BookCover, EmptyState, EstadoBadge, Pagination, SearchSelect],
  templateUrl: './prestamos.html',
  styleUrl: './prestamos.scss',
})
export class PrestamosPage implements OnInit {
  private prestamoService = inject(PrestamoService);
  private usuarioService = inject(UsuarioService);
  private libroService = inject(LibroService);
  private toast = inject(ToastService);
  protected auth = inject(AuthService);
  protected admin = this.auth.esBibliotecario();

  protected pagina = signal<Page<Prestamo> | null>(null);
  protected estado = signal<EstadoPrestamo | ''>('');
  protected conteos = signal<Partial<Record<string, number>>>({});
  protected q = '';
  private paginaActual = 0;
  private temporizador?: ReturnType<typeof setTimeout>;

  // ----- Registro -----
  protected usuarioSel = signal<Usuario | null>(null);
  protected libroSel = signal<Libro | null>(null);
  protected hoy = isoLocal(new Date());
  protected fechaVencimiento = isoLocal(new Date(Date.now() + DIAS_PRESTAMO * 86400000));
  protected maxFecha = isoLocal(new Date(Date.now() + 60 * 86400000));
  protected registrando = signal(false);
  protected intento = signal(false);

  protected buscarUsuarios = (q: string) =>
    this.usuarioService.buscar({ q, size: 8 }).pipe(map((p) => p.content));
  protected buscarLibros = (q: string) =>
    this.libroService.buscar({ q, size: 8 }).pipe(map((p) => p.content));
  protected usuarioNoHabilitado = (u: Usuario) => u.estado !== 'ACTIVO';

  ngOnInit(): void {
    this.cargar();
    this.cargarConteos();
  }

  cargar(p = this.paginaActual) {
    this.paginaActual = p;
    this.prestamoService.buscar({ estado: this.estado(), q: this.q, page: p, size: 10 }).subscribe({
      next: (res) => this.pagina.set(res),
      error: (e) => this.toast.error('No se pudo cargar', mensajeError(e)),
    });
  }

  cargarConteos() {
    const contar = (estado: EstadoPrestamo | '') =>
      this.prestamoService.buscar({ estado, size: 1 }).pipe(map((p) => p.totalElements));
    forkJoin({ TODOS: contar(''), ACTIVO: contar('ACTIVO'), VENCIDO: contar('VENCIDO'), DEVUELTO: contar('DEVUELTO') })
      .subscribe((c) => this.conteos.set(c));
  }

  filtrar(e: EstadoPrestamo | '') {
    this.estado.set(e);
    this.cargar(0);
  }

  buscarConRetraso() {
    clearTimeout(this.temporizador);
    this.temporizador = setTimeout(() => this.cargar(0), 300);
  }

  textoDias(p: Prestamo): string {
    if (p.estado === 'DEVUELTO') return 'Devuelto';
    if (p.diasRestantes < 0) return `${Math.abs(p.diasRestantes)} días de atraso`;
    if (p.diasRestantes === 0) return 'Vence hoy';
    return `${p.diasRestantes} días restantes`;
  }

  registrar() {
    this.intento.set(true);
    const u = this.usuarioSel();
    const l = this.libroSel();
    if (!u || !l || !this.fechaVencimiento) return;
    this.registrando.set(true);
    this.prestamoService.registrar({ usuarioId: u.id, libroId: l.id, fechaVencimiento: this.fechaVencimiento }).subscribe({
      next: (p) => {
        this.registrando.set(false);
        this.toast.exito('Préstamo registrado correctamente', `${p.codigo}: "${p.libroTitulo}" para ${p.usuarioNombre}`);
        this.usuarioSel.set(null);
        this.libroSel.set(null);
        this.intento.set(false);
        this.cargar(0);
        this.cargarConteos();
      },
      error: (e) => {
        this.registrando.set(false);
        this.toast.error('No se pudo registrar el préstamo', mensajeError(e));
      },
    });
  }
}
