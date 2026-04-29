import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TopbarComponent } from '../../../../shared/components/layout/topbar/topbar';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, TopbarComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  stats = [
    { num: '247',  label: 'Partidas' },
    { num: '68%',  label: 'Aciertos',       accent: 'var(--vs-accent-green)' },
    { num: '14',   label: 'Mejor racha',    accent: 'var(--vs-accent-gold)' },
    { num: '#312', label: 'Ranking global', accent: 'var(--vs-accent-blue)' },
  ];

  modes = [
    { name: 'Supervivencia', color: 'var(--vs-accent-red)',    desc: '3 vidas. Cero piedad.', badge: 'Más jugado' },
    { name: 'Precisión',     color: 'var(--vs-accent-blue)',   desc: 'Adivina el número' },
    { name: 'Duelo binario', color: 'var(--vs-accent-gold)',   desc: '2 jugadores' },
    { name: 'Sabotaje',      color: 'var(--vs-accent-purple)', desc: 'Quítale vida al rival' },
  ];

  activity = [
    { mode: 'SUPERVIVENCIA', modeClass: 'vs-pill--err',  result: 'VICTORIA', resultColor: 'var(--vs-accent-green)', streak: 14, pts: '+18 250', when: 'hace 12 min' },
    { mode: 'PRECISIÓN',     modeClass: 'vs-pill--info', result: 'DERROTA',  resultColor: 'var(--vs-accent-red)',   streak: 3,  pts: '+2 400',  when: 'hace 1 h' },
    { mode: 'SABOTAJE',      modeClass: '',              result: 'VICTORIA', resultColor: 'var(--vs-accent-green)', streak: 9,  pts: '+11 800', when: 'ayer' },
    { mode: 'SUPERVIVENCIA', modeClass: 'vs-pill--err',  result: 'DERROTA',  resultColor: 'var(--vs-accent-red)',   streak: 6,  pts: '+5 100',  when: 'ayer' },
  ];

  rankings = [
    { pos: 1, name: 'kil4_max', score: 89400,  you: false },
    { pos: 2, name: 'numerito', score: 71200,  you: false },
    { pos: 3, name: 'tucanela', score: 62900,  you: false },
    { pos: 4, name: 'aritzz92', score: 58120,  you: true  },
    { pos: 5, name: 'soyrobot', score: 54300,  you: false },
  ];

  formatScore(n: number): string {
    return n.toLocaleString('es-ES');
  }
}
