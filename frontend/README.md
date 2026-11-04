# Frontend – Sistema de Gestión de Biblioteca (Angular 20)

```bash
npm install        # solo la primera vez
npm start          # abre en http://localhost:4200
```

El backend debe estar corriendo en `http://localhost:8080` (configurable en `src/environments/environment.ts`).

Estructura:

```
src/app
├── core/          modelos, servicios HTTP, interceptor JWT y guards por rol
├── shared/        componentes reutilizables (modal, paginación, toasts, portadas…)
├── layout/shell/  barra lateral + barra superior
└── pages/         login, dashboard, libros/catálogo, autores, categorías,
                   usuarios, préstamos, devoluciones, reservas, mi-cuenta
src/styles/        SASS: variables, animaciones, base y componentes
```
