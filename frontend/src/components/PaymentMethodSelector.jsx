export default function PaymentMethodSelector({ value, onChange }) {
  return (
    <div className="method-grid">
      <label className={`method-card ${value === 'CARD' ? 'is-selected' : ''}`}>
        <input
          type="radio"
          checked={value === 'CARD'}
          onChange={() => onChange('CARD')}
        />
        <span>
          <strong>Credit / Debit Card</strong>
          <small>Visa, Mastercard, Amex</small>
        </span>
      </label>
      <label className={`method-card ${value === 'WALLET' ? 'is-selected' : ''}`}>
        <input
          type="radio"
          checked={value === 'WALLET'}
          onChange={() => onChange('WALLET')}
        />
        <span>
          <strong>Apple Pay / Google Pay</strong>
          <small>Simulated wallet payment</small>
        </span>
      </label>
    </div>
  );
}
