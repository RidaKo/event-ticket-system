import { createContext, useContext, useMemo, useState } from "react";

const CheckoutContext = createContext(null);

export function CheckoutProvider({ children }) {
  const [guest, setGuest] = useState({ guestName: "", guestEmail: "" });

  const value = useMemo(
    () => ({
      guest,
      updateGuest: setGuest,
    }),
    [guest]
  );

  return <CheckoutContext.Provider value={value}>{children}</CheckoutContext.Provider>;
}

export function useCheckout() {
  return useContext(CheckoutContext);
}
