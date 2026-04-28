import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TopbarComponent } from '../../../../shared/components/layout/topbar/topbar';

@Component({
  selector: 'app-result',
  standalone: true,
  imports: [RouterLink, TopbarComponent],
  templateUrl: './result.html',
  styleUrl: './result.scss',
})
export class Result {
  isWin = true;

  stats = [
    { num: '14',     label: 'Racha máxima', accent: 'var(--vs-accent-gold)' },
    { num: '18 250', label: 'Puntos',        accent: 'var(--vs-accent-green)' },
    { num: '12/14',  label: 'Aciertos' },
    { num: '+ 320',  label: 'XP',            accent: 'var(--vs-accent-blue)' },
  ];

  breakdown = [
    { cat: 'Fútbol',    hits: 5, miss: 0 },
    { cat: 'Música',    hits: 4, miss: 1 },
    { cat: 'Cine',      hits: 3, miss: 1 },
    { cat: 'Política',  hits: 0, miss: 0 },
  ];

  pct(hits: number, miss: number): number {
    const total = hits + miss;
    return total ? Math.round((hits / total) * 100) : 0;
  }
}
