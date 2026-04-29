import { Component } from '@angular/core';
import { AdminSidebarComponent } from '../../components/sidebar/sidebar';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [AdminSidebarComponent],
  templateUrl: './admin-users.html',
  styleUrl: '../dashboard/admin-dashboard.scss',
})
export class AdminUsers {
  users = [
    { name: 'kil4_max',   email: 'kil4@mail.com',      role: 'jugador',   state: 'ok',     joined: 'mar 26', games: 412, level: 22  },
    { name: 'aritzz92',   email: 'aritzz92@mail.com',  role: 'jugador',   state: 'ok',     joined: 'mar 26', games: 247, level: 14  },
    { name: 'numerito',   email: 'numero@otro.com',    role: 'moderador', state: 'ok',     joined: 'feb 26', games: 188, level: 18  },
    { name: 'tucanela',   email: 'tuca@aqui.com',      role: 'jugador',   state: 'warn',   joined: 'abr 26', games: 95,  level: 8   },
    { name: 'soyrobot',   email: 'rob@bot.io',         role: 'jugador',   state: 'banned', joined: 'abr 26', games: 12,  level: 1   },
    { name: 'admin_root', email: 'admin@versus.io',    role: 'admin',     state: 'ok',     joined: 'ene 26', games: 0,   level: '—' },
  ];

  roleColor(r: string): string {
    return { admin: 'var(--vs-accent-red)', moderador: 'var(--vs-accent-gold)' }[r] ?? 'var(--vs-accent-blue)';
  }

  roleBg(r: string): string {
    return { admin: 'rgba(230,57,70,0.12)', moderador: 'rgba(244,197,66,0.12)' }[r] ?? 'rgba(67,97,238,0.12)';
  }

  initials(name: string): string { return name.slice(0, 2).toUpperCase(); }
}
