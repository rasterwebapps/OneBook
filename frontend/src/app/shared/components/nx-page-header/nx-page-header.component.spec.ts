import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component } from '@angular/core';
import { NxPageHeaderComponent } from './nx-page-header.component';

@Component({
  standalone: true,
  imports: [NxPageHeaderComponent],
  template: `
    <nx-page-header [title]="title" [subtitle]="subtitle">
      <button class="nx-btn nx-btn--emerald">Action</button>
    </nx-page-header>
  `,
})
class TestHostComponent {
  title = 'Test Title';
  subtitle = 'Test subtitle description';
}

describe('NxPageHeaderComponent', () => {
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

  it('should display the title', () => {
    const el = fixture.nativeElement.querySelector('.nx-page-header__title');
    expect(el.textContent).toContain('Test Title');
  });

  it('should display the subtitle', () => {
    const el = fixture.nativeElement.querySelector('.nx-page-header__subtitle');
    expect(el.textContent).toContain('Test subtitle description');
  });

  it('should project action buttons', () => {
    const btn = fixture.nativeElement.querySelector('.nx-page-header__actions .nx-btn');
    expect(btn).toBeTruthy();
    expect(btn.textContent).toContain('Action');
  });

  it('should hide subtitle when empty', () => {
    host.subtitle = '';
    fixture.detectChanges();
    const el = fixture.nativeElement.querySelector('.nx-page-header__subtitle');
    expect(el).toBeFalsy();
  });
});
