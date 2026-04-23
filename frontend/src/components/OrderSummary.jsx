import { formatMoney } from '../utils.js';

export default function OrderSummary({ summary, actionLabel, onAction, actionDisabled, footer }) {
  const empty = !summary || summary.items.length === 0;

  return (
    <section className="summary-card">
      <h2>Order Summary</h2>
      {empty ? (
        <p className="muted">No tickets selected</p>
      ) : (
        <>
          <div className="summary-lines">
            {summary.items.map((item) => (
              <div className="summary-line" key={item.ticketTypeId}>
                <span>{item.name} x {item.quantity}</span>
                <strong>{formatMoney(item.lineTotal)}</strong>
              </div>
            ))}
          </div>
          <div className="summary-totals">
            <div>
              <span>Subtotal</span>
              <strong>{formatMoney(summary.subtotal)}</strong>
            </div>
            {Number(summary.discountAmount) > 0 && (
              <div>
                <span>Discount ({summary.discountCode})</span>
                <strong>-{formatMoney(summary.discountAmount)}</strong>
              </div>
            )}
            <div className="total-row">
              <span>Total</span>
              <strong>{formatMoney(summary.total)}</strong>
            </div>
          </div>
        </>
      )}
      {actionLabel && (
        <button className="primary-button" type="button" onClick={onAction} disabled={actionDisabled}>
          {actionLabel}
        </button>
      )}
      {footer}
    </section>
  );
}
