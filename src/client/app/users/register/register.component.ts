import { Component, OnInit, OnDestroy, Inject } from '@angular/core';
import { TwigsService, TWIGS_SERVICE } from '../../shared/twigs.service';
import { AppComponent } from 'src/client/app/app.component';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  public username: string;
  public email: string;
  public password: string;
  public confirmedPassword: string;
  public isLoading = false;

  constructor(
    private app: AppComponent,
    @Inject(TWIGS_SERVICE) private twigsService: TwigsService,
    private router: Router,
  ) { }

  ngOnInit() {
    this.app.setTitle('Register')
    this.app.setBackEnabled(true);
  }

  register(): void {
    if (this.password !== this.confirmedPassword) {
      alert('Passwords don\'t match');
      return;
    }
    this.isLoading = true;
    this.twigsService.register(this.username, this.email, this.password).subscribe(user => {
      console.log(user);
      this.router.navigate(['/'])
    }, error => {
      console.error(error);
      alert("Registration failed!")
      this.isLoading = false;
    })
  }
}
