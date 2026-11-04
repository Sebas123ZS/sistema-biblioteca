import { Component, ContentChild, ElementRef, HostListener, inject, input, output, signal, TemplateRef } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { Observable, Subject, debounceTime, distinctUntilChanged, switchMap, of, catchError } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

/**
 * Selector con búsqueda (autocompletado). El padre define cómo buscar y cómo
 * dibujar cada opción con <ng-template let-item>.
 */
@Component({
  selector: 'app-search-select',
  imports: [LucideAngularModule, NgTemplateOutlet],
  template: `
    <div class="ss" [class.open]="abierto()">
      <div class="input-icon">
        <lucide-icon name="search" [size]="16" />
        <input class="input" [placeholder]="placeholder()" [value]="texto()" [class.invalid]="invalido()"
               (input)="escribir($any($event.target).value)" (focus)="abrir()" autocomplete="off" />
      </div>
      @if (abierto()) {
        <div class="dropdown">
          @if (cargando()) {
            <div class="msg"><span class="spinner dark"></span> Buscando…</div>
          } @else if (opciones().length === 0) {
            <div class="msg muted">Sin coincidencias</div>
          } @else {
            @for (op of opciones(); track $index) {
              <button type="button" class="op" [class.disabled]="deshabilitar()(op)" (click)="elegir(op)" [style.--i]="$index">
                <ng-container *ngTemplateOutlet="plantilla; context: { $implicit: op }" />
              </button>
            }
          }
        </div>
      }
    </div>
  `,
  styles: `
    .ss { position: relative; }
    .dropdown { position: absolute; top: calc(100% + 6px); left: 0; right: 0; z-index: 50; background: #fff; border: 1px solid var(--border); border-radius: 12px; box-shadow: var(--shadow-lg); max-height: 290px; overflow-y: auto; padding: 6px; animation: scaleIn .2s var(--ease) both; }
    .op { width: 100%; text-align: left; border: none; background: none; padding: 9px 10px; border-radius: 9px; cursor: pointer; display: block; animation: fadeUp .25s var(--ease) both; animation-delay: calc(var(--i) * 25ms);
      &:hover { background: var(--primary-50); } &.disabled { opacity: .5; } }
    .msg { padding: 12px; display: flex; gap: 8px; align-items: center; font-size: 13px; }
  `,
})
export class SearchSelect<T> {
  buscar = input.required<(q: string) => Observable<T[]>>();
  placeholder = input('Buscar…');
  invalido = input(false);
  deshabilitar = input<(item: T) => boolean>(() => false);
  seleccionado = output<T>();

  @ContentChild(TemplateRef) plantilla!: TemplateRef<unknown>;

  protected texto = signal('');
  protected abierto = signal(false);
  protected cargando = signal(false);
  protected opciones = signal<T[]>([]);

  private el = inject(ElementRef);
  private consultas = new Subject<string>();

  constructor() {
    this.consultas.pipe(
      debounceTime(250),
      distinctUntilChanged(),
      switchMap((q) => {
        this.cargando.set(true);
        return this.buscar()(q).pipe(catchError(() => of([] as T[])));
      }),
      takeUntilDestroyed(),
    ).subscribe((res) => {
      this.opciones.set(res);
      this.cargando.set(false);
    });
  }

  abrir() {
    this.abierto.set(true);
    this.consultas.next(this.texto());
  }

  escribir(valor: string) {
    this.texto.set(valor);
    this.abierto.set(true);
    this.consultas.next(valor);
  }

  elegir(op: T) {
    if (this.deshabilitar()(op)) return;
    this.seleccionado.emit(op);
    this.abierto.set(false);
    this.texto.set('');
  }

  @HostListener('document:mousedown', ['$event'])
  fuera(ev: MouseEvent) {
    if (!this.el.nativeElement.contains(ev.target)) this.abierto.set(false);
  }
}
