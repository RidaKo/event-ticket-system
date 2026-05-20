import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";

const CheckoutContext = createContext(null);
const checkoutDraftStorageKey = "eventTicket.checkoutDraft";
const guestStorageKey = "eventTicket.checkoutGuest";
const orderTokensStorageKey = "eventTicket.orderTokens";

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
    readStoredValue(guestStorageKey, { guestName: "", guestEmail: "", guestPhone: "" })
  );
  const [checkoutDraft, setCheckoutDraft] = useState(() => readStoredValue(checkoutDraftStorageKey, null));
  const [orderTokens, setOrderTokens] = useState(() => readStoredValue(orderTokensStorageKey, {}));

  useEffect(() => {
    writeStoredValue(guestStorageKey, guest);
  }, [guest]);

  useEffect(() => {
    writeStoredValue(checkoutDraftStorageKey, checkoutDraft);
  }, [checkoutDraft]);

  useEffect(() => {
    writeStoredValue(orderTokensStorageKey, orderTokens);
  }, [orderTokens]);

  const rememberOrderToken = useCallback((orderNumber, orderToken) => {
    if (!orderNumber || !orderToken) {
      return;
    }
    setOrderTokens((current) => ({
      ...current,
      [orderNumber]: orderToken,
    }));
  }, []);

  const getOrderToken = useCallback(
    (orderNumber) => orderTokens?.[orderNumber] || null,
    [orderTokens]
  );

  const value = useMemo(
    () => ({
      guest,
      updateGuest: setGuest,
      checkoutDraft,
      setCheckoutDraft,
      clearCheckoutDraft: () => setCheckoutDraft(null),
      rememberOrderToken,
      getOrderToken,
    }),
    [checkoutDraft, getOrderToken, guest, rememberOrderToken]
  );

  return <CheckoutContext.Provider value={value}>{children}</CheckoutContext.Provider>;
}

export function useCheckout() {
  return useContext(CheckoutContext);
}
