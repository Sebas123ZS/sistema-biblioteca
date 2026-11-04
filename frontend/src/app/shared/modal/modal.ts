import { Component, HostListener, input, output } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';

/** Ventana modal con encabezado, contenido proyectado y pie opcional ([modal-footer]). */
@Component({
  selector: 'app-modal',
  imports: [LucideAngularModule],
  template: `
    <div class="backdrop" (mousedown)="cerrar.emit()">
      <div class="modal" [style.width.px]="ancho()" role="dialog" aria-modal="true" (mousedown)="$event.stopPropagation()">
        <header>
          <div class="flex">
            @if (icono()) {
              <span class="ico"><lucide-icon [name]="icono()!" [size]="18" /></span>
            }
            <div>
              <h2>{{ titulo() }}</h2>
              @if (subtitulo()) { <p class="muted small">{{ subtitulo() }}</p> }
            </div>
          </div>
          <button class="icon-btn" (click)="cerrar.emit()" aria-label="Cerrar"><lucide-icon name="x" [size]="16" /></button>
        </header>
        <div class="body"><ng-content /></div>
        <footer><ng-content select="[modal-footer]" /></footer>
      </div>
    </div>
  `,
  styles: `
    .backdrop { position: fixed; inset: 0; background: rgba(7, 21, 40, .45); backdrop-filter: blur(3px); display: grid; place-items: center; z-index: 800; padding: 16px; animation: fadeIn .2s ease; }
    .modal { max-width: 100%; max-height: calc(100vh - 32px); display: flex; flex-direction: column; background: #fff; border-radius: 20px; box-shadow: var(--shadow-lg); animation: scaleIn .3s var(--ease) both; }
    header { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; padding: 20px 22px 14px; border-bottom: 1px solid var(--border); }
    .ico { width: 38px; height: 38px; border-radius: 11px; display: grid; place-items: center; background: var(--primary-50); color: var(--primary); }
    .body { padding: 18px 22px; overflow-y: auto; }
    footer { padding: 0 22px 20px; display: flex; justify-content: flex-end; gap: 10px; &:empty { display: none; } }
  `,
})
export class Modal {
  titulo = input.required<string>();
  subtitulo = input<string>();
  icono = input<string>();
  ancho = input<number>(560);
  cerrar = output<void>();

  @HostListener('document:keydown.escape')
  onEsc() {
    this.cerrar.emit();
  }
}
