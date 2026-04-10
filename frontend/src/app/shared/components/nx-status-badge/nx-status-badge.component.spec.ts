import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NxStatusBadgeComponent } from './nx-status-badge.component';

describe('NxStatusBadgeComponent', () => {
  let fixture: ComponentFixture<NxStatusBadgeComponent>;
  let component: NxStatusBadgeComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NxStatusBadgeComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(NxStatusBadgeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should map POSTED to success variant', () => {
    fixture.componentRef.setInput('status', 'POSTED');
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.nx-badge');
    expect(badge.classList).toContain('nx-badge--success');
    expect(badge.textContent).toContain('Posted');
  });

  it('should map PENDING to warning variant', () => {
    fixture.componentRef.setInput('status', 'PENDING');
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.nx-badge');
    expect(badge.classList).toContain('nx-badge--warning');
    expect(badge.textContent).toContain('Pending');
  });

  it('should map DRAFT to info variant', () => {
    fixture.componentRef.setInput('status', 'DRAFT');
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.nx-badge');
    expect(badge.classList).toContain('nx-badge--info');
    expect(badge.textContent).toContain('Draft');
  });

  it('should map CANCELLED to danger variant', () => {
    fixture.componentRef.setInput('status', 'CANCELLED');
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.nx-badge');
    expect(badge.classList).toContain('nx-badge--danger');
    expect(badge.textContent).toContain('Cancelled');
  });

  it('should map REJECTED to danger variant', () => {
    fixture.componentRef.setInput('status', 'REJECTED');
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.nx-badge');
    expect(badge.classList).toContain('nx-badge--danger');
    expect(badge.textContent).toContain('Rejected');
  });

  it('should map APPROVED to success variant', () => {
    fixture.componentRef.setInput('status', 'APPROVED');
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.nx-badge');
    expect(badge.classList).toContain('nx-badge--success');
    expect(badge.textContent).toContain('Approved');
  });

  it('should use custom label when provided', () => {
    fixture.componentRef.setInput('status', 'POSTED');
    fixture.componentRef.setInput('label', 'Custom Label');
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.nx-badge');
    expect(badge.textContent).toContain('Custom Label');
  });

  it('should handle case-insensitive status', () => {
    fixture.componentRef.setInput('status', 'posted');
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.nx-badge');
    expect(badge.classList).toContain('nx-badge--success');
  });

  it('should fall back to neutral for unknown status', () => {
    fixture.componentRef.setInput('status', 'UNKNOWN_STATUS');
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.nx-badge');
    expect(badge.classList).toContain('nx-badge--neutral');
    expect(badge.textContent).toContain('UNKNOWN_STATUS');
  });
});
