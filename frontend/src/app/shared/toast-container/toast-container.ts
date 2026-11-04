import { Component, inject } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-toast-container',
  imports: [LucideAngularModule],
  template: `
    <div class="toasts" aria-live="polite">
      @for (t of toast.toasts(); track t.id) {
        <div class="toast" [class]="'toast ' + t.tipo" role="status">
          <span class="ico">
            <lucide-icon [name]="icono(t.tipo)" [size]="18" />
          </span>
          <div class="txt">
            <strong>{{ t.titulo }}</strong>
            @if (t.mensaje) { <p>{{ t.mensaje }}</p> }
          </div>
          <button class="close" (click)="toast.cerrar(t.id)" aria-label="Cerrar">
            <lucide-icon name="x" [size]="15" />
          </button>
          <span class="bar"></span>
        </div>
      }
    </div>
  `,
  styles: `
    .toasts { position: fixed; right: 20px; bottom: 20px; display: flex; flex-direction: column; gap: 10px; z-index: 1000; }
    .toast { position: relative; overflow: hidden; display: flex; gap: 12px; align-items: flex-start; width: 360px; max-width: calc(100vw - 40px);
      background: #fff; border-radius: 14px; padding: 14px 14px 16px; box-shadow: var(--shadow-lg); border: 1px solid var(--border);
      animation: slideInRight .4s var(--ease) both; }
    .ico { width: 34px; height: 34px; border-radius: 50%; display: grid; place-items: center; flex-shrink: 0; }
    .txt { flex: 1; min-width: 0; strong { font-size: 13.5px; } p { font-size: 12.5px; color: var(--muted); margin-top: 2px; } }
    .close { border: none; background: none; cursor: pointer; color: var(--muted); padding: 2px; border-radius: 6px; &:hover { background: var(--surface-2); } }
    .bar { position: absolute; left: 0; bottom: 0; height: 3px; width: 100%; transform-origin: left; animation: grow 4s linear reverse both; }
    .exito .ico { background: var(--success-50); color: var(--success); } .exito .bar { background: var(--success); }
    .error .ico { background: var(--danger-50); color: var(--danger); } .error .bar { background: var(--danger); animation-duration: 6s; }
    .info .ico { background: var(--info-50); color: var(--info); } .info .bar { background: var(--info); }
    .alerta .ico { background: var(--warning-50); color: var(--warning); } .alerta .bar { background: var(--warning); animation-duration: 6s; }
  `,
})
export class ToastContainer {
  protected toast = inject(ToastService);

  icono(tipo: string): string {
    return { exito: 'circle-check', error: 'circle-x', info: 'info', alerta: 'triangle-alert' }[tipo] ?? 'info';
  }
}
