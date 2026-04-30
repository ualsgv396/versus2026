import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserMe } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  me(): Observable<UserMe> {
    return this.http.get<UserMe>(`${this.base}/users/me`);
  }

  updateMe(payload: { username?: string; avatarUrl?: string }): Observable<UserMe> {
    return this.http.put<UserMe>(`${this.base}/users/me`, payload);
  }
}
