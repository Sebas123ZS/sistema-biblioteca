import { Component, computed, input, output } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-pagination',
  imports: [LucideAngularModule],
  template: `
    <div class="pag">
      <span class="muted small">
        @if (total() > 0) {
          Mostrando {{ desde() }}–{{ hasta() }} de {{ total() }} {{ etiqueta() }}
        } @else {
          Sin resultados
        }
      </span>
      @if (totalPaginas() > 1) {
        <div class="pages">
          <button class="pg" [disabled]="pagina() === 0" (click)="ir(pagina() - 1)" aria-label="Anterior">
            <lucide-icon name="chevron-left" [size]="15" />
          </button>
          @for (p of paginas(); track $index) {
            @if (p === -1) {
              <span class="dots">…</span>
            } @else {
              <button class="pg" [class.active]="p === pagina()" (click)="ir(p)">{{ p + 1 }}</button>
            }
          }
          <button class="pg" [disabled]="pagina() >= totalPaginas() - 1" (click)="ir(pagina() + 1)" aria-label="Siguiente">
            <lucide-icon name="chevron-right" [size]="15" />
          </button>
        </div>
      }
    </div>
  `,
  styles: `
    .pag { display: flex; justify-content: space-between; align-items: center; gap: 12px; padding: 14px 20px; border-top: 1px solid var(--border); flex-wrap: wrap; }
    .pages { display: flex; gap: 6px; align-items: center; }
    .pg { min-width: 32px; height: 32px; border-radius: 8px; border: 1px solid var(--border); background: #fff; cursor: pointer; display: grid; place-items: center; font-weight: 600; font-size: 12.5px; color: var(--text-2); transition: all .18s var(--ease);
      &:hover:not(:disabled):not(.active) { border-color: var(--primary); color: var(--primary); transform: translateY(-1px); }
      &.active { background: var(--primary); border-color: var(--primary); color: #fff; box-shadow: 0 4px 10px rgba(29, 111, 224, .3); }
      &:disabled { opacity: .4; cursor: not-allowed; } }
    .dots { color: var(--muted); padding: 0 2px; }
  `,
})
export class Pagination {
  pagina = input(0);
  totalPaginas = input(0);
  total = input(0);
  tamano = input(10);
  etiqueta = input('registros');
  cambiar = output<number>();

  desde = computed(() => this.pagina() * this.tamano() + 1);
  hasta = computed(() => Math.min((this.pagina() + 1) * this.tamano(), this.total()));

  /** Números de página con "…" cuando hay muchas. */
  paginas = computed(() => {
    const total = this.totalPaginas();
    const actual = this.pagina();
    if (total <= 7) return Array.from({ length: total }, (_, i) => i);
    const set = new Set([0, total - 1, actual - 1, actual, actual + 1].filter((p) => p >= 0 && p < total));
    const orden = [...set].sort((a, b) => a - b);
    const res: number[] = [];
    orden.forEach((p, i) => {
      if (i > 0 && p - orden[i - 1] > 1) res.push(-1);
      res.push(p);
    });
    return res;
  });

  ir(p: number) {
    if (p >= 0 && p < this.totalPaginas() && p !== this.pagina()) this.cambiar.emit(p);
  }
}
