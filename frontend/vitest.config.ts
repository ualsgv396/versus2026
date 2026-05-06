import { defineConfig } from 'vitest/config';

const isCI = !!process.env['CI'];

export default defineConfig({
  test: {
    reporters: isCI ? ['verbose', 'junit'] : ['default'],
    outputFile: isCI ? 'test-results/junit.xml' : undefined,
  },
});
