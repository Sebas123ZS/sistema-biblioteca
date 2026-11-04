import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

/**
 * 1) Agrega "Authorization: Bearer <token>" a cada petición.
 * 2) Si el backend responde 401 (token vencido o inválido) cierra la sesión.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const toast = inject(ToastService);
  const token = auth.token;

  const peticion = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(peticion).pipe(
    catchError((err: HttpErrorResponse) => {
      const esLogin = req.url.endsWith('/auth/login') || req.url.endsWith('/auth/registro');
      if (err.status === 401 && !esLogin) {
        toast.alerta('Sesión finalizada', 'Vuelva a iniciar sesión');
        auth.logout();
      }
      return throwError(() => err);
    }),
  );
};
