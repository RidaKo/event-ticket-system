import { useState } from 'react';

export default function DiscountCodeInput({ value, appliedCode, onApply, disabled }) {
  const [code, setCode] = useState(value || '');

  return (
    <form className="discount-form" onSubmit={(event) => {
      event.preventDefault();
      onApply(code.trim());
    }}>
      <label htmlFor="discountCode">Promo Code</label>
      <div className="inline-form-row">
        <input
          id="discountCode"
          value={code}
          onChange={(event) => setCode(event.target.value)}
          placeholder="SAVE10"
          disabled={disabled}
        />
        <button type="submit" disabled={disabled}>Apply</button>
      </div>
      {appliedCode && <p className="applied-code">✓ {appliedCode} applied</p>}
    </form>
  );
}
