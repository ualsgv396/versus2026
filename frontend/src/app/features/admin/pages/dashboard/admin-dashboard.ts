import { Component } from '@angular/core';
import { AdminSidebarComponent } from '../../components/sidebar/sidebar';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [AdminSidebarComponent],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss',
})
export class AdminDashboard {
  kpis = [
    { label: 'Usuarios activos',    num: '14 287', delta: '▲ 6.4% vs. semana ant.', up: true,  color: 'var(--vs-accent-green)',  spark: [10,12,11,14,13,17,19,18,22,24] },
    { label: 'Partidas hoy',        num: '3 142',  delta: '▲ 2.1%',                 up: true,  color: 'var(--vs-accent-blue)',   spark: [40,38,52,49,61,58,73] },
    { label: 'Preguntas en BD',     num: '98 442', delta: '+ 1 230 esta semana',     up: true,  color: 'var(--vs-accent-gold)',   spark: [20,22,24,26,28,30,33] },
    { label: 'Reportes pendientes', num: '27',     delta: '▲ 9 desde ayer',          up: false, color: 'var(--vs-accent-red)',    spark: [3,5,4,8,12,18,27] },
  ];

  spiders = [
    { name: 'spider_transfermarkt', stat: 'ok',   last: 'hace 14 min', q: '1 240' },
    { name: 'spider_spotify_charts', stat: 'ok',  last: 'hace 28 min', q: '845'   },
    { name: 'spider_box_office',    stat: 'warn', last: 'hace 2 h',    q: '320'   },
    { name: 'spider_instagram',     stat: 'err',  last: 'hace 6 h',    q: '0'     },
  ];

  modes = [
    { mode: 'Supervivencia',    pct: 42, color: 'var(--vs-accent-red)'    },
    { mode: 'Precisión',        pct: 24, color: 'var(--vs-accent-blue)'   },
    { mode: 'Duelo binario',    pct: 16, color: 'var(--vs-accent-gold)'   },
    { mode: 'Sabotaje',         pct: 12, color: 'var(--vs-accent-purple)' },
    { mode: 'Duelo precisión',  pct: 6,  color: 'var(--vs-accent-green)'  },
  ];

  logs = [
    { ts: '10:31:22', cls: 'ok',   msg: 'spider_transfermarkt insertó 1 240 preguntas' },
    { ts: '10:18:04', cls: 'warn', msg: 'spider_box_office latencia > 8s en 12 requests' },
    { ts: '10:02:51', cls: 'ok',   msg: 'usuario @kil4_max alcanzó nivel 30' },
    { ts: '09:54:09', cls: 'err',  msg: 'spider_instagram timeout · auth-required' },
    { ts: '09:41:18', cls: 'ok',   msg: '27 preguntas reportadas en cola moderación' },
    { ts: '09:18:02', cls: 'ok',   msg: 'backup nocturno completado · 2.4 GB' },
  ];

  pillClass(stat: string): string {
    return { ok: 'vs-pill--ok', warn: 'vs-pill--warn', err: 'vs-pill--err' }[stat] ?? 'vs-pill--mute';
  }

  pillLabel(stat: string): string {
    return { ok: 'OK', warn: 'LENTA', err: 'CAÍDA' }[stat] ?? stat;
  }

  sparkPoints(data: number[]): string {
    const max = Math.max(...data), min = Math.min(...data), range = max - min || 1;
    return data.map((v, i) =>
      `${(i / (data.length - 1)) * 80},${30 - ((v - min) / range) * 26 - 2}`
    ).join(' ');
  }
}
