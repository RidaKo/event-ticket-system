# Event ticket system

This project consists of two parts - backend and frontend. For backend, we use Java - Spring Boot MVC.

## How to launch projects

### Backend

To run the backend project, IntelliJ is recommended. Open the backend folder and look for the file with shortcut Ctrl + Shift + N with the name *EventTicketSystemApplication*. Click on the file, then run the class itself. It should auto setup the configuration for now, until we have more complex setups for different environments.

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

#### Seed accounts (with the `seed` profile)

With the `seed` profile, the SQL seed script creates two pre-seeded login accounts:

| Email | Password | Role |
| --- | --- | --- |
| `admin@test.com` | `admin123` | `ADMIN` |
| `organizer@test.com` | `organizer123` | `ORGANIZER` |

Log in via `POST /api/auth/login` or the Swagger UI at `http://localhost:8080/swagger-ui.html`. In Swagger, click the green **Authorize** button after logging in and paste the returned `token`.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@test.com","password":"admin123"}'
```

Without the `seed` profile, neither account exists. Register a normal user via `POST /api/auth/register` instead. The public registration endpoint always creates `USER`-role accounts.

#### Email delivery setup

Purchase confirmation emails are sent after a successful checkout payment. By default, the backend uses log mode, so emails are written to the application logs instead of being sent through SMTP:

```yaml
event-ticket:
  email:
    sender: log
```

To send real emails locally, create this ignored file:

```text
backend/src/main/resources/application-local.yaml
```

Example Gmail SMTP config:

```yaml
event-ticket:
  email:
    sender: smtp
    from-address: your-email@gmail.com

spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: "your-gmail-app-password-without-spaces"
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

Do not commit `application-local.yaml`. It is listed in `.gitignore` because it contains local credentials. Gmail requires a 16-character app password, not the normal account password.

### Frontend

The frontend is a Vite + React application located in the `frontend` folder.

From the project root:

```bash
cd frontend
npm install
npm run dev
```

By default, the app runs on `http://localhost:5173`. Open `Your Orders` in the top navigation and choose `Checkout` to start the checkout flow.
