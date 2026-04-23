import { useEffect, useState } from 'react';
import { getOrder, submitPayment } from '../api/checkoutApi.js';
import CheckoutStepLayout from '../components/CheckoutStepLayout.jsx';
import OrderSummary from '../components/OrderSummary.jsx';
import PaymentMethodSelector from '../components/PaymentMethodSelector.jsx';

export default function PaymentPage({ orderNumber, navigate }) {
  const [order, setOrder] = useState(null);
  const [methodType, setMethodType] = useState('CARD');
  const [cardNumber, setCardNumber] = useState('4242 4242 4242 4242');
  const [nameOnCard, setNameOnCard] = useState('');
  const [expiry, setExpiry] = useState('12/28');
  const [cvv, setCvv] = useState('123');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    getOrder(orderNumber)
      .then((data) => {
        if (!active) return;
        setOrder(data);
        setNameOnCard(data.guestName || '');
      })
      .catch((err) => active && setError(err.message))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [orderNumber]);

  async function payNow(event) {
    event.preventDefault();
    if (methodType === 'CARD' && cardNumber.replace(/\D/g, '').length < 12) {
      setError('Enter a valid card number');
      return;
    }

    setSubmitting(true);
    setError('');
    try {
      const result = await submitPayment(orderNumber, {
        methodType,
        cardNumber: methodType === 'CARD' ? cardNumber : '5555 5555 5555 4444'
      });
      if (result.orderStatus === 'CONFIRMED') {
        navigate(`/checkout/${orderNumber}/confirmation`);
        return;
      }
      setError('Payment failed');
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  const sidebar = (
    <>
      <OrderSummary
        summary={order?.summary}
        actionLabel="Pay Now"
        onAction={() => document.getElementById('paymentForm')?.requestSubmit()}
        actionDisabled={submitting || loading || !order}
      />
      {error && <p className="error-message">{error}</p>}
    </>
  );

  return (
    <CheckoutStepLayout title="Payment" sidebar={sidebar}>
      {loading && <p className="muted">Loading order...</p>}
      {order && (
        <form id="paymentForm" className="payment-form" onSubmit={payNow}>
          <section className="form-section">
            <h2>Payment Method</h2>
            <PaymentMethodSelector value={methodType} onChange={setMethodType} />
          </section>

          {methodType === 'CARD' && (
            <section className="form-section">
              <h2>Card Details</h2>
              <label htmlFor="cardNumber">Card Number</label>
              <input id="cardNumber" value={cardNumber} onChange={(e) => setCardNumber(e.target.value)} />
              <label htmlFor="nameOnCard">Name on Card</label>
              <input id="nameOnCard" value={nameOnCard} onChange={(e) => setNameOnCard(e.target.value)} />
              <div className="two-column">
                <label>
                  Expiration Date
                  <input value={expiry} onChange={(e) => setExpiry(e.target.value)} />
                </label>
                <label>
                  CVV
                  <input value={cvv} onChange={(e) => setCvv(e.target.value)} />
                </label>
              </div>
            </section>
          )}
        </form>
      )}
    </CheckoutStepLayout>
  );
}
