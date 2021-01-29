import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor(
        private storage: Storage
    ) { }

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        let token = this.storage.getItem('Authorization')
        if (!token) {
            return next.handle(req);
        }
        let headers = req.headers;
        headers = headers.append('Authorization', `Bearer ${token}`);
        return next.handle(req.clone({headers: headers}));
    }
}