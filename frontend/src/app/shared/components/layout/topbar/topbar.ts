import { Component, computed, input } from '@angular/core';

export type NavKey = 'home' | 'play' | 'ranking' | 'profile' | 'admin' | 'users' | 'spiders' | 'reports';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [],
  templateUrl: './topbar.html',
})
export class TopbarComponent {
  active = input<NavKey>('home');
  role = input<'player' | 'admin'>('player');
  user = input<{ name: string; xp: number }>({ name: 'aritzz92', xp: 4280 });

  items = computed<[NavKey, string][]>(() =>
    this.role() === 'admin'
      ? [['admin', 'Resumen'], ['users', 'Usuarios'], ['spiders', 'Spiders'], ['reports', 'Moderación']]
      : [['home', 'Inicio'], ['play', 'Jugar'], ['ranking', 'Ranking'], ['profile', 'Perfil']]
  );

  initials = computed(() => this.user().name.slice(0, 2).toUpperCase());
}
