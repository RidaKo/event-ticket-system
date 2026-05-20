import { apiFetch } from './client';

export function quoteCheckout(payload) {
  return apiFetch('/checkout/quote', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function createOrder(payload) {
  return apiFetch('/checkout/orders', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function getOrder(orderNumber) {
  return apiFetch(`/checkout/orders/${orderNumber}`);
}

export function applyDiscount(orderNumber, discountCode) {
  return apiFetch(`/checkout/orders/${orderNumber}/discount`, {
    method: 'POST',
    body: JSON.stringify({ discountCode })
  });
}

export function submitPayment(orderNumber, payload) {
  return apiFetch(`/checkout/orders/${orderNumber}/payment`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function getConfirmation(orderNumber) {
  return apiFetch(`/checkout/orders/${orderNumber}/confirmation`);
}
