import { Component, OnInit, OnDestroy, Inject, ChangeDetectorRef } from '@angular/core';
import { TwigsService, TWIGS_SERVICE } from '../../shared/twigs.service';
import { User } from '../user';
import { AppComponent } from 'src/client/app/app.component';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  public isLoading = false;
  public email: string;
  public password: string;
  private redirect: string;

  constructor(
    private app: AppComponent,
    @Inject(TWIGS_SERVICE) private twigsService: TwigsService,
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) { }

  ngOnInit() {
    this.app.setTitle('Login')
    this.app.setBackEnabled(true);
    this.redirect = this.activatedRoute.snapshot.queryParamMap.get('redirect');
  }

  login(): void {
    this.isLoading = true;
    this.twigsService.login(this.email, this.password)
      .subscribe(user => {
        this.app.user.next(user);
        this.router.navigate([this.redirect || '/'])
      },
      error => {
        console.error(error)
        //TODO: Replace this with an in-app dialog
        alert("Login failed. Please verify you have the correct credentials");
        this.isLoading = false;
      })
  }
}
