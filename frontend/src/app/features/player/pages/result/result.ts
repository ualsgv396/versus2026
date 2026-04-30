import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TopbarComponent } from '../../../../shared/components/layout/topbar/topbar';

interface ResultState {
  mode: 'SURVIVAL' | 'PRECISION';
  score: number;
  bestStreak: number;
  rounds: number;
  won: boolean;
}

@Component({
  selector: 'app-result',
  standalone: true,
  imports: [RouterLink, TopbarComponent],
  templateUrl: './result.html',
  styleUrl: './result.scss',
})
export class Result {
  private readonly router = inject(Router);

  readonly state = signal<ResultState | null>(this.readState());
  readonly isWin = computed(() => this.state()?.won ?? false);
  readonly modeLabel = computed(() => {
    const m = this.state()?.mode ?? 'SURVIVAL';
    return m === 'PRECISION' ? 'PRECISIÓN' : 'SUPERVIVENCIA';
  });

  readonly stats = computed(() => {
    const s = this.state();
    if (!s) {
      return [
        { num: '—', label: 'Racha máxima', accent: 'var(--vs-accent-gold)' },
        { num: '—', label: 'Puntos',       accent: 'var(--vs-accent-green)' },
        { num: '—', label: 'Rondas' },
      ];
    }
    return [
      { num: String(s.bestStreak), label: 'Racha máxima', accent: 'var(--vs-accent-gold)' },
      { num: s.score.toLocaleString('es-ES'), label: 'Puntos', accent: 'var(--vs-accent-green)' },
      { num: String(s.rounds), label: 'Rondas' },
    ];
  });

  readonly replayLink = computed(() =>
    this.state()?.mode === 'PRECISION' ? '/play/precision' : '/play/survival'
  );

  private readState(): ResultState | null {
    const nav = this.router.getCurrentNavigation();
    const fromNav = nav?.extras?.state as ResultState | undefined;
    if (fromNav) return fromNav;
    const fromHistory = (history.state ?? null) as ResultState | null;
    if (fromHistory && typeof fromHistory.score === 'number') return fromHistory;
    return null;
  }
}
