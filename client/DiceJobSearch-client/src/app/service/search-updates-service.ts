import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';

export interface SearchUpdatePayload {
  status: 'COMPLETED' | 'FAILED';
  query: string;
  newJobsCount: number;
  totalJobsCount: number;
  elapsedMs: number;
  error: string | null;
  timestamp: string;
}

@Injectable({ providedIn: 'root' })
export class SearchUpdatesService {
  constructor(private zone: NgZone) {}

  connect(): Observable<{ event: string; data: SearchUpdatePayload }> {
    return new Observable(sub => {
      const es = new EventSource('/api/search/updates');

      es.addEventListener('search-completed', (msg: MessageEvent) => {
        this.zone.run(() => sub.next({ event: 'search-completed', data: JSON.parse(msg.data) }));
      });

      es.addEventListener('search-failed', (msg: MessageEvent) => {
        this.zone.run(() => sub.next({ event: 'search-failed', data: JSON.parse(msg.data) }));
      });

      es.onerror = (err) => {
        this.zone.run(() => sub.error(err));
        es.close();
      };

      return () => es.close();
    });
  }
}
