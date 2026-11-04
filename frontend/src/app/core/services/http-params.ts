import { HttpParams } from '@angular/common/http';

/** Convierte un objeto de filtros en HttpParams omitiendo valores vacíos. */
export function toParams(filtros: object): HttpParams {
  let params = new HttpParams();
  for (const [clave, valor] of Object.entries(filtros)) {
    if (valor !== null && valor !== undefined && valor !== '') {
      params = params.set(clave, String(valor));
    }
  }
  return params;
}
