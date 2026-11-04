import { Injectable, signal } from '@angular/core';

export interface ConfirmOpciones {
  titulo: string;
  mensaje: string;
  textoConfirmar?: string;
  peligro?: boolean;
}

interface ConfirmEstado extends ConfirmOpciones {
  resolver: (valor: boolean) => void;
}

/** Diálogo de confirmación reutilizable: `await confirm.preguntar({...})`. */
@Injectable({ providedIn: 'root' })
export class ConfirmService {
  readonly actual = signal<ConfirmEstado | null>(null);

  preguntar(opciones: ConfirmOpciones): Promise<boolean> {
    return new Promise((resolver) => this.actual.set({ ...opciones, resolver }));
  }

  responder(valor: boolean) {
    const estado = this.actual();
    if (estado) {
      estado.resolver(valor);
      this.actual.set(null);
    }
  }
}
