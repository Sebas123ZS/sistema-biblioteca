import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import localeEs from '@angular/common/locales/es-PE';
import {
  ApplicationConfig, importProvidersFrom, LOCALE_ID, provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideRouter, withComponentInputBinding, withInMemoryScrolling, withViewTransitions } from '@angular/router';
import {
  ArrowLeftRight, ArrowRight, BadgeCheck, Ban, Bell, BookCopy, BookMarked, BookOpen, BookPlus, Bookmark,
  Calendar, CalendarCheck, CalendarClock, CalendarX, Check, ChevronDown, ChevronLeft, ChevronRight, CircleAlert,
  CircleCheck, CircleX, Clock, Eye, EyeOff, Feather, History, Hourglass, IdCard, Inbox, Info, LayoutDashboard,
  LayoutGrid, Layers, Library, LibraryBig, List, ListFilter, Lock, LogOut, LucideAngularModule, Mail, MapPin, Menu,
  Pencil, Phone, Plus, RefreshCw, RotateCcw, Save, Search, ShieldCheck, Sparkles, Tags, Trash2, TrendingUp,
  TriangleAlert, Undo2, User, UserCheck, UserPlus, UserRound, Users, UserX, X,
} from 'lucide-angular';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';

registerLocaleData(localeEs, 'es-PE');

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding(), withViewTransitions(),
      withInMemoryScrolling({ scrollPositionRestoration: 'top' })),
    provideHttpClient(withInterceptors([authInterceptor])),
    { provide: LOCALE_ID, useValue: 'es-PE' },
    importProvidersFrom(LucideAngularModule.pick({
      ArrowLeftRight, ArrowRight, BadgeCheck, Ban, Bell, BookCopy, BookMarked, BookOpen, BookPlus, Bookmark,
      Calendar, CalendarCheck, CalendarClock, CalendarX, Check, ChevronDown, ChevronLeft, ChevronRight, CircleAlert,
      CircleCheck, CircleX, Clock, Eye, EyeOff, Feather, History, Hourglass, IdCard, Inbox, Info, LayoutDashboard,
      LayoutGrid, Layers, Library, LibraryBig, List, ListFilter, Lock, LogOut, Mail, MapPin, Menu,
      Pencil, Phone, Plus, RefreshCw, RotateCcw, Save, Search, ShieldCheck, Sparkles, Tags, Trash2, TrendingUp,
      TriangleAlert, Undo2, User, UserCheck, UserPlus, UserRound, Users, UserX, X,
    })),
  ],
};
