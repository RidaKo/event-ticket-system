# Event ticket system

This project consists of two parts - backend and frontend. For backend, we use Java - Spring boot MVC.

## How to launch projects

### Backend
To run the backend project, IntelliJ is recommended. Open the backend folder - look for the file with shortcut Ctrl + Shift + N with the name *EventTicketSystemApplication*. Click on the file, then run the class itself - it should auto setup the configuration and this will do for the meantime, until we have more complex setups for different environments, etc.

The backend runs on `http://localhost:8080`. It serves APIs under `/api`. A quick API check is:

```text
http://localhost:8080/api/events/1
```

### Frontend
The frontend is a Vite + React application located in the `frontend` folder.

From the project root:

```bash
cd frontend
npm install
npm run dev
```

By default, the app runs on `http://localhost:5173`. Open `Your Orders` in the top navigation and choose `Checkout` to start the checkout flow.
