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
