import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  PrecisionAnswerRequest,
  PrecisionAnswerResponse,
  StartGameResponse,
  SurvivalAnswerRequest,
  SurvivalAnswerResponse,
} from '../models/game.models';

@Injectable({ providedIn: 'root' })
export class GameService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  startSurvival(): Observable<StartGameResponse> {
    return this.http.post<StartGameResponse>(`${this.base}/game/survival/start`, {});
  }

  answerSurvival(req: SurvivalAnswerRequest): Observable<SurvivalAnswerResponse> {
    return this.http.post<SurvivalAnswerResponse>(`${this.base}/game/survival/answer`, req);
  }

  startPrecision(): Observable<StartGameResponse> {
    return this.http.post<StartGameResponse>(`${this.base}/game/precision/start`, {});
  }

  answerPrecision(req: PrecisionAnswerRequest): Observable<PrecisionAnswerResponse> {
    return this.http.post<PrecisionAnswerResponse>(`${this.base}/game/precision/answer`, req);
  }
}
