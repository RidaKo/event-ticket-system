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
