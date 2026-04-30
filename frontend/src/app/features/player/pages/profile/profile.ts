import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { TopbarComponent } from '../../../../shared/components/layout/topbar/topbar';
import { AuthService } from '../../../../core/services/auth.service';
import { UserService } from '../../../../core/services/user.service';
import { StatsService } from '../../../../core/services/stats.service';
import { UserMe } from '../../../../core/models/auth.models';
import { GameMode, PlayerStats } from '../../../../core/models/game.models';

const MODE_LABEL: Record<GameMode, string> = {
  SURVIVAL: 'Supervivencia',
  PRECISION: 'Precisión',
  BINARY_DUEL: 'Duelo binario',
  PRECISION_DUEL: 'Duelo de precisión',
  SABOTAGE: 'Sabotaje',
};

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [TopbarComponent],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly users = inject(UserService);
  private readonly statsApi = inject(StatsService);

  readonly me = signal<UserMe | null>(null);
  readonly statsList = signal<PlayerStats[]>([]);

  readonly initials = computed(() => {
    const u = this.me()?.username ?? this.auth.user()?.username ?? '';
    return u.slice(0, 2).toUpperCase() || '??';
  });

  readonly username = computed(() => this.me()?.username ?? this.auth.user()?.username ?? '—');

  readonly joined = computed(() => {
    const iso = this.me()?.createdAt;
    if (!iso) return '—';
    const d = new Date(iso);
    return d.toLocaleDateString('es-ES', { month: 'long', year: 'numeric' });
  });

  readonly totalGames = computed(() =>
    this.statsList().reduce((s, x) => s + x.gamesPlayed, 0)
  );

  readonly bestStreakOverall = computed(() =>
    this.statsList().reduce((m, x) => Math.max(m, x.bestStreak), 0)
  );

  readonly modeStats = computed(() =>
    this.statsList().map((s) => ({
      mode: MODE_LABEL[s.mode] ?? s.mode,
      games: s.gamesPlayed,
      wins: s.gamesWon,
      acc: s.gamesPlayed === 0 ? '—' : `${s.winRate}%`,
      best: s.bestStreak,
    }))
  );

  // Categories + achievements + history come from endpoints not yet implemented.
  categories: { cat: string; pct: number }[] = [];
  achievements: { t: string; s: string; ico: string; c: string }[] = [];
  history: { mode: string; win: boolean; streak: number; pts: string; when: string }[] = [];

  ngOnInit(): void {
    this.users.me().subscribe({ next: (u) => this.me.set(u), error: () => {} });
    this.statsApi.mine().subscribe({ next: (l) => this.statsList.set(l ?? []), error: () => {} });
  }
}
