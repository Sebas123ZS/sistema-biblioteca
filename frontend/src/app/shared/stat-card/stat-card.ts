import { Component, effect, input, signal } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';

export type Tono = 'primary' | 'success' | 'danger' | 'warning' | 'info' | 'violet';

/** Tarjeta de indicador con número animado (cuenta desde 0). */
@Component({
  selector: 'app-stat-card',
  imports: [LucideAngularModule],
  template: `
    <div class="stat card hover-lift" [class]="'stat card hover-lift ' + tono()">
      <div class="ico"><lucide-icon [name]="icono()" [size]="22" /></div>
      <div class="datos">
        <span class="label">{{ etiqueta() }}</span>
        <strong class="valor">{{ mostrado() }}</strong>
        @if (detalle()) { <span class="detalle">{{ detalle() }}</span> }
      </div>
    </div>
  `,
  styles: `
    :host { display: block; }
    .stat { display: flex; gap: 14px; align-items: center; padding: 18px; position: relative; overflow: hidden; }
    .stat::after { content: ''; position: absolute; right: -30px; top: -30px; width: 110px; height: 110px; border-radius: 50%; background: currentColor; opacity: .05; transition: transform .4s var(--ease); }
    .stat:hover::after { transform: scale(1.25); }
    .ico { width: 50px; height: 50px; border-radius: 14px; display: grid; place-items: center; flex-shrink: 0; transition: transform .3s var(--ease); }
    .stat:hover .ico { transform: rotate(-6deg) scale(1.06); }
    .datos { display: flex; flex-direction: column; min-width: 0; }
    .label { color: var(--muted); font-size: 12.5px; font-weight: 500; }
    .valor { font-size: 26px; line-height: 1.2; font-weight: 700; color: var(--text); font-variant-numeric: tabular-nums; }
    .detalle { font-size: 12px; color: var(--muted); }
    .primary { color: var(--primary); .ico { background: var(--primary-50); } }
    .success { color: var(--success); .ico { background: var(--success-50); } }
    .danger { color: var(--danger); .ico { background: var(--danger-50); } }
    .warning { color: var(--warning); .ico { background: var(--warning-50); } }
    .stat.info { color: var(--info); .ico { background: var(--info-50); } }
    .violet { color: var(--violet); .ico { background: var(--violet-50); } }
  `,
})
export class StatCard {
  icono = input.required<string>();
  etiqueta = input.required<string>();
  valor = input<number | null>(0);
  detalle = input<string>();
  tono = input<Tono>('primary');

  protected mostrado = signal(0);

  constructor() {
    effect((onCleanup) => {
      const destino = this.valor() ?? 0;
      const inicio = performance.now();
      const duracion = 700;
      let frame = 0;
      const paso = (t: number) => {
        const p = Math.min(1, (t - inicio) / duracion);
        const ease = 1 - Math.pow(1 - p, 3);
        this.mostrado.set(Math.round(destino * ease));
        if (p < 1) frame = requestAnimationFrame(paso);
      };
      frame = requestAnimationFrame(paso);
      onCleanup(() => cancelAnimationFrame(frame));
    });
  }
}
