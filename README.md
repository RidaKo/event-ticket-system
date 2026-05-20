# Event ticket system

This project consists of two parts - backend and frontend. For backend, we use Java - Spring boot MVC.

## How to launch projects

### Backend
To run the backend project, IntelliJ is recommended. Open the backend folder - look for the file with shortcut Ctrl + Shift + N with the name *EventTicketSystemApplication*. Click on the file, then run the class itself - it should auto setup the configuration and this will do for the meantime, until we have more complex setups for different environments, etc.

The backend runs on `http://localhost:8080`. It serves APIs under `/api`. A quick API check is:

```text
http://localhost:8080/api/events/1
```

#### Seed data (local default)
Local runs use the `local` profile group, which includes `seed` and loads demo data from `db/seed/R__seed_data.sql` (checkout sample, recommendation tags/events, demo user).

To start **without** demo data (schema only), set:

```bash
SPRING_PROFILES_ACTIVE=
```

or pass `--spring.profiles.active=` to Gradle.

#### Seed accounts (only available with the `seed` profile)

On startup with the `seed` profile, the `SeedAccountInitializer` rewrites the placeholder password hashes for two pre-seeded login accounts:

| Email | Password | Role |
| --- | --- | --- |
| `admin@test.com` | `admin123` | `ADMIN` |
| `organizer@test.com` | `organizer123` | `ORGANIZER` |

Log in via `POST /api/auth/login` (or the Swagger UI at `http://localhost:8080/swagger-ui.html` — click the green **Authorize** button after logging in and paste the returned `token`):

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@test.com","password":"admin123"}'
```

Without the `seed` profile, neither account exists — register a normal user via `POST /api/auth/register` instead (the public registration endpoint always creates `USER`-role accounts).

### Frontend
The frontend is a Vite + React application located in the `frontend` folder.

From the project root:

```bash
cd frontend
npm install
npm run dev
```

By default, the app runs on `http://localhost:5173`. Open `Your Orders` in the top navigation and choose `Checkout` to start the checkout flow.
