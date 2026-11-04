import { Component, HostListener, inject } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';
import { ConfirmService } from '../../core/services/confirm.service';

@Component({
  selector: 'app-confirm-dialog',
  imports: [LucideAngularModule],
  template: `
    @if (confirm.actual(); as c) {
      <div class="backdrop" (click)="confirm.responder(false)">
        <div class="dialog" role="alertdialog" aria-modal="true" (click)="$event.stopPropagation()">
          <div class="icon" [class.danger]="c.peligro">
            <lucide-icon [name]="c.peligro ? 'triangle-alert' : 'circle-alert'" [size]="22" />
          </div>
          <h3>{{ c.titulo }}</h3>
          <p>{{ c.mensaje }}</p>
          <div class="acciones">
            <button class="btn" (click)="confirm.responder(false)">Cancelar</button>
            <button class="btn" [class.btn-danger]="c.peligro" [class.btn-primary]="!c.peligro"
                    (click)="confirm.responder(true)">{{ c.textoConfirmar ?? 'Confirmar' }}</button>
          </div>
        </div>
      </div>
    }
  `,
  styles: `
    .backdrop { position: fixed; inset: 0; background: rgba(7, 21, 40, .45); backdrop-filter: blur(3px); display: grid; place-items: center; z-index: 900; animation: fadeIn .2s ease; padding: 16px; }
    .dialog { width: 420px; max-width: 100%; background: #fff; border-radius: 18px; padding: 26px; text-align: center; box-shadow: var(--shadow-lg); animation: scaleIn .28s var(--ease) both; }
    .icon { width: 52px; height: 52px; margin: 0 auto 14px; border-radius: 50%; display: grid; place-items: center; background: var(--primary-50); color: var(--primary); animation: pulse-ring 1.4s ease-out 2; }
    .icon.danger { background: var(--danger-50); color: var(--danger); }
    h3 { font-size: 17px; margin-bottom: 6px; }
    p { color: var(--muted); font-size: 13.5px; }
    .acciones { display: flex; gap: 10px; justify-content: center; margin-top: 22px; .btn { min-width: 120px; } }
  `,
})
export class ConfirmDialog {
  protected confirm = inject(ConfirmService);

  @HostListener('document:keydown.escape')
  cerrar() {
    this.confirm.responder(false);
  }
}
