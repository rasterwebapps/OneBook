import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { StartComponent } from './start.component';
import { AuthService } from '../../services/auth.service';
import { signal } from '@angular/core';

describe('StartComponent', () => {
  let component: StartComponent;
  let fixture: ComponentFixture<StartComponent>;
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let routerMock: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceMock = jasmine.createSpyObj('AuthService', ['login', 'isAuthenticated'], {
      isAuthenticated: signal(false)
    });
    
    routerMock = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [StartComponent],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(StartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have four features', () => {
    expect(component.features.length).toBe(4);
  });

  it('should have Zero-Trust Security as first feature', () => {
    expect(component.features[0].title).toBe('Zero-Trust Security');
  });

  it('should call authService.login when onLogin is called', () => {
    component.onLogin();
    expect(authServiceMock.login).toHaveBeenCalled();
  });

  it('should set isLoading to true when onLogin is called', () => {
    expect(component.isLoading()).toBeFalse();
    component.onLogin();
    expect(component.isLoading()).toBeTrue();
  });

  it('should redirect to dashboard if already authenticated', () => {
    // Create a new mock that returns true for isAuthenticated
    const authenticatedMock = jasmine.createSpyObj('AuthService', ['login'], {
      isAuthenticated: signal(true)
    });

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [StartComponent],
      providers: [
        { provide: AuthService, useValue: authenticatedMock },
        { provide: Router, useValue: routerMock }
      ]
    });

    const newFixture = TestBed.createComponent(StartComponent);
    newFixture.detectChanges();
    
    expect(routerMock.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should display login button with correct text', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const loginBtn = compiled.querySelector('.login-btn');
    expect(loginBtn?.textContent).toContain('Login');
    expect(loginBtn?.textContent).toContain('உள்நுழை');
  });

  it('should display brand title', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const brandTitle = compiled.querySelector('.brand-title');
    expect(brandTitle?.textContent).toContain('OneBook');
  });
});
