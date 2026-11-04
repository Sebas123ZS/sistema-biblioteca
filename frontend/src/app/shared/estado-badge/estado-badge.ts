import { Component, computed, input } from '@angular/core';

const MAPA: Record<string, { clase: string; texto: string }> = {
  ACTIVO: { clase: 'success', texto: 'Activo' },
  INACTIVO: { clase: 'neutral', texto: 'Inactivo' },
  SUSPENDIDO: { clase: 'danger', texto: 'Suspendido' },
  VENCIDO: { clase: 'danger', texto: 'Vencido' },
  DEVUELTO: { clase: 'info', texto: 'Devuelto' },
  PENDIENTE: { clase: 'warning', texto: 'Pendiente' },
  DISPONIBLE: { clase: 'success', texto: 'Disponible' },
  ATENDIDA: { clase: 'info', texto: 'Atendida' },
  CANCELADA: { clase: 'neutral', texto: 'Cancelada' },
  EXPIRADA: { clase: 'neutral', texto: 'Expirada' },
  BUENO: { clase: 'success', texto: 'Buen estado' },
  DANADO: { clase: 'danger', texto: 'Dañado' },
  BIBLIOTECARIO: { clase: 'violet', texto: 'Bibliotecario' },
  LECTOR: { clase: 'primary', texto: 'Lector' },
  SI: { clase: 'success', texto: 'Disponible' },
  NO: { clase: 'danger', texto: 'No disponible' },
};

@Component({
  selector: 'app-estado-badge',
  template: `<span class="badge" [class]="'badge ' + info().clase">{{ info().texto }}</span>`,
})
export class EstadoBadge {
  estado = input.required<string>();
  info = computed(() => MAPA[this.estado()] ?? { clase: 'neutral', texto: this.estado() });
}
