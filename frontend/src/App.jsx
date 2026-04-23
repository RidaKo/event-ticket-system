import { useEffect, useState } from 'react';
import { CheckoutProvider } from './state/CheckoutContext.jsx';
import TicketSelectionPage from './pages/TicketSelectionPage.jsx';
import PaymentPage from './pages/PaymentPage.jsx';
import ConfirmationPage from './pages/ConfirmationPage.jsx';

function readRoute() {
  const path = window.location.pathname;
  const payment = path.match(/^\/checkout\/([^/]+)\/payment$/);
  if (payment) {
    return { name: 'payment', orderNumber: payment[1] };
  }

  const confirmation = path.match(/^\/checkout\/([^/]+)\/confirmation$/);
  if (confirmation) {
    return { name: 'confirmation', orderNumber: confirmation[1] };
  }

  const tickets = path.match(/^\/events\/(\d+)\/checkout\/tickets$/);
  return { name: 'tickets', eventId: tickets ? Number(tickets[1]) : 1 };
}

export default function App() {
  const [route, setRoute] = useState(readRoute);

  useEffect(() => {
    const onPopState = () => setRoute(readRoute());
    window.addEventListener('popstate', onPopState);
    return () => window.removeEventListener('popstate', onPopState);
  }, []);

  function navigate(path) {
    window.history.pushState(null, '', path);
    setRoute(readRoute());
  }

  return (
    <CheckoutProvider>
      {route.name === 'payment' && <PaymentPage orderNumber={route.orderNumber} navigate={navigate} />}
      {route.name === 'confirmation' && <ConfirmationPage orderNumber={route.orderNumber} />}
      {route.name === 'tickets' && <TicketSelectionPage eventId={route.eventId} navigate={navigate} />}
    </CheckoutProvider>
  );
}
