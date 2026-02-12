import { TestBed } from '@angular/core/testing';

import { SearchUpdatesService } from './search-updates-service';

describe('SearchUpdatesService', () => {
  let service: SearchUpdatesService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SearchUpdatesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
