import { apiFetch } from './client';

export function getUserOrders({ page = 0, size = 6 } = {}) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size)
  });

  return apiFetch(`/checkout/orders?${params.toString()}`);
}
