# Frontend

React + Mantine browse and checkout UI for the Event Ticket System.

## Stack

- React 18 (JSX)
- Mantine 7
- Vite 8

Entry point: `src/main.jsx` → `src/App.jsx`

## Prerequisites

- Node.js 20+
- npm 10+
- Backend at `http://localhost:8080` (see root `README.md`)

## Setup

```bash
cd frontend
npm install
cp .env.example .env   # optional
```

## Scripts

| Command           | Purpose                                            |
| ----------------- | -------------------------------------------------- |
| `npm run dev`     | Dev server at http://localhost:5173 (proxies `/api`) |
| `npm run build`   | Production build to `dist/`                        |
| `npm run preview` | Preview the production build                       |

## API

- Dev: Vite proxies `/api/*` to `http://localhost:8080` (`vite.config.js`)
- Base URL: `VITE_API_BASE_URL` (defaults to `/api`)

## Routes

- `/` — browse events and recommendations
- `/events/:id` — event details
- `/events/:id/checkout/tickets` — ticket selection
- `/checkout/:orderNumber/payment` — payment
- `/checkout/:orderNumber/confirmation` — confirmation
- `/orders` — orders stub
