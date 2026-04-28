import { Component, effect, input, signal } from '@angular/core';

@Component({
  selector: 'app-rolling-number',
  standalone: true,
  template: `<span class="vs-rolling vs-mono">{{ prefix() }}{{ fmt(shown()) }}</span>`,
})
export class RollingNumberComponent {
  value  = input.required<number>();
  prefix = input('');

  shown = signal(0);

  private raf = 0;

  constructor() {
    effect(() => {
      const target = this.value();
      cancelAnimationFrame(this.raf);
      const start = performance.now();
      const dur = 900;
      const tick = (t: number) => {
        const p = Math.min(1, (t - start) / dur);
        const eased = 1 - Math.pow(1 - p, 3);
        this.shown.set(Math.floor(target * eased));
        if (p < 1) this.raf = requestAnimationFrame(tick);
        else this.shown.set(target);
      };
      this.raf = requestAnimationFrame(tick);
    });
  }

  fmt(n: number): string {
    return n.toLocaleString('es-ES');
  }
}
