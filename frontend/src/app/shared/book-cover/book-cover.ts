import { Component, computed, input, signal } from '@angular/core';

const PALETAS = [
  ['#1d4e89', '#3f88c5'], ['#7b2d26', '#c4543d'], ['#1b5e4b', '#3aa17e'], ['#5b3a8c', '#9b6fd6'],
  ['#8a5a0b', '#d99a2b'], ['#173b5c', '#2f6d9e'], ['#6b2148', '#b5487f'], ['#2d4a22', '#5f8f45'],
];

/**
 * Portada del libro. Si la imagen no carga (sin internet o sin URL),
 * dibuja una portada generada con el título.
 */
@Component({
  selector: 'app-book-cover',
  template: `
    <div class="cover" [style.width.px]="ancho()" [style.height.px]="ancho() * 1.45"
         [style.background]="fondo()" [class.with-img]="mostrarImagen()">
      @if (mostrarImagen()) {
        <img [src]="url()" [alt]="titulo()" loading="lazy" (error)="fallo.set(true)" (load)="verificar($event)" />
      } @else {
        <span class="lomo"></span>
        <span class="titulo" [style.font-size.px]="ancho() / 7.5">{{ titulo() }}</span>
      }
    </div>
  `,
  styles: `
    .cover { position: relative; border-radius: 4px 8px 8px 4px; overflow: hidden; flex-shrink: 0; box-shadow: 0 6px 14px rgba(11, 31, 58, .22), inset 0 0 0 1px rgba(255,255,255,.08); display: flex; align-items: center; justify-content: center; }
    .cover.with-img { background: #e9eef5 !important; }
    img { width: 100%; height: 100%; object-fit: cover; display: block; }
    .lomo { position: absolute; left: 0; top: 0; bottom: 0; width: 12%; background: rgba(0, 0, 0, .18); border-right: 1px solid rgba(255,255,255,.15); }
    .titulo { position: relative; color: #fff; font-family: var(--font-display); font-weight: 600; text-align: center; padding: 0 10% 0 18%; line-height: 1.15; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 4; -webkit-box-orient: vertical; text-shadow: 0 1px 2px rgba(0,0,0,.25); }
  `,
})
export class BookCover {
  url = input<string | null | undefined>();
  titulo = input('');
  ancho = input(44);

  protected fallo = signal(false);
  protected mostrarImagen = computed(() => !!this.url() && !this.fallo());
  protected fondo = computed(() => {
    let h = 0;
    for (const c of this.titulo()) h = (h * 31 + c.charCodeAt(0)) >>> 0;
    const [a, b] = PALETAS[h % PALETAS.length];
    return `linear-gradient(150deg, ${a}, ${b})`;
  });

  /** Open Library devuelve una imagen de 1x1 cuando no tiene la portada. */
  verificar(ev: Event) {
    const img = ev.target as HTMLImageElement;
    if (img.naturalWidth < 10) this.fallo.set(true);
  }
}
