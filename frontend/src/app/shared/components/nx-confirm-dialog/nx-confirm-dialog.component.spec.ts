import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NxConfirmDialogComponent, NxConfirmDialogService } from './nx-confirm-dialog.component';

describe('NxConfirmDialogService', () => {
  let service: NxConfirmDialogService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NxConfirmDialogService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should not be visible initially', () => {
    expect(service.visible()).toBe(false);
  });

  it('should become visible on confirm()', () => {
    service.confirm({ title: 'Delete', message: 'Are you sure?' });
    expect(service.visible()).toBe(true);
    expect(service.config().title).toBe('Delete');

    // Clean up
    service.resolve(false);
  });

  it('should resolve true on confirm', async () => {
    const promise = service.confirm({ title: 'Test', message: 'Confirm?' });
    service.resolve(true);
    const result = await promise;
    expect(result).toBe(true);
    expect(service.visible()).toBe(false);
  });

  it('should resolve false on cancel', async () => {
    const promise = service.confirm({ title: 'Test', message: 'Cancel?' });
    service.resolve(false);
    const result = await promise;
    expect(result).toBe(false);
  });
});

describe('NxConfirmDialogComponent', () => {
  let fixture: ComponentFixture<NxConfirmDialogComponent>;
  let service: NxConfirmDialogService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NxConfirmDialogComponent],
    }).compileComponents();

    service = TestBed.inject(NxConfirmDialogService);
    fixture = TestBed.createComponent(NxConfirmDialogComponent);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should not show dialog initially', () => {
    const overlay = fixture.nativeElement.querySelector('.nx-confirm-overlay');
    expect(overlay).toBeFalsy();
  });

  it('should show dialog when service is visible', () => {
    service.confirm({ title: 'Delete Item', message: 'This action cannot be undone.' });
    fixture.detectChanges();

    const overlay = fixture.nativeElement.querySelector('.nx-confirm-overlay');
    expect(overlay).toBeTruthy();

    const title = fixture.nativeElement.querySelector('.nx-confirm-dialog__title');
    expect(title.textContent).toContain('Delete Item');

    const msg = fixture.nativeElement.querySelector('.nx-confirm-dialog__message');
    expect(msg.textContent).toContain('This action cannot be undone.');

    // Clean up
    service.resolve(false);
  });

  it('should have role="dialog" and aria-modal', () => {
    service.confirm({ title: 'Test', message: 'Test' });
    fixture.detectChanges();

    const overlay = fixture.nativeElement.querySelector('.nx-confirm-overlay');
    expect(overlay.getAttribute('role')).toBe('dialog');
    expect(overlay.getAttribute('aria-modal')).toBe('true');

    service.resolve(false);
  });

  it('should call resolve(true) on confirm click', async () => {
    const promise = service.confirm({
      title: 'Confirm',
      message: 'Sure?',
      confirmLabel: 'Yes, Delete',
    });
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('.nx-confirm-dialog__actions .nx-btn');
    // Second button is the confirm button
    buttons[1].click();
    fixture.detectChanges();

    const result = await promise;
    expect(result).toBe(true);
  });

  it('should call resolve(false) on cancel click', async () => {
    const promise = service.confirm({
      title: 'Cancel',
      message: 'Cancel this?',
      cancelLabel: 'No',
    });
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('.nx-confirm-dialog__actions .nx-btn');
    // First button is the cancel button
    buttons[0].click();
    fixture.detectChanges();

    const result = await promise;
    expect(result).toBe(false);
  });
});
