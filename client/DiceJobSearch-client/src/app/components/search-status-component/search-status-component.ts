import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import {SearchUpdatesService} from '../../service/search-updates-service';

@Component({
  selector: 'app-search-status',
  templateUrl: './search-status-component.html',
  styleUrl: './search-status-component.css',
})
export class SearchStatusComponent implements OnInit, OnDestroy {
  last: any;
  private sub?: Subscription;

  constructor(private updates: SearchUpdatesService) {}

  ngOnInit(): void {
    this.sub = this.updates.connect().subscribe({
      next: ({ data }) => (this.last = data),
      error: () => {
        // optionally reconnect with backoff
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
}
