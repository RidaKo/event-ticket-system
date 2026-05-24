import { apiFetch } from './client';

export function getUserOrders() {
  return apiFetch('/checkout/orders');
}
