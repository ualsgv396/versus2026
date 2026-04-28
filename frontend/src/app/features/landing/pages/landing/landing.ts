import { AfterViewInit, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

const DEMOS = [
  { q: '¿Quién tiene MÁS oyentes en Spotify?', a: 'Bad Bunny', av: '79.5M', b: 'Taylor Swift', bv: '102.3M', winner: 'b' },
  { q: '¿Qué película recaudó MÁS?', a: 'Avatar', av: '2 923M $', b: 'Endgame', bv: '2 799M $', winner: 'a' },
  { q: '¿Quién tiene MÁS seguidores en Instagram?', a: 'Cristiano', av: '638M', b: 'Messi', bv: '503M', winner: 'a' },
];

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './landing.html',
  styleUrl: './landing.scss',
})
export class Landing implements AfterViewInit {
  private destroyRef = inject(DestroyRef);

  demoIdx = signal(0);
  demo = computed(() => DEMOS[this.demoIdx()]);

  activeSection = signal<string>('inicio');

  private observer?: IntersectionObserver;

  ngAfterViewInit(): void {
    const sections = ['inicio', 'modos', 'como-funciona', 'datos'];
    this.observer = new IntersectionObserver(
      (entries) => {
        const visible = entries.find(e => e.isIntersecting);
        if (visible) this.activeSection.set(visible.target.id);
      },
      { rootMargin: '-40% 0px -55% 0px', threshold: 0 }
    );
    sections.forEach(id => {
      const el = document.getElementById(id);
      if (el) this.observer!.observe(el);
    });
    this.destroyRef.onDestroy(() => this.observer?.disconnect());
  }

  readonly steps = [
    { n: '01', t: 'Te lanzamos dos opciones', d: 'O un número que adivinar. Datos reales sacados de la web — seguidores, recaudaciones, récords.' },
    { n: '02', t: 'Eliges. Rápido.', d: 'Cuanto más aciertes seguidos, más puntos. Cada fallo te quita una vida.' },
    { n: '03', t: 'Aguantas o caes', d: 'Tres vidas en solo. Cero en duelo. Spoiler: casi siempre cae el que se confía.' },
  ];

  readonly modes = [
    { ico: '☠', n: 'SUPERVIVENCIA',      d: 'Binario. 3 vidas. Hasta que falles.',                              tag: '1J', c: 'red',    big: true },
    { ico: '◎', n: 'PRECISIÓN',           d: 'Adivina el número. Cerca = vida. Lejos = adiós.',                  tag: '1J', c: 'blue' },
    { ico: '⚔', n: 'DUELO BINARIO',       d: 'Cara a cara. El último en pie gana.',                             tag: '2J', c: 'gold' },
    { ico: '⊕', n: 'DUELO DE PRECISIÓN',  d: 'Precisión a dos bandas. Sin tregua.',                             tag: '2J', c: 'green' },
    { ico: '⚡', n: 'SABOTAJE',            d: 'No pierdes vida fallando. Se la quitas al rival si aciertas mejor.', tag: '2J', c: 'purple' },
  ];

  constructor() {
    const id = setInterval(() => this.demoIdx.update(i => (i + 1) % DEMOS.length), 3200);
    this.destroyRef.onDestroy(() => clearInterval(id));
  }

  scrollTo(id: string): void {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
}
