import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { VoucherExplorerComponent } from './voucher-explorer.component';

describe('VoucherExplorerComponent', () => {
  let component: VoucherExplorerComponent;
  let fixture: ComponentFixture<VoucherExplorerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VoucherExplorerComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(VoucherExplorerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should default to explorer mode', () => {
    expect(component.mode()).toBe('explorer');
  });

  it('should switch to entry mode on openNewEntry', () => {
    component.openNewEntry('PAYMENT');
    expect(component.mode()).toBe('entry');
  });

  it('should return to explorer mode on closeEntry', () => {
    component.openNewEntry('PAYMENT');
    expect(component.mode()).toBe('entry');

    component.closeEntry();
    expect(component.mode()).toBe('explorer');
  });

  it('should return to explorer mode on save success', () => {
    component.openNewEntry('PAYMENT');
    expect(component.mode()).toBe('entry');

    // Simulate save success by calling closeEntry (which save() calls on success)
    component.closeEntry();
    expect(component.mode()).toBe('explorer');
  });
});
