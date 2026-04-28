import { Component } from '@angular/core';
import { TopbarComponent } from '../../../../shared/components/layout/topbar/topbar';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [TopbarComponent],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile {
  modeStats = [
    { mode: 'Supervivencia',  games: 120, wins: 78,  acc: '72%', best: 14 },
    { mode: 'Precisión',       games: 64,  wins: 31,  acc: '49%', best: 8  },
    { mode: 'Duelo binario',   games: 38,  wins: 22,  acc: '58%', best: 11 },
    { mode: 'Sabotaje',        games: 25,  wins: 14,  acc: '56%', best: 7  },
  ];

  categories = [
    { cat: 'Fútbol',     pct: 78 },
    { cat: 'Música',     pct: 65 },
    { cat: 'Cine',       pct: 52 },
    { cat: 'Geografía',  pct: 41 },
    { cat: 'Política',   pct: 28 },
  ];

  achievements = [
    { t: 'Sin piedad',      s: 'Racha de 10 sin fallar',        ico: '☠', c: 'red'    },
    { t: 'Numerólogo',      s: 'Desviación < 1% en Precisión',  ico: '◎', c: 'blue'   },
    { t: 'Sabotaje brutal', s: 'Eliminar a alguien en <30s',    ico: '⚡', c: 'purple' },
    { t: 'Top 500',         s: 'Alcanzar el ranking',           ico: '★', c: 'gold'   },
  ];

  history = [
    { mode: 'SUPERVIVENCIA', win: true,  streak: 14, pts: '+18 250', when: 'hace 12 min' },
    { mode: 'PRECISIÓN',     win: false, streak: 3,  pts: '+2 400',  when: 'hace 1 h'    },
    { mode: 'SABOTAJE',      win: true,  streak: 9,  pts: '+11 800', when: 'ayer'         },
    { mode: 'SUPERVIVENCIA', win: false, streak: 6,  pts: '+5 100',  when: 'ayer'         },
    { mode: 'DUELO BINARIO', win: true,  streak: 8,  pts: '+9 600',  when: 'hace 2 d'    },
  ];
}
