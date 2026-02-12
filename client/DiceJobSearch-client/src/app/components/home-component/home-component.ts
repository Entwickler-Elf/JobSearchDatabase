import {Component, inject, OnInit, signal} from '@angular/core';
import {JobListing, JobSearchService} from "../../service/job-search.service";
import {DatePipe} from '@angular/common';
import {SearchStatusComponent} from '../search-status-component/search-status-component';

@Component({
  selector: 'app-home-component',
  imports: [
    DatePipe,
    SearchStatusComponent
  ],
  templateUrl: './home-component.html',
  styleUrl: './home-component.css',
})
export class HomeComponent implements OnInit {
  private jobSearchService = inject(JobSearchService);

  protected jobs = signal<JobListing[]>([]);
  protected isLoading = signal(false);

  ngOnInit() {
    this.onHomeClick();
  }

  onHomeClick() {
    this.isLoading.set(true);
    this.jobSearchService.getJobs().subscribe({
      next: (response) => {
        this.jobs.set(response.results);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error fetching jobs', err);
        this.isLoading.set(false);
      }
    });
  }
}
