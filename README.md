# Event ticket system

This project consists of two parts - backend and frontend. For backend, we use Java - Spring boot MVC.

## How to launch projects

### Backend
To run the backend project, IntelliJ is recommended. Open the backend folder - look for the file with shortcut Ctrl + Shift + N with the name *EventTicketSystemApplication*. Click on the file, then run the class itself - it should auto setup the configuration and this will do for the meantime, until we have more complex setups for different environments, etc.

The backend runs on `http://localhost:8080`. It only serves APIs, not the React app. A quick API check is:

```text
http://localhost:8080/api/events/1
```

### Frontend
The checkout UI is a separate React/Vite app. Start it from the `frontend` folder:

```bash
npm install
npm run dev
```

Then open:

```text
http://127.0.0.1:5173/events/1/checkout/tickets
```

In IntelliJ, create a separate npm run configuration with:

```text
package.json: frontend/package.json
command: run
scripts: dev
```
