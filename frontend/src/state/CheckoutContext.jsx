import { createContext, useContext, useMemo, useState } from 'react';

const CheckoutContext = createContext(null);

export function CheckoutProvider({ children }) {
  const [guest, setGuest] = useState(() => {
    const saved = localStorage.getItem('checkoutGuest');
    return saved ? JSON.parse(saved) : { guestName: '', guestEmail: '' };
  });

  function updateGuest(nextGuest) {
    setGuest(nextGuest);
    localStorage.setItem('checkoutGuest', JSON.stringify(nextGuest));
  }

  const value = useMemo(() => ({ guest, updateGuest }), [guest]);
  return <CheckoutContext.Provider value={value}>{children}</CheckoutContext.Provider>;
}

export function useCheckout() {
  return useContext(CheckoutContext);
}
