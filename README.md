# Event ticket system

This project consists of two parts - backend and frontend. For backend, we use Java - Spring boot MVC.

## How to launch projects

### Backend
To run the backend project, IntelliJ is recommended. Open the backend folder - look for the file with shortcut Ctrl + Shift + N with the name *EventTicketSystemApplication*. Click on the file, then run the class itself - it should auto setup the configuration and this will do for the meantime, until we have more complex setups for different environments, etc.

### Frontend
See `frontend/README.md`. Quick start:

```bash
cd frontend
npm install
npm run dev   # http://localhost:5173, proxies /api to the backend on :8080
```
