import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink, LucideAngularModule],
  template: `
    <div class="nf">
      <span class="code">404</span>
      <h1>Página no encontrada</h1>
      <p class="muted">La página que busca no existe o fue movida.</p>
      <a class="btn btn-primary mt-3" [routerLink]="auth.autenticado() ? auth.rutaInicio() : '/login'">
        <lucide-icon name="arrow-right" [size]="16" /> Volver al inicio
      </a>
    </div>
  `,
  styles: `
    .nf { min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; padding: 24px; animation: fadeUp .5s var(--ease) both; }
    .code { font-family: var(--font-display); font-size: 120px; line-height: 1; font-weight: 700;
      background: linear-gradient(135deg, #2a7cf0, var(--navy-800)); -webkit-background-clip: text; background-clip: text; color: transparent; }
  `,
})
export class NotFoundPage {
  protected auth = inject(AuthService);
}
