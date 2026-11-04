import { Component, ElementRef, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';

interface Cita {
  texto: string;
  autor: string;
}

/**
 * Panel izquierdo animado del login: fondo "aurora", libro 3D que pasa páginas
 * y se inclina con el mouse, tarjetas flotantes, palabra rotativa y carrusel de frases.
 */
@Component({
  selector: 'app-showcase',
  imports: [LucideAngularModule],
  templateUrl: './showcase.html',
  styleUrl: './showcase.scss',
  host: {
    '(mousemove)': 'mover($event)',
    '(mouseleave)': 'reiniciar()',
  },
})
export class Showcase implements OnInit, OnDestroy {
  private el = inject(ElementRef<HTMLElement>);

  protected readonly palabras = ['digital', 'organizada', 'a un clic', 'para todos'];
  protected readonly citas: Cita[] = [
    { texto: 'Siempre imaginé que el Paraíso sería algún tipo de biblioteca.', autor: 'Jorge Luis Borges' },
    { texto: 'Si tienes un jardín y una biblioteca, tienes todo lo que necesitas.', autor: 'Cicerón' },
    { texto: 'El conocimiento también construye un mejor mañana.', autor: 'Sistema de Gestión de Biblioteca' },
  ];
  protected readonly particulas = Array.from({ length: 18 }, (_, i) => ({
    left: (i * 53) % 100,
    delay: (i * 0.7) % 9,
    dur: 9 + (i % 5) * 2,
    size: 3 + (i % 3) * 2,
  }));

  protected palabra = signal(0);
  protected cita = signal(0);
  protected tiltX = signal(0);
  protected tiltY = signal(0);

  private timers: ReturnType<typeof setInterval>[] = [];

  ngOnInit(): void {
    this.timers.push(setInterval(() => this.palabra.update((i) => (i + 1) % this.palabras.length), 3200));
    this.timers.push(setInterval(() => this.siguienteCita(), 6000));
  }

  ngOnDestroy(): void {
    this.timers.forEach(clearInterval);
  }

  irACita(i: number) {
    this.cita.set(i);
    clearInterval(this.timers[1]);
    this.timers[1] = setInterval(() => this.siguienteCita(), 6000);
  }

  private siguienteCita() {
    this.cita.update((i) => (i + 1) % this.citas.length);
  }

  /** Efecto parallax: el libro y las tarjetas siguen al mouse. */
  mover(ev: MouseEvent) {
    const r = (this.el.nativeElement as HTMLElement).getBoundingClientRect();
    this.tiltX.set((ev.clientX - r.left) / r.width - 0.5);
    this.tiltY.set((ev.clientY - r.top) / r.height - 0.5);
  }

  reiniciar() {
    this.tiltX.set(0);
    this.tiltY.set(0);
  }
}
