import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Rol } from '../models/models';
import { AuthService } from '../services/auth.service';

/** Solo usuarios autenticados. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.autenticado() ? true : inject(Router).createUrlTree(['/login']);
};

/** Login: si ya hay sesión se envía al inicio según el rol. */
export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.autenticado() ? inject(Router).createUrlTree([auth.rutaInicio()]) : true;
};

/** Restringe la ruta a ciertos roles (data: { roles: ['BIBLIOTECARIO'] }). */
export const rolGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const roles = (route.data?.['roles'] ?? []) as Rol[];
  if (roles.length === 0 || auth.tieneRol(...roles)) {
    return true;
  }
  return inject(Router).createUrlTree([auth.rutaInicio()]);
};
