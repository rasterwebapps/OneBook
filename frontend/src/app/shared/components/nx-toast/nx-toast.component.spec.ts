import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NxToastComponent, NxToastService } from './nx-toast.component';

describe('NxToastService', () => {
  let service: NxToastService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NxToastService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should add a toast on show()', () => {
    service.show('Test message', 'info', 0);
    expect(service.toasts().length).toBe(1);
    expect(service.toasts()[0].message).toBe('Test message');
    expect(service.toasts()[0].type).toBe('info');
  });

  it('should add success toast', () => {
    service.success('Success!', 0);
    expect(service.toasts()[0].type).toBe('success');
  });

  it('should add error toast', () => {
    service.error('Error!', 0);
    expect(service.toasts()[0].type).toBe('error');
  });

  it('should add warning toast', () => {
    service.warning('Warning!', 0);
    expect(service.toasts()[0].type).toBe('warning');
  });

  it('should dismiss a toast by id', () => {
    service.show('First', 'info', 0);
    service.show('Second', 'info', 0);
    expect(service.toasts().length).toBe(2);

    const firstId = service.toasts()[0].id;
    service.dismiss(firstId);
    expect(service.toasts().length).toBe(1);
    expect(service.toasts()[0].message).toBe('Second');
  });

  it('should auto-dismiss after duration', (done) => {
    service.show('Auto dismiss', 'info', 100);
    expect(service.toasts().length).toBe(1);

    setTimeout(() => {
      expect(service.toasts().length).toBe(0);
      done();
    }, 200);
  });
});

describe('NxToastComponent', () => {
  let fixture: ComponentFixture<NxToastComponent>;
  let service: NxToastService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NxToastComponent],
    }).compileComponents();

    service = TestBed.inject(NxToastService);
    fixture = TestBed.createComponent(NxToastComponent);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render toasts from service', () => {
    service.show('Test toast', 'success', 0);
    fixture.detectChanges();

    const toasts = fixture.nativeElement.querySelectorAll('.nx-toast');
    expect(toasts.length).toBe(1);
    expect(toasts[0].textContent).toContain('Test toast');
  });

  it('should apply correct type class', () => {
    service.error('Error toast', 0);
    fixture.detectChanges();

    const toast = fixture.nativeElement.querySelector('.nx-toast');
    expect(toast.classList).toContain('nx-toast--error');
  });

  it('should dismiss when close button is clicked', () => {
    service.show('Dismissable', 'info', 0);
    fixture.detectChanges();

    const closeBtn = fixture.nativeElement.querySelector('.nx-toast__close');
    closeBtn.click();
    fixture.detectChanges();

    const toasts = fixture.nativeElement.querySelectorAll('.nx-toast');
    expect(toasts.length).toBe(0);
  });

  it('should have aria-live for accessibility', () => {
    const container = fixture.nativeElement.querySelector('.nx-toast-container');
    expect(container.getAttribute('aria-live')).toBe('polite');
  });
});
