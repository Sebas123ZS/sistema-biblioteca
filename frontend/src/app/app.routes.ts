import { inject } from '@angular/core';
import { Routes } from '@angular/router';
import { authGuard, invitadoGuard, rolGuard } from './core/guards/auth.guards';
import { AuthService } from './core/services/auth.service';

const SOLO_BIBLIOTECARIO = { roles: ['BIBLIOTECARIO'] };
const SOLO_LECTOR = { roles: ['LECTOR'] };

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [invitadoGuard],
    title: 'Iniciar sesión · Biblioteca',
    loadComponent: () => import('./pages/login/login').then((m) => m.LoginPage),
  },
  {
    path: 'registro',
    canActivate: [invitadoGuard],
    title: 'Crear cuenta · Biblioteca',
    data: { modo: 'registro' },
    loadComponent: () => import('./pages/login/login').then((m) => m.LoginPage),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell/shell').then((m) => m.Shell),
    children: [
      { path: '', pathMatch: 'full', redirectTo: () => inject(AuthService).rutaInicio() },
      {
        path: 'dashboard', canActivate: [rolGuard], data: SOLO_BIBLIOTECARIO, title: 'Dashboard · Biblioteca',
        loadComponent: () => import('./pages/dashboard/dashboard').then((m) => m.DashboardPage),
      },
      {
        path: 'libros', canActivate: [rolGuard], data: SOLO_BIBLIOTECARIO, title: 'Libros · Biblioteca',
        loadComponent: () => import('./pages/libros/libros').then((m) => m.LibrosPage),
      },
      {
        path: 'catalogo', title: 'Catálogo · Biblioteca',
        loadComponent: () => import('./pages/libros/libros').then((m) => m.LibrosPage),
      },
      {
        path: 'autores', canActivate: [rolGuard], data: SOLO_BIBLIOTECARIO, title: 'Autores · Biblioteca',
        loadComponent: () => import('./pages/autores/autores').then((m) => m.AutoresPage),
      },
      {
        path: 'categorias', canActivate: [rolGuard], data: SOLO_BIBLIOTECARIO, title: 'Categorías · Biblioteca',
        loadComponent: () => import('./pages/categorias/categorias').then((m) => m.CategoriasPage),
      },
      {
        path: 'usuarios', canActivate: [rolGuard], data: SOLO_BIBLIOTECARIO, title: 'Usuarios · Biblioteca',
        loadComponent: () => import('./pages/usuarios/usuarios').then((m) => m.UsuariosPage),
      },
      {
        path: 'prestamos', title: 'Préstamos · Biblioteca',
        loadComponent: () => import('./pages/prestamos/prestamos').then((m) => m.PrestamosPage),
      },
      {
        path: 'devoluciones', canActivate: [rolGuard], data: SOLO_BIBLIOTECARIO, title: 'Devoluciones · Biblioteca',
        loadComponent: () => import('./pages/devoluciones/devoluciones').then((m) => m.DevolucionesPage),
      },
      {
        path: 'reservas', title: 'Reservas · Biblioteca',
        loadComponent: () => import('./pages/reservas/reservas').then((m) => m.ReservasPage),
      },
      {
        path: 'mi-cuenta', canActivate: [rolGuard], data: SOLO_LECTOR, title: 'Mi cuenta · Biblioteca',
        loadComponent: () => import('./pages/mi-cuenta/mi-cuenta').then((m) => m.MiCuentaPage),
      },
    ],
  },
  {
    path: '**',
    title: 'Página no encontrada',
    loadComponent: () => import('./pages/not-found/not-found').then((m) => m.NotFoundPage),
  },
];
