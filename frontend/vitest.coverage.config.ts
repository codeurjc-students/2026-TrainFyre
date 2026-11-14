import { defineConfig } from 'vitest/config'

// Cobertura conjunta (unitarias + integración + componentes) para SonarQube.
export default defineConfig({
    test: {
        environment: 'node',
        setupFiles: ['tests/components/setup.ts'],
        include: [
            'tests/unit/**/*.test.ts',
            'tests/integration/**/*.test.ts',
            'tests/components/**/*.test.tsx',
        ],
        env: {
            VITE_API_BASE_URL: 'https://api.test/api/v1',
            VITE_PAGE_SIZE: '2',
        },
        coverage: {
            provider: 'v8',
            reporter: ['text', 'html', 'lcov'],
            include: ['src/**/*.{ts,tsx}'],
            exclude: [
                'src/main.tsx',
                'src/**/*.d.ts',
                'src/model/types.ts',
                'src/model/index.ts',
                'src/api/config.ts',
                'src/assets/**',
                'src/App.css',
                '**/*.config.ts',
            ],
        },
    },
})
