import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Component, signal } from '@angular/core';
import { NxSearchInputComponent } from './nx-search-input.component';

@Component({
  standalone: true,
  imports: [NxSearchInputComponent],
  template: `
    <nx-search-input
      [placeholder]="placeholder"
      [value]="value"
      [debounceMs]="debounceMs"
      (searchChange)="onSearch($event)"
    />
  `,
})
class TestHostComponent {
  placeholder = 'Search vouchers…';
  value = '';
  debounceMs = 100;
  lastSearch = '';

  onSearch(val: string): void {
    this.lastSearch = val;
  }
}

describe('NxSearchInputComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(host).toBeTruthy();
  });

  it('should display the placeholder', () => {
    const input = fixture.nativeElement.querySelector('.nx-search-input__field');
    expect(input.placeholder).toBe('Search vouchers…');
  });

  it('should debounce input events', fakeAsync(() => {
    const input = fixture.nativeElement.querySelector('.nx-search-input__field') as HTMLInputElement;
    input.value = 'test';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    // Before debounce
    expect(host.lastSearch).toBe('');

    // After debounce
    tick(150);
    expect(host.lastSearch).toBe('test');
  }));

  it('should emit empty string on clear', fakeAsync(() => {
    // First type something
    const input = fixture.nativeElement.querySelector('.nx-search-input__field') as HTMLInputElement;
    input.value = 'search term';
    input.dispatchEvent(new Event('input'));
    tick(150);
    fixture.detectChanges();

    // Now set value to make clear button appear
    host.value = 'search term';
    fixture.detectChanges();

    const clearBtn = fixture.nativeElement.querySelector('.nx-search-input__clear');
    if (clearBtn) {
      clearBtn.click();
      fixture.detectChanges();
      expect(host.lastSearch).toBe('');
    }
  }));

  it('should show clear button when value is set', () => {
    host.value = 'some text';
    fixture.detectChanges();
    const clearBtn = fixture.nativeElement.querySelector('.nx-search-input__clear');
    expect(clearBtn).toBeTruthy();
  });

  it('should not show clear button when value is empty', () => {
    host.value = '';
    fixture.detectChanges();
    const clearBtn = fixture.nativeElement.querySelector('.nx-search-input__clear');
    expect(clearBtn).toBeFalsy();
  });
});
