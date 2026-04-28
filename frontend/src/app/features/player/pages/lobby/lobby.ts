import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TopbarComponent } from '../../../../shared/components/layout/topbar/topbar';

@Component({
  selector: 'app-lobby',
  standalone: true,
  imports: [RouterLink, TopbarComponent],
  templateUrl: './lobby.html',
  styleUrl: './lobby.scss',
})
export class Lobby {}
