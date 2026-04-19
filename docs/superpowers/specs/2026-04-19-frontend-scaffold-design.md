# Frontend scaffold — design

**Date:** 2026-04-19
**Scope:** Stand up the `frontend/` folder as a React + TypeScript SPA that talks to the existing Spring Boot backend. First iteration includes an auth skeleton; no business features yet.
**Non-goals:** Implementing real auth against the backend, building events/tickets UIs, CI pipelines, deployment.

## 1. Stack

| Concern | Choice | Notes |
|---|---|---|
| Build tool | Vite 6 | SPA dev server + build |
| Language | React 19 + TypeScript (strict) | |
| Styling | CSS Modules | One `.module.css` per component. Designs will come from Figma. |
| Server state | TanStack Query v5 | Caching, loading/error, invalidation |
| HTTP | Native `fetch` (wrapped) | No axios — smaller dep surface |
| Client state | Zustand | Auth slice + UI slice only |
| Routing | React Router v7 (data-router mode) | |
| Forms | React Hook Form + Zod | Needed by auth; type-safe schemas |
| Testing | Vitest + React Testing Library + Playwright | Unit/component + E2E |
| Mocking | MSW (Mock Service Worker) | Shared between Vitest and Playwright |
| Lint/format | ESLint (flat config) + Prettier | |
| Package manager | npm | No external tooling required |
| Dev backend link | Vite proxy `/api` → `http://localhost:8080` | Matches Spring Boot default port |

## 2. Folder layout

Hybrid: layered at the top, with a `features/` folder for vertical slices. Path alias `@/` → `src/`.

```
frontend/
├── public/
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── router.tsx
│   │
│   ├── app/                      # app-wide wiring
│   │   ├── providers/            # QueryProvider, AuthProvider
│   │   ├── layouts/              # RootLayout, AuthLayout
│   │   └── guards/               # ProtectedRoute, RoleRoute
│   │
│   ├── features/                 # vertical slices
│   │   └── auth/
│   │       ├── api/              # auth.api.ts
│   │       ├── components/       # LoginForm + .module.css
│   │       ├── hooks/            # useAuth, useCurrentUser, useLogin, useLogout
│   │       ├── pages/            # LoginPage.tsx
│   │       ├── store/            # auth.store.ts (Zustand)
│   │       ├── types.ts
│   │       └── index.ts          # public barrel
│   │
│   ├── pages/                    # simple pages not worth a feature folder yet
│   │   └── HomePage.tsx
│   │
│   ├── shared/                   # cross-feature reusable code
│   │   ├── ui/                   # Button, Input, Modal (pure presentation)
│   │   ├── hooks/
│   │   ├── lib/                  # api-client.ts, query-keys, errors
│   │   ├── utils/
│   │   └── types/                # ApiError, Paginated<T>
│   │
│   ├── styles/
│   │   ├── tokens.css            # CSS custom properties (colors, spacing, typography)
│   │   └── global.css            # resets, base element styles
│   │
│   └── test/
│       ├── setup.ts              # Vitest setup; jest-dom + MSW server
│       └── mocks/handlers.ts     # MSW request handlers
│
├── e2e/                          # Playwright specs
├── index.html
├── vite.config.ts
├── tsconfig.json / tsconfig.node.json
├── eslint.config.js
├── .prettierrc
├── .env / .env.example           # VITE_API_BASE_URL
├── ARCHITECTURE.md               # repeats the rule-of-thumb below for developers
└── package.json
```

### 2.1 Where code goes — the hybrid rule

This rule is committed to `frontend/ARCHITECTURE.md` so it is visible to anyone opening the folder.

1. **Used by one feature** → that feature's folder.
2. **Used by two or more features** → `src/shared/`.
3. **Wires the whole app** (providers, layouts, guards, router) → `src/app/`.
4. **A page with real logic** → promote from `src/pages/` to `src/features/<name>/pages/`. Don't pre-split; refactor when the page grows.

Additional conventions:
- Each feature exposes a single public barrel `features/<name>/index.ts`. Everything else is internal.
- Siblings import relatively; everything else uses `@/`.
- A feature never imports from another feature's internals — only from its barrel or from `shared/`.

## 3. Data flow & conventions

### 3.1 Request path

```
Component → feature hook (e.g., useCurrentUser)
          → TanStack Query (queryFn)
          → apiClient.get('/api/auth/me')
          → fetch wrapper (adds credentials, parses JSON, normalizes errors)
          → Spring Boot (via Vite proxy in dev)
```

### 3.2 API client

`src/shared/lib/api-client.ts` — single `fetch` wrapper. Responsibilities:
- Prefix requests with `VITE_API_BASE_URL` (defaults to `/api`).
- `credentials: 'include'` so Spring Session cookies flow.
- Parse JSON; throw `ApiError` (shape: `{ status, message, details? }`) on non-2xx.
- Expose `get`, `post`, `put`, `delete` helpers.

No feature ever calls `fetch` directly.

### 3.3 Query keys

Per-feature const factories, co-located with the feature's API module. Example:

```ts
// features/auth/api/auth.keys.ts
export const authKeys = {
  all: ['auth'] as const,
  me:  () => [...authKeys.all, 'me'] as const,
};
```

Prevents typo-driven cache misses and makes invalidation explicit.

### 3.4 State split

- **Server state** → TanStack Query. Never mirrored into Zustand.
- **Client state** → Zustand. Auth slice holds `{ user, status }` as a snapshot written on login success; does not re-fetch.
- **Route guards** read the auth store; they don't fetch.

### 3.5 Styling

- Every component has a sibling `Component.module.css`.
- Design tokens are CSS custom properties in `src/styles/tokens.css` (e.g., `--color-primary`, `--space-4`, `--font-body`).
- Modules reference tokens via `var(--…)`. When a Figma design changes a token, it propagates without touching components.

## 4. Auth skeleton

Endpoints targeted (not yet implemented on the backend — 404s in dev are expected):

| Function | Method | Path |
|---|---|---|
| `login({ username, password })` | POST | `/api/auth/login` |
| `logout()` | POST | `/api/auth/logout` |
| `fetchCurrentUser()` | GET  | `/api/auth/me` |

**Zustand auth store** (`features/auth/store/auth.store.ts`):

```ts
{
  user: User | null,
  status: 'idle' | 'authenticated' | 'unauthenticated',
  setUser(user: User): void,
  clear(): void,
}
```

Persisted to `localStorage` via `zustand/middleware/persist`. Source of truth for the session remains the server cookie; the persisted store is a hint for first paint.

**Hooks** (`features/auth/hooks/useAuth.ts`):
- `useCurrentUser()` → `useQuery(authKeys.me(), fetchCurrentUser, { retry: false })`.
- `useLogin()` → `useMutation` that on success writes the user to the auth store and invalidates `authKeys.me()`.
- `useLogout()` → mutation that calls `queryClient.clear()` and `authStore.clear()`.

**Guard** (`app/guards/ProtectedRoute.tsx`): reads the store; if unauthenticated, `<Navigate to="/login" replace />`. Role-based variant (`<RoleRoute allow={['ORGANIZER']} />`) is a future extension, not built now.

**Login page** (`features/auth/pages/LoginPage.tsx`): React Hook Form + Zod schema. Submits via `useLogin().mutate`. Displays `ApiError.message` on failure.

**Routes**:
- `/login` → `LoginPage` inside `AuthLayout`.
- `/` → `HomePage` inside `RootLayout`, wrapped by `ProtectedRoute`.

## 5. Testing & tooling

### 5.1 Testing layout

- Unit/component tests co-located as `Component.test.tsx` beside the code.
- `src/test/setup.ts` wires `@testing-library/jest-dom` and starts the MSW server.
- MSW handlers in `src/test/mocks/handlers.ts` — stubs for the three auth endpoints.
- Playwright specs in `e2e/`. One smoke test: `login.spec.ts` — loads `/login`, logs in against a mocked route, lands on `/`.

### 5.2 npm scripts

```
dev         vite
build       tsc -b && vite build
preview     vite preview
lint        eslint .
format      prettier --write .
test        vitest
test:ui     vitest --ui
test:e2e    playwright test
typecheck   tsc -b --noEmit
```

### 5.3 `vite.config.ts` essentials

- `@/` alias → `src/`.
- Dev proxy: `/api` → `http://localhost:8080` with `changeOrigin: true`.
- Vitest config inline: `{ environment: 'jsdom', setupFiles: ['./src/test/setup.ts'] }`.

### 5.4 Env

- `.env.example` committed.
- `VITE_API_BASE_URL` — defaults to `/api` in dev (through the Vite proxy). Swapped for the deployed backend URL in production.

## 6. Out of scope for this scaffold

- Real authentication against the Spring Boot backend (endpoints don't exist yet).
- Events, tickets, organizers, admin flows.
- Design system build-out beyond token placeholders.
- CI pipeline, Docker, deployment.
- Internationalization.

These will be separate specs layered on top of this scaffold.
