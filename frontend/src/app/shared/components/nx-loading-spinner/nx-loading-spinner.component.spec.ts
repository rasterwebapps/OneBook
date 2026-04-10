import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NxLoadingSpinnerComponent } from './nx-loading-spinner.component';

describe('NxLoadingSpinnerComponent', () => {
  let fixture: ComponentFixture<NxLoadingSpinnerComponent>;
  let component: NxLoadingSpinnerComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NxLoadingSpinnerComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(NxLoadingSpinnerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have role="status" for accessibility', () => {
    const el = fixture.nativeElement.querySelector('.nx-loading-spinner');
    expect(el.getAttribute('role')).toBe('status');
  });

  it('should contain an sr-only element for screen readers', () => {
    const srOnly = fixture.nativeElement.querySelector('.sr-only');
    expect(srOnly).toBeTruthy();
    expect(srOnly.textContent).toContain('Loading');
  });

  it('should display label when provided', () => {
    fixture.componentRef.setInput('label', 'Loading data…');
    fixture.detectChanges();
    const label = fixture.nativeElement.querySelector('.nx-loading-spinner__label');
    expect(label).toBeTruthy();
    expect(label.textContent).toContain('Loading data…');
  });

  it('should hide label when empty', () => {
    const label = fixture.nativeElement.querySelector('.nx-loading-spinner__label');
    expect(label).toBeFalsy();
  });

  it('should have a spinning ring element', () => {
    const ring = fixture.nativeElement.querySelector('.nx-loading-spinner__ring');
    expect(ring).toBeTruthy();
  });
});
