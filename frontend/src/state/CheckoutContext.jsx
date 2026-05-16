import { createContext, useContext, useEffect, useMemo, useState } from "react";

const CheckoutContext = createContext(null);
const checkoutDraftStorageKey = "eventTicket.checkoutDraft";
const guestStorageKey = "eventTicket.checkoutGuest";

function readStoredValue(key, fallback) {
  if (typeof window === "undefined") {
    return fallback;
  }

  try {
    const value = window.sessionStorage.getItem(key);
    return value ? JSON.parse(value) : fallback;
  } catch {
    return fallback;
  }
}

function writeStoredValue(key, value) {
  if (typeof window === "undefined") {
    return;
  }

  if (value == null) {
    window.sessionStorage.removeItem(key);
    return;
  }

  window.sessionStorage.setItem(key, JSON.stringify(value));
}

export function CheckoutProvider({ children }) {
  const [guest, setGuest] = useState(() =>
    readStoredValue(guestStorageKey, { guestName: "", guestEmail: "" })
  );
  const [checkoutDraft, setCheckoutDraft] = useState(() => readStoredValue(checkoutDraftStorageKey, null));

  useEffect(() => {
    writeStoredValue(guestStorageKey, guest);
  }, [guest]);

  useEffect(() => {
    writeStoredValue(checkoutDraftStorageKey, checkoutDraft);
  }, [checkoutDraft]);

  const value = useMemo(
    () => ({
      guest,
      updateGuest: setGuest,
      checkoutDraft,
      setCheckoutDraft,
      clearCheckoutDraft: () => setCheckoutDraft(null),
    }),
    [checkoutDraft, guest]
  );

  return <CheckoutContext.Provider value={value}>{children}</CheckoutContext.Provider>;
}

export function useCheckout() {
  return useContext(CheckoutContext);
}
