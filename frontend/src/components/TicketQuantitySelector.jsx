import { formatMoney } from '../utils.js';

export default function TicketQuantitySelector({ ticket, quantity, onChange }) {
  const soldOut = !ticket.salesEnabled || ticket.availableQuantity <= 0;

  return (
    <article className={`ticket-card ${soldOut ? 'is-disabled' : ''}`}>
      <div className="ticket-icon" aria-hidden="true">▣</div>
      <div className="ticket-body">
        <div className="ticket-heading">
          <h2>{ticket.name}</h2>
          <span>{soldOut ? 'Sold out' : `${ticket.availableQuantity} left`}</span>
        </div>
        <p>{formatMoney(ticket.price)}</p>
        <div className="quantity-control">
          <button type="button" onClick={() => onChange(Math.max(0, quantity - 1))} disabled={quantity === 0}>
            −
          </button>
          <span>{quantity}</span>
          <button type="button" onClick={() => onChange(quantity + 1)} disabled={soldOut || quantity >= ticket.availableQuantity}>
            +
          </button>
        </div>
      </div>
    </article>
  );
}
