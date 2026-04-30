import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { GameMode, PlayerStats } from '../models/game.models';

@Injectable({ providedIn: 'root' })
export class StatsService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  mine(): Observable<PlayerStats[]> {
    return this.http.get<PlayerStats[]>(`${this.base}/stats/me`);
  }

  mineByMode(mode: GameMode): Observable<PlayerStats> {
    const params = new HttpParams().set('mode', mode);
    return this.http.get<PlayerStats>(`${this.base}/stats/me`, { params });
  }
}
