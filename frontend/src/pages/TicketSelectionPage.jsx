import { useEffect, useMemo, useState } from 'react';
import { createOrder, quoteCheckout } from '../api/checkoutApi.js';
import { getEvent, getTicketTypes } from '../api/eventsApi.js';
import CheckoutStepLayout from '../components/CheckoutStepLayout.jsx';
import DiscountCodeInput from '../components/DiscountCodeInput.jsx';
import OrderSummary from '../components/OrderSummary.jsx';
import TicketQuantitySelector from '../components/TicketQuantitySelector.jsx';
import { useCheckout } from '../state/CheckoutContext.jsx';

export default function TicketSelectionPage({ eventId, navigate }) {
  const { guest, updateGuest } = useCheckout();
  const [event, setEvent] = useState(null);
  const [tickets, setTickets] = useState([]);
  const [quantities, setQuantities] = useState({});
  const [discountCode, setDiscountCode] = useState('');
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const selectedItems = useMemo(() => Object.entries(quantities)
    .filter(([, quantity]) => quantity > 0)
    .map(([ticketTypeId, quantity]) => ({ ticketTypeId: Number(ticketTypeId), quantity })), [quantities]);

  useEffect(() => {
    let active = true;
    Promise.all([getEvent(eventId), getTicketTypes(eventId)])
      .then(([eventData, ticketData]) => {
        if (!active) return;
        setEvent(eventData);
        setTickets(ticketData);
      })
      .catch((err) => active && setError(err.message))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [eventId]);

  useEffect(() => {
    if (selectedItems.length === 0) {
      setSummary(null);
      return;
    }

    let active = true;
    quoteCheckout({ eventId, items: selectedItems, discountCode: discountCode || null })
      .then((quotedSummary) => {
        if (!active) return;
        setSummary(quotedSummary);
        setError('');
      })
      .catch((err) => {
        if (!active) return;
        setSummary(null);
        setError(err.message);
      });
    return () => {
      active = false;
    };
  }, [eventId, selectedItems, discountCode]);

  function updateQuantity(ticketId, quantity) {
    setQuantities((current) => ({ ...current, [ticketId]: quantity }));
  }

  async function continueToPayment() {
    if (!summary || selectedItems.length === 0) {
      setError('Select at least one ticket');
      return;
    }
    if (!guest.guestEmail.trim()) {
      setError('Email address is required');
      return;
    }

    setSubmitting(true);
    setError('');
    try {
      const order = await createOrder({
        eventId,
        guestName: guest.guestName.trim() || null,
        guestEmail: guest.guestEmail.trim(),
        discountCode: summary.discountCode,
        items: selectedItems
      });
      navigate(`/checkout/${order.orderNumber}/payment`);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  const sidebar = (
    <>
      <OrderSummary
        summary={summary}
        actionLabel="Continue to Payment"
        onAction={continueToPayment}
        actionDisabled={submitting || !summary || selectedItems.length === 0}
        footer={
          <>
            <DiscountCodeInput
              value={discountCode}
              appliedCode={summary?.discountCode}
              onApply={setDiscountCode}
              disabled={selectedItems.length === 0}
            />
            <div className="contact-form">
              <label htmlFor="guestName">Name</label>
              <input
                id="guestName"
                value={guest.guestName}
                onChange={(e) => updateGuest({ ...guest, guestName: e.target.value })}
                placeholder="John Doe"
              />
              <label htmlFor="guestEmail">Email</label>
              <input
                id="guestEmail"
                type="email"
                value={guest.guestEmail}
                onChange={(e) => updateGuest({ ...guest, guestEmail: e.target.value })}
                placeholder="john.doe@email.com"
              />
            </div>
          </>
        }
      />
      {error && <p className="error-message">{error}</p>}
    </>
  );

  return (
    <CheckoutStepLayout title="Ticket Selection" sidebar={sidebar}>
      {loading && <p className="muted">Loading tickets...</p>}
      {event && (
        <div className="event-strip">
          <strong>{event.title}</strong>
          <span>{event.venueName}</span>
        </div>
      )}
      <div className="ticket-list">
        {tickets.map((ticket) => (
          <TicketQuantitySelector
            key={ticket.id}
            ticket={ticket}
            quantity={quantities[ticket.id] || 0}
            onChange={(quantity) => updateQuantity(ticket.id, quantity)}
          />
        ))}
      </div>
    </CheckoutStepLayout>
  );
}
