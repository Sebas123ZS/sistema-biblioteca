import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { filter } from 'rxjs';
import { Reserva, Rol } from '../../core/models/models';
import { ReservaService } from '../../core/services/api.services';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';

interface ItemMenu {
  ruta: string;
  texto: string;
  icono: string;
  roles: Rol[];
}

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LucideAngularModule, DatePipe],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell implements OnInit {
  protected auth = inject(AuthService);
  private router = inject(Router);
  private confirm = inject(ConfirmService);
  private reservaService = inject(ReservaService);

  protected hoy = new Date();
  protected menuAbierto = signal(false);
  protected notificacionesAbiertas = signal(false);
  protected avisos = signal<Reserva[]>([]);

  private readonly menu: ItemMenu[] = [
    { ruta: '/dashboard', texto: 'Dashboard', icono: 'layout-dashboard', roles: ['BIBLIOTECARIO'] },
    { ruta: '/libros', texto: 'Libros', icono: 'book-open', roles: ['BIBLIOTECARIO'] },
    { ruta: '/catalogo', texto: 'Catálogo', icono: 'library-big', roles: ['LECTOR'] },
    { ruta: '/autores', texto: 'Autores', icono: 'feather', roles: ['BIBLIOTECARIO'] },
    { ruta: '/categorias', texto: 'Categorías', icono: 'tags', roles: ['BIBLIOTECARIO'] },
    { ruta: '/usuarios', texto: 'Usuarios', icono: 'users', roles: ['BIBLIOTECARIO'] },
    { ruta: '/prestamos', texto: 'Préstamos', icono: 'arrow-left-right', roles: ['BIBLIOTECARIO'] },
    { ruta: '/prestamos', texto: 'Mis préstamos', icono: 'arrow-left-right', roles: ['LECTOR'] },
    { ruta: '/devoluciones', texto: 'Devoluciones', icono: 'undo-2', roles: ['BIBLIOTECARIO'] },
    { ruta: '/reservas', texto: 'Reservas', icono: 'calendar-clock', roles: ['BIBLIOTECARIO'] },
    { ruta: '/reservas', texto: 'Mis reservas', icono: 'calendar-clock', roles: ['LECTOR'] },
    { ruta: '/mi-cuenta', texto: 'Mi cuenta', icono: 'user-round', roles: ['LECTOR'] },
  ];

  protected items = computed(() => {
    const rol = this.auth.usuario()?.rol;
    return this.menu.filter((i) => rol && i.roles.includes(rol));
  });

  protected iniciales = computed(() => {
    const u = this.auth.usuario();
    return u ? (u.nombres[0] + (u.apellidos[0] ?? '')).toUpperCase() : '';
  });

  constructor() {
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => {
      this.menuAbierto.set(false);
      this.notificacionesAbiertas.set(false);
    });
  }

  ngOnInit(): void {
    this.cargarAvisos();
  }

  /** Campana: reservas con libro apartado (listo para retirar). */
  cargarAvisos() {
    this.reservaService.buscar({ estado: 'DISPONIBLE', size: 5 }).subscribe({
      next: (p) => this.avisos.set(p.content),
      error: () => this.avisos.set([]),
    });
  }

  toggleNotificaciones() {
    this.notificacionesAbiertas.update((v) => !v);
    if (this.notificacionesAbiertas()) this.cargarAvisos();
  }

  async salir() {
    const ok = await this.confirm.preguntar({
      titulo: '¿Cerrar sesión?',
      mensaje: 'Tendrá que volver a ingresar sus credenciales.',
      textoConfirmar: 'Cerrar sesión',
    });
    if (ok) this.auth.logout();
  }
}
