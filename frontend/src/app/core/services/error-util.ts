import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormGroup } from '@angular/forms';
import { ApiError } from '../models/models';

/** Mensaje legible a partir de un error HTTP del backend. */
export function mensajeError(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'No se pudo conectar con el servidor. Verifique que el backend esté encendido.';
    }
    const body = err.error as ApiError | null;
    if (body?.mensaje) {
      return body.mensaje;
    }
  }
  return 'Ocurrió un error inesperado';
}

/** Copia los errores de validación del backend ({campo: mensaje}) a los controles del formulario. */
export function aplicarErroresServidor(form: FormGroup, err: unknown): void {
  if (err instanceof HttpErrorResponse && err.status === 400) {
    const errores = (err.error as ApiError | null)?.errores ?? {};
    for (const [campo, mensaje] of Object.entries(errores)) {
      const control: AbstractControl | null = form.get(campo);
      control?.setErrors({ servidor: mensaje });
      control?.markAsTouched();
    }
  }
}
