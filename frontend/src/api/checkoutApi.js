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

export function createGuestOrder(payload) {
  return apiFetch('/checkout/orders/guest', {
    method: 'POST',
    skipAuth: true,
    body: JSON.stringify(payload)
  });
}

export function getOrder(orderNumber, orderToken) {
  return apiFetch(`/checkout/orders/${orderNumber}`, { orderToken });
}

export function applyDiscount(orderNumber, discountCode, orderToken) {
  return apiFetch(`/checkout/orders/${orderNumber}/discount`, {
    method: 'POST',
    orderToken,
    body: JSON.stringify({ discountCode })
  });
}

export function submitPayment(orderNumber, payload, orderToken) {
  return apiFetch(`/checkout/orders/${orderNumber}/payment`, {
    method: 'POST',
    orderToken,
    body: JSON.stringify(payload)
  });
}

export function getPaymentStatus(orderNumber, orderToken) {
  return apiFetch(`/checkout/orders/${orderNumber}/payment`, { orderToken });
}

export function getConfirmation(orderNumber, orderToken) {
  return apiFetch(`/checkout/orders/${orderNumber}/confirmation`, { orderToken });
}
