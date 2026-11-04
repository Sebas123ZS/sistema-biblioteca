import { Component, input } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-empty-state',
  imports: [LucideAngularModule],
  template: `
    <div class="empty">
      <div class="ico"><lucide-icon [name]="icono()" [size]="26" /></div>
      <h3>{{ titulo() }}</h3>
      @if (texto()) { <p>{{ texto() }}</p> }
      <ng-content />
    </div>
  `,
  styles: `
    .empty { text-align: center; padding: 40px 20px; animation: fadeIn .3s ease; }
    .ico { width: 60px; height: 60px; margin: 0 auto 12px; border-radius: 18px; display: grid; place-items: center; background: var(--surface-2); color: var(--muted); }
    h3 { font-size: 15px; } p { color: var(--muted); font-size: 13px; margin-top: 4px; }
  `,
})
export class EmptyState {
  icono = input('inbox');
  titulo = input('Sin resultados');
  texto = input<string>();
}
