import { useEffect, useState } from 'react';
import { getConfirmation } from '../api/checkoutApi.js';
import { formatDateTime, formatMoney } from '../utils.js';

export default function ConfirmationPage({ orderNumber }) {
  const [confirmation, setConfirmation] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    getConfirmation(orderNumber)
      .then((data) => active && setConfirmation(data))
      .catch((err) => active && setError(err.message));
    return () => {
      active = false;
    };
  }, [orderNumber]);

  if (error) {
    return <main className="confirmation-shell"><p className="error-message">{error}</p></main>;
  }

  if (!confirmation) {
    return <main className="confirmation-shell"><p className="muted">Loading confirmation...</p></main>;
  }

  return (
    <main className="confirmation-shell">
      <section className="confirmation-header">
        <div className="success-mark">✓</div>
        <h1>Order Confirmed</h1>
        <p>Confirmation for {confirmation.guestEmail}</p>
      </section>

      <section className="detail-section detail-grid">
        <div>
          <span>Order Number</span>
          <strong>{confirmation.orderNumber}</strong>
        </div>
        <div>
          <span>Order Date</span>
          <strong>{formatDateTime(confirmation.confirmedAt)}</strong>
        </div>
        <div>
          <span>Email</span>
          <strong>{confirmation.guestEmail}</strong>
        </div>
      </section>

      <section className="detail-section">
        <h2>Event Details</h2>
        <strong>{confirmation.event.title}</strong>
        <p>{formatDateTime(confirmation.event.startsAt)}</p>
        <p>{confirmation.event.venueName}</p>
        <p>{confirmation.event.address}, {confirmation.event.city}</p>
      </section>

      <section className="detail-section">
        <h2>Ticket Details</h2>
        <div className="confirmation-lines">
          {confirmation.summary.items.map((item) => (
            <div key={item.ticketTypeId}>
              <span>
                <strong>{item.name}</strong>
                <small>Quantity: {item.quantity}</small>
              </span>
              <strong>{formatMoney(item.lineTotal)}</strong>
            </div>
          ))}
        </div>
      </section>

      <section className="detail-section">
        <h2>Payment Information</h2>
        <p>{confirmation.paymentMethod} {confirmation.cardLast4 ? `•••• ${confirmation.cardLast4}` : ''}</p>
      </section>

      <section className="detail-section">
        <h2>Order Summary</h2>
        <div className="summary-totals">
          <div><span>Subtotal</span><strong>{formatMoney(confirmation.summary.subtotal)}</strong></div>
          {Number(confirmation.summary.discountAmount) > 0 && (
            <div><span>Discount ({confirmation.summary.discountCode})</span><strong>-{formatMoney(confirmation.summary.discountAmount)}</strong></div>
          )}
          <div className="total-row"><span>Total Paid</span><strong>{formatMoney(confirmation.summary.total)}</strong></div>
        </div>
      </section>
    </main>
  );
}
