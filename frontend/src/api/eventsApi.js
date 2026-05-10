import { apiFetch } from './client';

export function getEvent(eventId) {
  return apiFetch(`/events/${eventId}`);
}

export function getTicketTypes(eventId) {
  return apiFetch(`/events/${eventId}/ticket-types`);
}
