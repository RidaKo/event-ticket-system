# Frontend Scaffold Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stand up the `frontend/` folder as a Vite + React + TypeScript SPA with clean hybrid architecture (layered top-level, `features/` slices), wired to talk to the Spring Boot backend, including an auth skeleton.

**Architecture:** Layered top-level (`app/`, `pages/`, `shared/`, `styles/`) with vertical feature slices in `features/`. Single `fetch`-based API client with Spring Session cookie support. TanStack Query owns server state; Zustand owns client state. CSS Modules with design-token CSS variables.

**Tech Stack:** Vite 6, React 19, TypeScript (strict), React Router 7, TanStack Query 5, Zustand 5, React Hook Form 7, Zod 3, Vitest 2, React Testing Library, MSW 2, Playwright 1.49, ESLint 9 (flat), Prettier 3.

**Working directory for all commands:** `frontend/` (relative to repo root). The existing `frontend/dummy.txt` must be deleted in Task 1.

**Spec reference:** `docs/superpowers/specs/2026-04-19-frontend-scaffold-design.md`

---

## File structure overview

Files created across tasks (exact paths under `frontend/`):

```
frontend/
├── package.json
├── package-lock.json                 # npm-generated
├── index.html
├── vite.config.ts
├── tsconfig.json
├── tsconfig.node.json
├── tsconfig.app.json
├── eslint.config.js
├── .prettierrc
├── .prettierignore
├── .gitignore
├── .env.example
├── ARCHITECTURE.md
├── README.md
├── playwright.config.ts
│
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── router.tsx
│   ├── vite-env.d.ts
│   │
│   ├── app/
│   │   ├── providers/QueryProvider.tsx
│   │   ├── layouts/RootLayout.tsx
│   │   ├── layouts/RootLayout.module.css
│   │   ├── layouts/AuthLayout.tsx
│   │   ├── layouts/AuthLayout.module.css
│   │   └── guards/ProtectedRoute.tsx
│   │
│   ├── features/
│   │   └── auth/
│   │       ├── api/auth.api.ts
│   │       ├── api/auth.keys.ts
│   │       ├── components/LoginForm.tsx
│   │       ├── components/LoginForm.module.css
│   │       ├── components/LoginForm.test.tsx
│   │       ├── hooks/useAuth.ts
│   │       ├── hooks/useAuth.test.tsx
│   │       ├── pages/LoginPage.tsx
│   │       ├── pages/LoginPage.module.css
│   │       ├── store/auth.store.ts
│   │       ├── store/auth.store.test.ts
│   │       ├── types.ts
│   │       └── index.ts
│   │
│   ├── pages/
│   │   ├── HomePage.tsx
│   │   └── HomePage.module.css
│   │
│   ├── shared/
│   │   ├── lib/api-client.ts
│   │   ├── lib/api-client.test.ts
│   │   ├── lib/api-error.ts
│   │   ├── types/api.ts
│   │   ├── ui/Button/Button.tsx
│   │   ├── ui/Button/Button.module.css
│   │   ├── ui/Button/index.ts
│   │   ├── ui/Input/Input.tsx
│   │   ├── ui/Input/Input.module.css
│   │   └── ui/Input/index.ts
│   │
│   ├── styles/
│   │   ├── tokens.css
│   │   └── global.css
│   │
│   └── test/
│       ├── setup.ts
│       ├── render.tsx
│       └── mocks/
│           ├── handlers.ts
│           └── server.ts
│
└── e2e/
    └── login.spec.ts
```

Each file has one responsibility. Feature internals are private; only `features/<name>/index.ts` is the public surface.

---

## Task 1: Initialize Vite + React + TypeScript project

**Files:**
- Create: `frontend/package.json`, `frontend/index.html`, `frontend/vite.config.ts`, `frontend/tsconfig.json`, `frontend/tsconfig.app.json`, `frontend/tsconfig.node.json`, `frontend/.gitignore`, `frontend/.env.example`, `frontend/src/main.tsx`, `frontend/src/App.tsx`, `frontend/src/vite-env.d.ts`
- Delete: `frontend/dummy.txt`

- [ ] **Step 1: Delete the placeholder file**

```bash
cd frontend
rm dummy.txt
```

- [ ] **Step 2: Create `frontend/package.json`**

```json
{
  "name": "event-ticket-system-frontend",
  "private": true,
  "version": "0.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "preview": "vite preview",
    "lint": "eslint .",
    "format": "prettier --write .",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:e2e": "playwright test",
    "typecheck": "tsc -b --noEmit"
  },
  "dependencies": {
    "@hookform/resolvers": "^3.9.1",
    "@tanstack/react-query": "^5.62.0",
    "@tanstack/react-query-devtools": "^5.62.0",
    "react": "^19.0.0",
    "react-dom": "^19.0.0",
    "react-hook-form": "^7.54.0",
    "react-router-dom": "^7.1.0",
    "zod": "^3.24.0",
    "zustand": "^5.0.2"
  },
  "devDependencies": {
    "@eslint/js": "^9.17.0",
    "@playwright/test": "^1.49.0",
    "@testing-library/jest-dom": "^6.6.0",
    "@testing-library/react": "^16.1.0",
    "@testing-library/user-event": "^14.5.2",
    "@types/node": "^22.10.0",
    "@types/react": "^19.0.0",
    "@types/react-dom": "^19.0.0",
    "@vitejs/plugin-react": "^4.3.4",
    "eslint": "^9.17.0",
    "eslint-plugin-react-hooks": "^5.1.0",
    "eslint-plugin-react-refresh": "^0.4.16",
    "globals": "^15.13.0",
    "jsdom": "^25.0.1",
    "msw": "^2.7.0",
    "prettier": "^3.4.0",
    "typescript": "^5.7.0",
    "typescript-eslint": "^8.18.0",
    "vite": "^6.0.0",
    "vitest": "^2.1.8"
  }
}
```

- [ ] **Step 3: Create `frontend/index.html`**

```html
<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Event Ticket System</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
```

- [ ] **Step 4: Create `frontend/vite.config.ts`**

```ts
/// <reference types="vitest" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'node:path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    css: true,
  },
});
```

- [ ] **Step 5: Create `frontend/tsconfig.json`**

```json
{
  "files": [],
  "references": [
    { "path": "./tsconfig.app.json" },
    { "path": "./tsconfig.node.json" }
  ]
}
```

- [ ] **Step 6: Create `frontend/tsconfig.app.json`**

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "useDefineForClassFields": true,
    "lib": ["ES2022", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "moduleDetection": "force",
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    },
    "types": ["vitest/globals", "@testing-library/jest-dom"]
  },
  "include": ["src", "src/**/*.test.ts", "src/**/*.test.tsx"]
}
```

- [ ] **Step 7: Create `frontend/tsconfig.node.json`**

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "lib": ["ES2023"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "isolatedModules": true,
    "moduleDetection": "force",
    "noEmit": true,
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true
  },
  "include": ["vite.config.ts", "playwright.config.ts"]
}
```

- [ ] **Step 8: Create `frontend/.gitignore`**

```
node_modules
dist
dist-ssr
*.local
.env
.env.*.local

# Editor
.vscode/*
!.vscode/extensions.json
.idea
*.suo
*.ntvs*
*.njsproj
*.sln
*.sw?

# Logs
npm-debug.log*
yarn-debug.log*
yarn-error.log*
pnpm-debug.log*

# Test output
coverage
playwright-report
test-results
.nyc_output
```

- [ ] **Step 9: Create `frontend/.env.example`**

```
# Base URL for API calls. In dev this is proxied to Spring Boot by Vite.
VITE_API_BASE_URL=/api
```

- [ ] **Step 10: Create `frontend/src/vite-env.d.ts`**

```ts
/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
```

- [ ] **Step 11: Create minimal `frontend/src/App.tsx`**

```tsx
export default function App() {
  return <h1>Event Ticket System</h1>;
}
```

- [ ] **Step 12: Create `frontend/src/main.tsx`**

```tsx
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
```

- [ ] **Step 13: Install dependencies**

Run: `cd frontend && npm install`
Expected: completes without errors, `node_modules/` and `package-lock.json` appear.

- [ ] **Step 14: Verify dev server boots and typechecks**

Run: `cd frontend && npm run typecheck`
Expected: exits with code 0, no output.

Run: `cd frontend && timeout 6 npm run dev || true`
Expected: prints `Local: http://localhost:5173/` and then the timeout kills it.

- [ ] **Step 15: Commit**

```bash
cd frontend
git add -A
git rm --cached -r node_modules 2>/dev/null || true
cd ..
git add frontend/ -- ':!frontend/node_modules'
git commit -m "feat(frontend): initialize Vite + React + TypeScript project"
```

---

## Task 2: ESLint (flat config) + Prettier

**Files:**
- Create: `frontend/eslint.config.js`, `frontend/.prettierrc`, `frontend/.prettierignore`

- [ ] **Step 1: Create `frontend/eslint.config.js`**

```js
import js from '@eslint/js';
import globals from 'globals';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import tseslint from 'typescript-eslint';

export default tseslint.config(
  { ignores: ['dist', 'playwright-report', 'test-results', 'coverage'] },
  {
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      ecmaVersion: 2022,
      globals: globals.browser,
    },
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': [
        'warn',
        { allowConstantExport: true },
      ],
      '@typescript-eslint/no-unused-vars': [
        'error',
        { argsIgnorePattern: '^_', varsIgnorePattern: '^_' },
      ],
    },
  },
);
```

- [ ] **Step 2: Create `frontend/.prettierrc`**

```json
{
  "semi": true,
  "singleQuote": true,
  "trailingComma": "all",
  "printWidth": 100,
  "tabWidth": 2,
  "arrowParens": "always"
}
```

- [ ] **Step 3: Create `frontend/.prettierignore`**

```
node_modules
dist
playwright-report
test-results
coverage
package-lock.json
```

- [ ] **Step 4: Run lint and format**

Run: `cd frontend && npm run lint`
Expected: exits with code 0.

Run: `cd frontend && npm run format`
Expected: completes without errors; may rewrite a few files with Prettier formatting.

- [ ] **Step 5: Commit**

```bash
git add frontend/eslint.config.js frontend/.prettierrc frontend/.prettierignore frontend/src
git commit -m "feat(frontend): add ESLint flat config and Prettier"
```

---

## Task 3: Folder skeleton, styles, and path alias sanity check

**Files:**
- Create: `frontend/src/styles/tokens.css`, `frontend/src/styles/global.css`
- Create empty folder markers (`.gitkeep`) in: `frontend/src/app/providers/`, `frontend/src/app/layouts/`, `frontend/src/app/guards/`, `frontend/src/features/`, `frontend/src/pages/`, `frontend/src/shared/lib/`, `frontend/src/shared/ui/`, `frontend/src/shared/types/`, `frontend/src/test/mocks/`, `frontend/e2e/`
- Modify: `frontend/src/main.tsx` to import global CSS.

- [ ] **Step 1: Create `frontend/src/styles/tokens.css`**

```css
:root {
  /* Color tokens — placeholder palette. Swap with Figma tokens as designs land. */
  --color-bg: #ffffff;
  --color-surface: #f6f7fb;
  --color-text: #1b1d24;
  --color-text-muted: #5a6072;
  --color-primary: #2f5bea;
  --color-primary-hover: #2548c0;
  --color-border: #d8dbe3;
  --color-danger: #c23b3b;
  --color-success: #1f8b4a;

  /* Spacing scale (4px base) */
  --space-1: 0.25rem;
  --space-2: 0.5rem;
  --space-3: 0.75rem;
  --space-4: 1rem;
  --space-6: 1.5rem;
  --space-8: 2rem;

  /* Typography */
  --font-body: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
  --font-size-sm: 0.875rem;
  --font-size-base: 1rem;
  --font-size-lg: 1.125rem;
  --font-size-xl: 1.5rem;

  /* Radii */
  --radius-sm: 4px;
  --radius-md: 8px;

  /* Shadows */
  --shadow-sm: 0 1px 2px rgba(16, 24, 40, 0.05);
  --shadow-md: 0 4px 8px rgba(16, 24, 40, 0.08);
}
```

- [ ] **Step 2: Create `frontend/src/styles/global.css`**

```css
@import './tokens.css';

*,
*::before,
*::after {
  box-sizing: border-box;
}

html,
body,
#root {
  height: 100%;
  margin: 0;
}

body {
  font-family: var(--font-body);
  font-size: var(--font-size-base);
  color: var(--color-text);
  background: var(--color-bg);
  -webkit-font-smoothing: antialiased;
}

a {
  color: var(--color-primary);
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}

button {
  font-family: inherit;
}
```

- [ ] **Step 3: Import global CSS from `frontend/src/main.tsx`**

Replace the full contents of `frontend/src/main.tsx` with:

```tsx
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App';
import './styles/global.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
```

- [ ] **Step 4: Create folder markers**

Run:

```bash
cd frontend
for d in src/app/providers src/app/layouts src/app/guards src/features src/pages src/shared/lib src/shared/ui src/shared/types src/test/mocks e2e; do
  mkdir -p "$d"
  touch "$d/.gitkeep"
done
```

- [ ] **Step 5: Confirm path alias by adding a one-line sanity import**

Modify `frontend/src/App.tsx` to import nothing new but prove `@/` works by referencing the stylesheet path (we already import via main). No change needed to App; instead run typecheck to verify the alias in `tsconfig.app.json`.

Run: `cd frontend && npm run typecheck`
Expected: exits 0.

- [ ] **Step 6: Commit**

```bash
git add frontend/src frontend/e2e
git commit -m "feat(frontend): add folder skeleton, design tokens, and global styles"
```

---

## Task 4: API client and shared types (TDD)

**Files:**
- Create: `frontend/src/shared/types/api.ts`, `frontend/src/shared/lib/api-error.ts`, `frontend/src/shared/lib/api-client.ts`, `frontend/src/shared/lib/api-client.test.ts`

- [ ] **Step 1: Write failing tests — `frontend/src/shared/lib/api-client.test.ts`**

```ts
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from './api-client';
import { ApiError } from './api-error';

describe('apiClient', () => {
  const fetchMock = vi.fn();

  beforeEach(() => {
    vi.stubGlobal('fetch', fetchMock);
  });

  afterEach(() => {
    fetchMock.mockReset();
    vi.unstubAllGlobals();
  });

  it('prefixes paths with VITE_API_BASE_URL and includes credentials', async () => {
    fetchMock.mockResolvedValueOnce(
      new Response(JSON.stringify({ ok: true }), {
        status: 200,
        headers: { 'content-type': 'application/json' },
      }),
    );

    await apiClient.get('/auth/me');

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe('/api/auth/me');
    expect(init.credentials).toBe('include');
    expect(init.method).toBe('GET');
  });

  it('parses JSON responses', async () => {
    fetchMock.mockResolvedValueOnce(
      new Response(JSON.stringify({ id: 1, name: 'Ada' }), {
        status: 200,
        headers: { 'content-type': 'application/json' },
      }),
    );

    const data = await apiClient.get<{ id: number; name: string }>('/user');

    expect(data).toEqual({ id: 1, name: 'Ada' });
  });

  it('returns undefined for 204 No Content', async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }));

    const data = await apiClient.post('/auth/logout');

    expect(data).toBeUndefined();
  });

  it('throws ApiError on non-2xx with parsed JSON body', async () => {
    fetchMock.mockResolvedValueOnce(
      new Response(JSON.stringify({ message: 'Invalid credentials' }), {
        status: 401,
        headers: { 'content-type': 'application/json' },
      }),
    );

    await expect(apiClient.post('/auth/login', { u: 'x' })).rejects.toMatchObject({
      status: 401,
      message: 'Invalid credentials',
    });
  });

  it('throws ApiError on non-2xx with non-JSON body', async () => {
    fetchMock.mockResolvedValueOnce(
      new Response('Server down', {
        status: 500,
        headers: { 'content-type': 'text/plain' },
      }),
    );

    await expect(apiClient.get('/anything')).rejects.toBeInstanceOf(ApiError);
    await expect(apiClient.get('/anything')).rejects.toMatchObject({ status: 500 });
  });

  it('sends JSON body on POST', async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }));

    await apiClient.post('/auth/login', { username: 'ada', password: 'pw' });

    const [, init] = fetchMock.mock.calls[0];
    expect(init.method).toBe('POST');
    expect(init.headers).toMatchObject({ 'content-type': 'application/json' });
    expect(JSON.parse(init.body as string)).toEqual({ username: 'ada', password: 'pw' });
  });
});
```

- [ ] **Step 2: Run tests to confirm they fail**

Run: `cd frontend && npm test -- --run src/shared/lib/api-client.test.ts`
Expected: FAIL with "Cannot find module './api-client'" or similar.

- [ ] **Step 3: Create `frontend/src/shared/types/api.ts`**

```ts
export interface ApiErrorBody {
  message?: string;
  details?: unknown;
}

export interface Paginated<T> {
  items: T[];
  page: number;
  pageSize: number;
  total: number;
}
```

- [ ] **Step 4: Create `frontend/src/shared/lib/api-error.ts`**

```ts
export class ApiError extends Error {
  readonly status: number;
  readonly details?: unknown;

  constructor(status: number, message: string, details?: unknown) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.details = details;
  }
}
```

- [ ] **Step 5: Create `frontend/src/shared/lib/api-client.ts`**

```ts
import { ApiError } from './api-error';

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

type JsonBody = Record<string, unknown> | unknown[] | null;

interface RequestOptions {
  signal?: AbortSignal;
}

async function request<T>(
  method: string,
  path: string,
  body?: JsonBody,
  options: RequestOptions = {},
): Promise<T> {
  const init: RequestInit = {
    method,
    credentials: 'include',
    signal: options.signal,
    headers: {},
  };

  if (body !== undefined) {
    (init.headers as Record<string, string>)['content-type'] = 'application/json';
    init.body = JSON.stringify(body);
  }

  const response = await fetch(`${BASE_URL}${path}`, init);

  if (response.status === 204) {
    return undefined as T;
  }

  const contentType = response.headers.get('content-type') ?? '';
  const isJson = contentType.includes('application/json');
  const parsed = isJson ? await response.json().catch(() => undefined) : await response.text();

  if (!response.ok) {
    const message =
      (isJson && parsed && typeof parsed === 'object' && 'message' in parsed
        ? String((parsed as { message: unknown }).message)
        : typeof parsed === 'string' && parsed
          ? parsed
          : response.statusText) || `Request failed with status ${response.status}`;
    throw new ApiError(response.status, message, isJson ? parsed : undefined);
  }

  return parsed as T;
}

export const apiClient = {
  get: <T>(path: string, options?: RequestOptions) => request<T>('GET', path, undefined, options),
  post: <T = void>(path: string, body?: JsonBody, options?: RequestOptions) =>
    request<T>('POST', path, body ?? null, options),
  put: <T = void>(path: string, body?: JsonBody, options?: RequestOptions) =>
    request<T>('PUT', path, body ?? null, options),
  delete: <T = void>(path: string, options?: RequestOptions) =>
    request<T>('DELETE', path, undefined, options),
};
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `cd frontend && npm test -- --run src/shared/lib/api-client.test.ts`
Expected: all 6 tests pass.

- [ ] **Step 7: Commit**

```bash
git add frontend/src/shared
git commit -m "feat(frontend): add API client with ApiError and shared types"
```

---

## Task 5: App providers, router skeleton, RootLayout, HomePage

**Files:**
- Create: `frontend/src/app/providers/QueryProvider.tsx`, `frontend/src/app/layouts/RootLayout.tsx`, `frontend/src/app/layouts/RootLayout.module.css`, `frontend/src/pages/HomePage.tsx`, `frontend/src/pages/HomePage.module.css`, `frontend/src/router.tsx`
- Modify: `frontend/src/App.tsx`

- [ ] **Step 1: Create `frontend/src/app/providers/QueryProvider.tsx`**

```tsx
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { type ReactNode, useState } from 'react';

export function QueryProvider({ children }: { children: ReactNode }) {
  const [client] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            retry: false,
            refetchOnWindowFocus: false,
            staleTime: 30_000,
          },
        },
      }),
  );

  return (
    <QueryClientProvider client={client}>
      {children}
      {import.meta.env.DEV ? <ReactQueryDevtools initialIsOpen={false} /> : null}
    </QueryClientProvider>
  );
}
```

- [ ] **Step 2: Create `frontend/src/app/layouts/RootLayout.module.css`**

```css
.shell {
  display: grid;
  grid-template-rows: auto 1fr;
  min-height: 100%;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-6);
  border-bottom: 1px solid var(--color-border);
  background: var(--color-surface);
}

.brand {
  font-size: var(--font-size-lg);
  font-weight: 600;
}

.main {
  padding: var(--space-6);
}
```

- [ ] **Step 3: Create `frontend/src/app/layouts/RootLayout.tsx`**

```tsx
import { Outlet } from 'react-router-dom';
import styles from './RootLayout.module.css';

export function RootLayout() {
  return (
    <div className={styles.shell}>
      <header className={styles.header}>
        <span className={styles.brand}>Event Ticket System</span>
      </header>
      <main className={styles.main}>
        <Outlet />
      </main>
    </div>
  );
}
```

- [ ] **Step 4: Create `frontend/src/pages/HomePage.module.css`**

```css
.page {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.title {
  font-size: var(--font-size-xl);
  margin: 0;
}

.body {
  color: var(--color-text-muted);
}
```

- [ ] **Step 5: Create `frontend/src/pages/HomePage.tsx`**

```tsx
import styles from './HomePage.module.css';

export function HomePage() {
  return (
    <section className={styles.page}>
      <h1 className={styles.title}>Welcome</h1>
      <p className={styles.body}>Scaffold ready. Features land here.</p>
    </section>
  );
}
```

- [ ] **Step 6: Create `frontend/src/router.tsx`**

```tsx
import { createBrowserRouter } from 'react-router-dom';
import { RootLayout } from '@/app/layouts/RootLayout';
import { HomePage } from '@/pages/HomePage';

export const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [{ index: true, element: <HomePage /> }],
  },
]);
```

- [ ] **Step 7: Replace `frontend/src/App.tsx`**

```tsx
import { RouterProvider } from 'react-router-dom';
import { QueryProvider } from '@/app/providers/QueryProvider';
import { router } from './router';

export default function App() {
  return (
    <QueryProvider>
      <RouterProvider router={router} />
    </QueryProvider>
  );
}
```

- [ ] **Step 8: Verify typecheck and dev boot**

Run: `cd frontend && npm run typecheck`
Expected: exits 0.

Run: `cd frontend && timeout 6 npm run dev || true`
Expected: `Local: http://localhost:5173/` printed.

- [ ] **Step 9: Commit**

```bash
git add frontend/src
git commit -m "feat(frontend): add QueryProvider, RootLayout, HomePage, router skeleton"
```

---

## Task 6: Zustand auth store (TDD)

**Files:**
- Create: `frontend/src/features/auth/types.ts`, `frontend/src/features/auth/store/auth.store.ts`, `frontend/src/features/auth/store/auth.store.test.ts`

- [ ] **Step 1: Create `frontend/src/features/auth/types.ts`**

```ts
export interface User {
  id: string;
  username: string;
  email?: string;
  roles: string[];
}

export interface LoginRequest {
  username: string;
  password: string;
}
```

- [ ] **Step 2: Write failing tests — `frontend/src/features/auth/store/auth.store.test.ts`**

```ts
import { beforeEach, describe, expect, it } from 'vitest';
import { useAuthStore } from './auth.store';
import type { User } from '../types';

const user: User = {
  id: 'u1',
  username: 'ada',
  roles: ['USER'],
};

describe('useAuthStore', () => {
  beforeEach(() => {
    useAuthStore.getState().clear();
    localStorage.clear();
  });

  it('starts in idle status with no user', () => {
    expect(useAuthStore.getState().user).toBeNull();
    expect(useAuthStore.getState().status).toBe('idle');
  });

  it('setUser stores the user and marks authenticated', () => {
    useAuthStore.getState().setUser(user);
    expect(useAuthStore.getState().user).toEqual(user);
    expect(useAuthStore.getState().status).toBe('authenticated');
  });

  it('clear resets to unauthenticated', () => {
    useAuthStore.getState().setUser(user);
    useAuthStore.getState().clear();
    expect(useAuthStore.getState().user).toBeNull();
    expect(useAuthStore.getState().status).toBe('unauthenticated');
  });

  it('persists user to localStorage when set', () => {
    useAuthStore.getState().setUser(user);
    const raw = localStorage.getItem('auth');
    expect(raw).toBeTruthy();
    expect(raw!).toContain('"username":"ada"');
  });
});
```

- [ ] **Step 3: Run tests to confirm failure**

Run: `cd frontend && npm test -- --run src/features/auth/store/auth.store.test.ts`
Expected: FAIL — module not found.

- [ ] **Step 4: Create `frontend/src/features/auth/store/auth.store.ts`**

```ts
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { User } from '../types';

export type AuthStatus = 'idle' | 'authenticated' | 'unauthenticated';

interface AuthState {
  user: User | null;
  status: AuthStatus;
  setUser: (user: User) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      status: 'idle',
      setUser: (user) => set({ user, status: 'authenticated' }),
      clear: () => set({ user: null, status: 'unauthenticated' }),
    }),
    {
      name: 'auth',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({ user: state.user, status: state.status }),
    },
  ),
);
```

- [ ] **Step 5: Run tests to verify pass**

Run: `cd frontend && npm test -- --run src/features/auth/store/auth.store.test.ts`
Expected: all 4 tests pass.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/features/auth
git commit -m "feat(auth): add Zustand auth store with localStorage persistence"
```

---

## Task 7: MSW test infrastructure

**Files:**
- Create: `frontend/src/test/setup.ts`, `frontend/src/test/mocks/handlers.ts`, `frontend/src/test/mocks/server.ts`, `frontend/src/test/render.tsx`

- [ ] **Step 1: Create `frontend/src/test/mocks/handlers.ts`**

```ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  http.post('/api/auth/login', async ({ request }) => {
    const body = (await request.json()) as { username?: string; password?: string };
    if (body.username === 'ada' && body.password === 'correct') {
      return HttpResponse.json({
        id: 'u1',
        username: 'ada',
        roles: ['USER'],
      });
    }
    return HttpResponse.json({ message: 'Invalid credentials' }, { status: 401 });
  }),

  http.post('/api/auth/logout', () => new HttpResponse(null, { status: 204 })),

  http.get('/api/auth/me', () =>
    HttpResponse.json({ message: 'Unauthenticated' }, { status: 401 }),
  ),
];
```

- [ ] **Step 2: Create `frontend/src/test/mocks/server.ts`**

```ts
import { setupServer } from 'msw/node';
import { handlers } from './handlers';

export const server = setupServer(...handlers);
```

- [ ] **Step 3: Create `frontend/src/test/setup.ts`**

```ts
import '@testing-library/jest-dom/vitest';
import { afterAll, afterEach, beforeAll } from 'vitest';
import { server } from './mocks/server';

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
```

- [ ] **Step 4: Create `frontend/src/test/render.tsx`** (shared RTL helper that wires QueryClient + Router)

```tsx
import type { ReactElement, ReactNode } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, type RenderOptions } from '@testing-library/react';

export function makeQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false, gcTime: 0, staleTime: 0 },
      mutations: { retry: false },
    },
  });
}

interface RenderWithProvidersOptions extends Omit<RenderOptions, 'wrapper'> {
  route?: string;
  queryClient?: QueryClient;
}

export function renderWithProviders(ui: ReactElement, options: RenderWithProvidersOptions = {}) {
  const { route = '/', queryClient = makeQueryClient(), ...rest } = options;

  function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={[route]}>{children}</MemoryRouter>
      </QueryClientProvider>
    );
  }

  return { queryClient, ...render(ui, { wrapper: Wrapper, ...rest }) };
}
```

- [ ] **Step 5: Verify existing tests still pass**

Run: `cd frontend && npm test -- --run`
Expected: the 10 tests from Tasks 4 and 6 all pass; MSW logs no unhandled requests.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/test
git commit -m "feat(frontend): add MSW test infrastructure and render helper"
```

---

## Task 8: Auth API module (keys + endpoints)

**Files:**
- Create: `frontend/src/features/auth/api/auth.keys.ts`, `frontend/src/features/auth/api/auth.api.ts`

- [ ] **Step 1: Create `frontend/src/features/auth/api/auth.keys.ts`**

```ts
export const authKeys = {
  all: ['auth'] as const,
  me: () => [...authKeys.all, 'me'] as const,
};
```

- [ ] **Step 2: Create `frontend/src/features/auth/api/auth.api.ts`**

```ts
import { apiClient } from '@/shared/lib/api-client';
import type { LoginRequest, User } from '../types';

export function login(body: LoginRequest): Promise<User> {
  return apiClient.post<User>('/auth/login', body);
}

export function logout(): Promise<void> {
  return apiClient.post<void>('/auth/logout');
}

export function fetchCurrentUser(): Promise<User> {
  return apiClient.get<User>('/auth/me');
}
```

- [ ] **Step 3: Verify typecheck**

Run: `cd frontend && npm run typecheck`
Expected: exits 0.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/features/auth/api
git commit -m "feat(auth): add auth API module and query keys"
```

---

## Task 9: Auth hooks (`useAuth`) with MSW-backed tests

**Files:**
- Create: `frontend/src/features/auth/hooks/useAuth.ts`, `frontend/src/features/auth/hooks/useAuth.test.tsx`

- [ ] **Step 1: Write failing tests — `frontend/src/features/auth/hooks/useAuth.test.tsx`**

```tsx
import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { useCurrentUser, useLogin, useLogout } from './useAuth';
import { useAuthStore } from '../store/auth.store';

function makeWrapper() {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false, gcTime: 0 }, mutations: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
  };
}

describe('auth hooks', () => {
  beforeEach(() => {
    useAuthStore.getState().clear();
    localStorage.clear();
  });

  it('useLogin success writes user to store', async () => {
    const { result } = renderHook(() => useLogin(), { wrapper: makeWrapper() });

    await act(async () => {
      await result.current.mutateAsync({ username: 'ada', password: 'correct' });
    });

    expect(useAuthStore.getState().user?.username).toBe('ada');
    expect(useAuthStore.getState().status).toBe('authenticated');
  });

  it('useLogin failure surfaces an error and leaves store cleared', async () => {
    const { result } = renderHook(() => useLogin(), { wrapper: makeWrapper() });

    await act(async () => {
      await expect(
        result.current.mutateAsync({ username: 'ada', password: 'wrong' }),
      ).rejects.toMatchObject({ status: 401 });
    });

    expect(useAuthStore.getState().user).toBeNull();
  });

  it('useLogout clears the store', async () => {
    useAuthStore.getState().setUser({ id: 'u1', username: 'ada', roles: ['USER'] });
    const { result } = renderHook(() => useLogout(), { wrapper: makeWrapper() });

    await act(async () => {
      await result.current.mutateAsync();
    });

    expect(useAuthStore.getState().user).toBeNull();
    expect(useAuthStore.getState().status).toBe('unauthenticated');
  });

  it('useCurrentUser returns error when unauthenticated', async () => {
    const { result } = renderHook(() => useCurrentUser(), { wrapper: makeWrapper() });
    await waitFor(() => expect(result.current.isError).toBe(true));
    expect(result.current.error).toMatchObject({ status: 401 });
  });
});
```

- [ ] **Step 2: Run tests to confirm failure**

Run: `cd frontend && npm test -- --run src/features/auth/hooks/useAuth.test.tsx`
Expected: FAIL — module not found.

- [ ] **Step 3: Create `frontend/src/features/auth/hooks/useAuth.ts`**

```ts
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchCurrentUser, login, logout } from '../api/auth.api';
import { authKeys } from '../api/auth.keys';
import { useAuthStore } from '../store/auth.store';
import type { LoginRequest, User } from '../types';

export function useCurrentUser() {
  return useQuery<User>({
    queryKey: authKeys.me(),
    queryFn: fetchCurrentUser,
    retry: false,
  });
}

export function useLogin() {
  const queryClient = useQueryClient();
  const setUser = useAuthStore((s) => s.setUser);

  return useMutation({
    mutationFn: (body: LoginRequest) => login(body),
    onSuccess: (user) => {
      setUser(user);
      queryClient.setQueryData(authKeys.me(), user);
    },
  });
}

export function useLogout() {
  const queryClient = useQueryClient();
  const clear = useAuthStore((s) => s.clear);

  return useMutation({
    mutationFn: () => logout(),
    onSettled: () => {
      clear();
      queryClient.clear();
    },
  });
}
```

- [ ] **Step 4: Run tests to verify pass**

Run: `cd frontend && npm test -- --run src/features/auth/hooks/useAuth.test.tsx`
Expected: all 4 tests pass.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/features/auth/hooks
git commit -m "feat(auth): add useCurrentUser, useLogin, useLogout hooks"
```

---

## Task 10: Shared UI primitives — Button and Input

**Files:**
- Create: `frontend/src/shared/ui/Button/Button.tsx`, `frontend/src/shared/ui/Button/Button.module.css`, `frontend/src/shared/ui/Button/index.ts`, `frontend/src/shared/ui/Input/Input.tsx`, `frontend/src/shared/ui/Input/Input.module.css`, `frontend/src/shared/ui/Input/index.ts`

- [ ] **Step 1: Create `frontend/src/shared/ui/Button/Button.module.css`**

```css
.button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-2) var(--space-4);
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-base);
  font-weight: 500;
  cursor: pointer;
  transition: background-color 120ms ease;
}

.button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.primary {
  background: var(--color-primary);
  color: #fff;
}

.primary:hover:not(:disabled) {
  background: var(--color-primary-hover);
}

.secondary {
  background: transparent;
  color: var(--color-text);
  border-color: var(--color-border);
}

.secondary:hover:not(:disabled) {
  background: var(--color-surface);
}
```

- [ ] **Step 2: Create `frontend/src/shared/ui/Button/Button.tsx`**

```tsx
import { forwardRef, type ButtonHTMLAttributes } from 'react';
import styles from './Button.module.css';

type Variant = 'primary' | 'secondary';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(function Button(
  { variant = 'primary', className, ...rest },
  ref,
) {
  const classes = [styles.button, styles[variant], className].filter(Boolean).join(' ');
  return <button ref={ref} className={classes} {...rest} />;
});
```

- [ ] **Step 3: Create `frontend/src/shared/ui/Button/index.ts`**

```ts
export { Button } from './Button';
```

- [ ] **Step 4: Create `frontend/src/shared/ui/Input/Input.module.css`**

```css
.field {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.label {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.input {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-base);
  background: #fff;
  color: var(--color-text);
}

.input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(47, 91, 234, 0.15);
}

.error {
  color: var(--color-danger);
  font-size: var(--font-size-sm);
}
```

- [ ] **Step 5: Create `frontend/src/shared/ui/Input/Input.tsx`**

```tsx
import { forwardRef, type InputHTMLAttributes } from 'react';
import styles from './Input.module.css';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input(
  { label, error, id, ...rest },
  ref,
) {
  const inputId = id ?? `in-${label.toLowerCase().replace(/\s+/g, '-')}`;
  const errorId = `${inputId}-error`;
  return (
    <label className={styles.field} htmlFor={inputId}>
      <span className={styles.label}>{label}</span>
      <input
        ref={ref}
        id={inputId}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? errorId : undefined}
        className={styles.input}
        {...rest}
      />
      {error ? (
        <span id={errorId} className={styles.error}>
          {error}
        </span>
      ) : null}
    </label>
  );
});
```

- [ ] **Step 6: Create `frontend/src/shared/ui/Input/index.ts`**

```ts
export { Input } from './Input';
```

- [ ] **Step 7: Verify typecheck**

Run: `cd frontend && npm run typecheck`
Expected: exits 0.

- [ ] **Step 8: Commit**

```bash
git add frontend/src/shared/ui
git commit -m "feat(frontend): add Button and Input shared UI primitives"
```

---

## Task 11: ProtectedRoute guard and AuthLayout

**Files:**
- Create: `frontend/src/app/guards/ProtectedRoute.tsx`, `frontend/src/app/layouts/AuthLayout.tsx`, `frontend/src/app/layouts/AuthLayout.module.css`

- [ ] **Step 1: Create `frontend/src/app/guards/ProtectedRoute.tsx`**

```tsx
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/features/auth/store/auth.store';

export function ProtectedRoute() {
  const status = useAuthStore((s) => s.status);
  const location = useLocation();

  if (status !== 'authenticated') {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}
```

- [ ] **Step 2: Create `frontend/src/app/layouts/AuthLayout.module.css`**

```css
.shell {
  display: grid;
  place-items: center;
  min-height: 100%;
  padding: var(--space-6);
  background: var(--color-surface);
}

.card {
  width: 100%;
  max-width: 360px;
  padding: var(--space-6);
  border-radius: var(--radius-md);
  background: #fff;
  box-shadow: var(--shadow-md);
}
```

- [ ] **Step 3: Create `frontend/src/app/layouts/AuthLayout.tsx`**

```tsx
import { Outlet } from 'react-router-dom';
import styles from './AuthLayout.module.css';

export function AuthLayout() {
  return (
    <div className={styles.shell}>
      <div className={styles.card}>
        <Outlet />
      </div>
    </div>
  );
}
```

- [ ] **Step 4: Verify typecheck**

Run: `cd frontend && npm run typecheck`
Expected: exits 0.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/app
git commit -m "feat(auth): add ProtectedRoute guard and AuthLayout"
```

---

## Task 12: LoginForm, LoginPage, and feature barrel

**Files:**
- Create: `frontend/src/features/auth/components/LoginForm.tsx`, `frontend/src/features/auth/components/LoginForm.module.css`, `frontend/src/features/auth/components/LoginForm.test.tsx`, `frontend/src/features/auth/pages/LoginPage.tsx`, `frontend/src/features/auth/pages/LoginPage.module.css`, `frontend/src/features/auth/index.ts`

- [ ] **Step 1: Write failing test — `frontend/src/features/auth/components/LoginForm.test.tsx`**

```tsx
import { beforeEach, describe, expect, it } from 'vitest';
import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/react';
import { renderWithProviders } from '@/test/render';
import { LoginForm } from './LoginForm';
import { useAuthStore } from '../store/auth.store';

describe('LoginForm', () => {
  beforeEach(() => {
    useAuthStore.getState().clear();
    localStorage.clear();
  });

  it('logs the user in on valid credentials', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginForm />);

    await user.type(screen.getByLabelText(/username/i), 'ada');
    await user.type(screen.getByLabelText(/password/i), 'correct');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => expect(useAuthStore.getState().status).toBe('authenticated'));
  });

  it('shows a server error on invalid credentials', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginForm />);

    await user.type(screen.getByLabelText(/username/i), 'ada');
    await user.type(screen.getByLabelText(/password/i), 'wrong');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText(/invalid credentials/i)).toBeInTheDocument();
    expect(useAuthStore.getState().status).not.toBe('authenticated');
  });

  it('shows validation errors when fields are empty', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginForm />);

    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText(/username is required/i)).toBeInTheDocument();
    expect(await screen.findByText(/password is required/i)).toBeInTheDocument();
  });
});
```

- [ ] **Step 2: Run the test to confirm failure**

Run: `cd frontend && npm test -- --run src/features/auth/components/LoginForm.test.tsx`
Expected: FAIL — module not found.

- [ ] **Step 3: Create `frontend/src/features/auth/components/LoginForm.module.css`**

```css
.form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.heading {
  margin: 0 0 var(--space-2);
  font-size: var(--font-size-xl);
}

.serverError {
  color: var(--color-danger);
  font-size: var(--font-size-sm);
}
```

- [ ] **Step 4: Create `frontend/src/features/auth/components/LoginForm.tsx`**

```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import { ApiError } from '@/shared/lib/api-error';
import { useLogin } from '../hooks/useAuth';
import styles from './LoginForm.module.css';

const schema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
});

type FormValues = z.infer<typeof schema>;

export function LoginForm() {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { username: '', password: '' },
  });
  const login = useLogin();

  const onSubmit = handleSubmit(async (values) => {
    login.reset();
    await login.mutateAsync(values).catch(() => {
      /* error surfaces via login.error */
    });
  });

  const serverError = login.error instanceof ApiError ? login.error.message : null;

  return (
    <form className={styles.form} onSubmit={onSubmit} noValidate>
      <h1 className={styles.heading}>Sign in</h1>

      <Input
        label="Username"
        autoComplete="username"
        error={errors.username?.message}
        {...register('username')}
      />

      <Input
        label="Password"
        type="password"
        autoComplete="current-password"
        error={errors.password?.message}
        {...register('password')}
      />

      {serverError ? <p className={styles.serverError}>{serverError}</p> : null}

      <Button type="submit" disabled={isSubmitting || login.isPending}>
        {login.isPending ? 'Signing in…' : 'Sign in'}
      </Button>
    </form>
  );
}
```

- [ ] **Step 5: Create `frontend/src/features/auth/pages/LoginPage.module.css`**

```css
.page {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
```

- [ ] **Step 6: Create `frontend/src/features/auth/pages/LoginPage.tsx`**

```tsx
import { LoginForm } from '../components/LoginForm';
import styles from './LoginPage.module.css';

export function LoginPage() {
  return (
    <div className={styles.page}>
      <LoginForm />
    </div>
  );
}
```

- [ ] **Step 7: Create `frontend/src/features/auth/index.ts`** (public feature barrel)

```ts
export { LoginPage } from './pages/LoginPage';
export { useCurrentUser, useLogin, useLogout } from './hooks/useAuth';
export { useAuthStore } from './store/auth.store';
export type { User, LoginRequest } from './types';
```

- [ ] **Step 8: Run tests to verify pass**

Run: `cd frontend && npm test -- --run src/features/auth/components/LoginForm.test.tsx`
Expected: all 3 tests pass.

- [ ] **Step 9: Commit**

```bash
git add frontend/src/features/auth
git commit -m "feat(auth): add LoginForm, LoginPage, and feature barrel"
```

---

## Task 13: Wire auth routes into router and protect HomePage

**Files:**
- Modify: `frontend/src/router.tsx`

- [ ] **Step 1: Replace `frontend/src/router.tsx` contents**

```tsx
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { RootLayout } from '@/app/layouts/RootLayout';
import { AuthLayout } from '@/app/layouts/AuthLayout';
import { ProtectedRoute } from '@/app/guards/ProtectedRoute';
import { HomePage } from '@/pages/HomePage';
import { LoginPage } from '@/features/auth';

export const router = createBrowserRouter([
  {
    element: <AuthLayout />,
    children: [{ path: '/login', element: <LoginPage /> }],
  },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <RootLayout />,
        children: [{ index: true, element: <HomePage /> }],
      },
    ],
  },
  { path: '*', element: <Navigate to="/" replace /> },
]);
```

- [ ] **Step 2: Verify typecheck and dev boot**

Run: `cd frontend && npm run typecheck`
Expected: exits 0.

Run: `cd frontend && timeout 6 npm run dev || true`
Expected: `Local: http://localhost:5173/` printed.

- [ ] **Step 3: Manual verification (document in the commit)**

Open `http://localhost:5173/` in a browser. Expected: redirects to `/login` because auth status is `idle` (treated as unauthenticated by the guard).

- [ ] **Step 4: Run full test suite**

Run: `cd frontend && npm test -- --run`
Expected: all tests from Tasks 4, 6, 9, 12 pass (17 total).

- [ ] **Step 5: Commit**

```bash
git add frontend/src/router.tsx
git commit -m "feat(auth): wire /login route and protect the app shell"
```

---

## Task 14: Playwright config and login smoke test

**Files:**
- Create: `frontend/playwright.config.ts`, `frontend/e2e/login.spec.ts`

- [ ] **Step 1: Create `frontend/playwright.config.ts`**

```ts
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  reporter: [['list']],
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },
});
```

- [ ] **Step 2: Create `frontend/e2e/login.spec.ts`**

This test uses Playwright's network routing to stub the Spring Boot endpoints — no live backend required.

```ts
import { test, expect } from '@playwright/test';

test('successful login lands on the home page', async ({ page }) => {
  await page.route('**/api/auth/login', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ id: 'u1', username: 'ada', roles: ['USER'] }),
    });
  });
  await page.route('**/api/auth/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ id: 'u1', username: 'ada', roles: ['USER'] }),
    });
  });

  await page.goto('/');
  await expect(page).toHaveURL(/\/login$/);

  await page.getByLabel(/username/i).fill('ada');
  await page.getByLabel(/password/i).fill('correct');
  await page.getByRole('button', { name: /sign in/i }).click();

  await expect(page).toHaveURL('/');
  await expect(page.getByRole('heading', { name: /welcome/i })).toBeVisible();
});

test('failed login shows server error', async ({ page }) => {
  await page.route('**/api/auth/login', async (route) => {
    await route.fulfill({
      status: 401,
      contentType: 'application/json',
      body: JSON.stringify({ message: 'Invalid credentials' }),
    });
  });

  await page.goto('/login');
  await page.getByLabel(/username/i).fill('ada');
  await page.getByLabel(/password/i).fill('wrong');
  await page.getByRole('button', { name: /sign in/i }).click();

  await expect(page.getByText(/invalid credentials/i)).toBeVisible();
});
```

- [ ] **Step 3: Install Playwright browsers**

Run: `cd frontend && npx playwright install chromium`
Expected: downloads Chromium; completes without error.

- [ ] **Step 4: Run E2E tests**

Run: `cd frontend && npm run test:e2e`
Expected: both specs pass. Playwright auto-starts Vite via `webServer`.

- [ ] **Step 5: Commit**

```bash
git add frontend/playwright.config.ts frontend/e2e
git commit -m "test(frontend): add Playwright config and login smoke tests"
```

---

## Task 15: Final polish — `ARCHITECTURE.md`, `README.md`, full verification

**Files:**
- Create: `frontend/ARCHITECTURE.md`, `frontend/README.md`

- [ ] **Step 1: Create `frontend/ARCHITECTURE.md`**

```markdown
# Frontend architecture

React + TypeScript SPA built with Vite. Talks to the Spring Boot backend via REST.

## Folder layout

```
src/
├── app/         # app-wide wiring: providers, layouts, route guards
├── features/    # vertical feature slices (auth/, events/, ...)
├── pages/       # thin pages that don't warrant a feature folder yet
├── shared/      # cross-feature code: ui/, lib/, hooks/, utils/, types/
├── styles/      # global.css + tokens.css (CSS custom properties)
├── test/        # Vitest setup, MSW server, render helpers
├── App.tsx      # composition root (providers + router)
├── main.tsx     # entrypoint
└── router.tsx   # route tree
```

## Where does my new code go?

Use this rule of thumb when adding a file:

1. **Used by one feature** → that feature's folder (`features/<name>/`).
2. **Used by two or more features** → `shared/`.
3. **Wires the whole app** (providers, layouts, guards, router) → `app/`.
4. **A page with real logic** → promote from `pages/` to `features/<name>/pages/`. Don't pre-split; refactor when the page grows.

Additional rules:

- Each feature exposes a single public barrel at `features/<name>/index.ts`. Everything else in that folder is internal.
- Siblings import relatively; everything else uses `@/` (the `src/` alias).
- A feature **never** imports from another feature's internals — only from its barrel or from `shared/`.
- CSS: every component has a sibling `Component.module.css`. Reference design tokens via CSS custom properties (see `styles/tokens.css`). Don't hard-code colors/spacing.

## State ownership

- **Server state** — TanStack Query. Never mirror server data into Zustand.
- **Client state** — Zustand. Example: auth session snapshot.
- **URL state** — React Router params and search params.

## API calls

All requests go through `src/shared/lib/api-client.ts`. Features never call `fetch` directly. Query keys are const factories co-located with the feature's API module.

## Testing

- `npm test` — Vitest watch mode.
- `npm test -- --run` — single run.
- `npm run test:e2e` — Playwright E2E (auto-starts Vite).
- Use `src/test/render.tsx` (`renderWithProviders`) for component tests that need routing or a QueryClient.
- Mock network calls with MSW handlers in `src/test/mocks/handlers.ts`.
```

- [ ] **Step 2: Create `frontend/README.md`**

```markdown
# Frontend

React + TypeScript SPA for the Event Ticket System. See `ARCHITECTURE.md` for structure and conventions.

## Prerequisites

- Node.js 20+ (22 recommended)
- npm 10+
- Backend running at `http://localhost:8080` (see the root `README.md`)

## Setup

```bash
cd frontend
npm install
cp .env.example .env   # then edit if needed
```

## Scripts

| Command              | Purpose                                               |
| -------------------- | ----------------------------------------------------- |
| `npm run dev`        | Dev server at http://localhost:5173 with API proxy    |
| `npm run build`      | Typecheck + production build to `dist/`               |
| `npm run preview`    | Preview the production build locally                  |
| `npm run lint`       | ESLint                                                |
| `npm run format`     | Prettier (writes)                                     |
| `npm run typecheck`  | `tsc -b --noEmit`                                     |
| `npm test`           | Vitest (watch)                                        |
| `npm test -- --run`  | Vitest (single run)                                   |
| `npm run test:ui`    | Vitest UI                                             |
| `npm run test:e2e`   | Playwright E2E                                        |

## API proxy

Vite proxies `/api/*` to `http://localhost:8080` in dev — configured in `vite.config.ts`. The app reads `VITE_API_BASE_URL` (defaults to `/api`) so production deployments can point at the real backend URL.
```

- [ ] **Step 3: Run every check**

Run: `cd frontend && npm run typecheck && npm run lint && npm test -- --run && npm run build`
Expected: all four commands exit 0; build produces `dist/`.

- [ ] **Step 4: Run the E2E smoke again to confirm nothing regressed**

Run: `cd frontend && npm run test:e2e`
Expected: both specs pass.

- [ ] **Step 5: Update the repo root `README.md` with a frontend pointer**

Add this block at the bottom of `README.md` (repo root):

```markdown
### Frontend

See `frontend/README.md`. Quick start:

```bash
cd frontend
npm install
npm run dev   # http://localhost:5173, proxies /api to the backend on :8080
```
```

- [ ] **Step 6: Commit**

```bash
git add frontend/ARCHITECTURE.md frontend/README.md README.md
git commit -m "docs(frontend): add architecture rules and README"
```

---

## Self-review notes

- Spec coverage: every section of the spec maps to a task — stack (Task 1–2), folder layout + hybrid rule (Task 3, 15), data flow + api client (Task 4), conventions in `ARCHITECTURE.md` (Task 15), auth skeleton (Tasks 6–9, 11, 12, 13), testing + tooling (Tasks 2, 7, 14), env var (Task 1).
- Type consistency: `User`, `LoginRequest`, `AuthStatus`, `ApiError`, `authKeys.me()` are defined once and referenced consistently across Tasks 6–13.
- No placeholders. Each step has exact file paths, complete code, and exact commands with expected output.
- `frontend/node_modules` is listed in `.gitignore` (Task 1, Step 8), so commits will not accidentally include it.
