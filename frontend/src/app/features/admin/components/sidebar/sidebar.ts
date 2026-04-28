import { Component, input } from '@angular/core';

export type AdminNavKey = 'dash' | 'spiders' | 'reports' | 'users' | 'quest' | 'rank' | 'cfg' | 'logs';

@Component({
  selector: 'app-admin-sidebar',
  standalone: true,
  templateUrl: './sidebar.html',
})
export class AdminSidebarComponent {
  active = input<AdminNavKey>('dash');

  sections = [
    { label: 'SUPERVISIÓN', items: [
      { key: 'dash',    label: 'Resumen'    },
      { key: 'spiders', label: 'Spiders'    },
      { key: 'reports', label: 'Moderación' },
    ]},
    { label: 'GESTIÓN', items: [
      { key: 'users', label: 'Usuarios'   },
      { key: 'quest', label: 'Preguntas'  },
      { key: 'rank',  label: 'Rankings'   },
    ]},
    { label: 'SISTEMA', items: [
      { key: 'cfg',  label: 'Configuración' },
      { key: 'logs', label: 'Logs'          },
    ]},
  ];
}
