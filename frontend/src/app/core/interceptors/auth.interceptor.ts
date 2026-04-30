import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

const PUBLIC_PATHS = ['/auth/login', '/auth/register', '/auth/refresh'];

function isApiRequest(url: string): boolean {
  return url.startsWith(environment.apiBaseUrl);
}

function isPublic(url: string): boolean {
  return PUBLIC_PATHS.some((p) => url.includes(p));
}

function withToken(req: HttpRequest<unknown>, token: string | null): HttpRequest<unknown> {
  if (!token) return req;
  return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!isApiRequest(req.url) || isPublic(req.url)) {
    return next(req);
  }

  const accessToken = auth.getAccessToken();
  return next(withToken(req, accessToken)).pipe(
    catchError((err) => {
      if (err?.status !== 401 || !auth.getRefreshToken()) {
        return throwError(() => err);
      }
      return auth.refresh().pipe(
        switchMap((res) => next(withToken(req, res.accessToken))),
        catchError((refreshErr) => {
          auth.clear();
          router.navigate(['/login']);
          return throwError(() => refreshErr);
        })
      );
    })
  );
};
