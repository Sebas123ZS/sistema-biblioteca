import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Page, Prestamo, UsuarioResumen } from '../../core/models/models';
import { UsuarioService } from '../../core/services/api.services';
import { AuthService } from '../../core/services/auth.service';
import { BookCover } from '../../shared/book-cover/book-cover';
import { EmptyState } from '../../shared/empty-state/empty-state';
import { EstadoBadge } from '../../shared/estado-badge/estado-badge';
import { Pagination } from '../../shared/pagination/pagination';
import { StatCard } from '../../shared/stat-card/stat-card';

/** Perfil del lector con su historial de préstamos (RF-07). */
@Component({
  selector: 'app-mi-cuenta',
  imports: [LucideAngularModule, DatePipe, RouterLink, BookCover, EmptyState, EstadoBadge, Pagination, StatCard],
  templateUrl: './mi-cuenta.html',
  styleUrl: './mi-cuenta.scss',
})
export class MiCuentaPage implements OnInit {
  private usuarioService = inject(UsuarioService);
  protected auth = inject(AuthService);

  protected resumen = signal<UsuarioResumen | null>(null);
  protected historial = signal<Page<Prestamo> | null>(null);

  ngOnInit(): void {
    const id = this.auth.usuario()!.id;
    this.usuarioService.resumen(id).subscribe((r) => this.resumen.set(r));
    this.cargarHistorial(0);
  }

  cargarHistorial(p: number) {
    this.usuarioService.historial(this.auth.usuario()!.id, { page: p, size: 8 }).subscribe((h) => this.historial.set(h));
  }
}
