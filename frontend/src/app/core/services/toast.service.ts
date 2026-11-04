import { Injectable, signal } from '@angular/core';

export type ToastTipo = 'exito' | 'error' | 'info' | 'alerta';

export interface Toast {
  id: number;
  tipo: ToastTipo;
  titulo: string;
  mensaje?: string;
}

/** Notificaciones flotantes (esquina inferior derecha). */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private contador = 0;
  readonly toasts = signal<Toast[]>([]);

  exito(titulo: string, mensaje?: string) { this.mostrar('exito', titulo, mensaje); }
  error(titulo: string, mensaje?: string) { this.mostrar('error', titulo, mensaje, 6000); }
  info(titulo: string, mensaje?: string) { this.mostrar('info', titulo, mensaje); }
  alerta(titulo: string, mensaje?: string) { this.mostrar('alerta', titulo, mensaje, 6000); }

  cerrar(id: number) {
    this.toasts.update((lista) => lista.filter((t) => t.id !== id));
  }

  private mostrar(tipo: ToastTipo, titulo: string, mensaje?: string, duracion = 4000) {
    const id = ++this.contador;
    this.toasts.update((lista) => [...lista, { id, tipo, titulo, mensaje }]);
    setTimeout(() => this.cerrar(id), duracion);
  }
}
