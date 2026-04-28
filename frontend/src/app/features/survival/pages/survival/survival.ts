import { Component, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CompareCardComponent, CardItem } from '../../components/compare-card/compare-card';

interface Question {
  category: string;
  a: { label: string; sub: string; value: number; unit: string };
  b: { label: string; sub: string; value: number; unit: string };
}

const QUESTIONS: Question[] = [
  {
    category: 'Fútbol',
    a: { label: 'Cristiano Ronaldo', sub: 'Seguidores en Instagram', value: 638_000_000, unit: '' },
    b: { label: 'Lionel Messi',      sub: 'Seguidores en Instagram', value: 503_000_000, unit: '' },
  },
  {
    category: 'Cine',
    a: { label: 'Avatar (2009)',        sub: 'Recaudación mundial', value: 2_923_000_000, unit: '$' },
    b: { label: 'Vengadores: Endgame',  sub: 'Recaudación mundial', value: 2_799_000_000, unit: '$' },
  },
  {
    category: 'Música',
    a: { label: 'Bad Bunny',    sub: 'Oyentes mensuales Spotify', value: 79_500_000,  unit: '' },
    b: { label: 'Taylor Swift', sub: 'Oyentes mensuales Spotify', value: 102_300_000, unit: '' },
  },
];

type Phase = 'idle' | 'correct' | 'wrong';

@Component({
  selector: 'app-survival',
  standalone: true,
  imports: [RouterLink, CompareCardComponent],
  templateUrl: './survival.html',
  styleUrl:    './survival.scss',
})
export class Survival {
  lives  = signal(3);
  streak = signal(7);
  score  = signal(12_480);
  phase  = signal<Phase>('idle');
  qIdx   = signal(0);

  feedback = signal<{ isCorrect: boolean; delta: number } | null>(null);

  question = computed(() => QUESTIONS[this.qIdx() % QUESTIONS.length]);

  cardA = computed<CardItem>(() => ({
    ...this.question().a, cat: this.question().category, stub: 'foto figura A',
  }));
  cardB = computed<CardItem>(() => ({
    ...this.question().b, cat: this.question().category, stub: 'foto figura B',
  }));

  winner = computed<'a' | 'b'>(() =>
    this.question().a.value > this.question().b.value ? 'a' : 'b'
  );

  cardStateA = computed(() =>
    this.phase() === 'idle' ? 'idle' : (this.winner() === 'a' ? 'correct' : 'wrong')
  );
  cardStateB = computed(() =>
    this.phase() === 'idle' ? 'idle' : (this.winner() === 'b' ? 'correct' : 'wrong')
  );

  showBurst = computed(() => this.phase() === 'correct' && this.streak() >= 5);
  burstItems = Array.from({ length: 12 }, (_, i) => i);

  fmt(n: number): string { return n.toLocaleString('es-ES'); }

  pick(side: 'a' | 'b'): void {
    if (this.phase() !== 'idle') return;
    const isCorrect = side === this.winner();
    const delta = isCorrect ? 1500 + this.streak() * 250 : 0;

    this.phase.set(isCorrect ? 'correct' : 'wrong');
    this.feedback.set({ isCorrect, delta });

    if (isCorrect) {
      this.streak.update(s => s + 1);
      this.score.update(s => s + delta);
    } else {
      this.streak.set(0);
      this.lives.update(l => Math.max(0, l - 1));
    }
  }

  next(): void {
    this.phase.set('idle');
    this.feedback.set(null);
    this.qIdx.update(i => i + 1);
  }

  heartClass(i: number): string {
    const l = this.lives();
    if (i >= l)   return 'vs-lifebar__heart vs-lifebar__heart--lost';
    if (l === 1)  return 'vs-lifebar__heart vs-lifebar__heart--low';
    if (l === 2)  return 'vs-lifebar__heart vs-lifebar__heart--mid';
    return 'vs-lifebar__heart';
  }
}
