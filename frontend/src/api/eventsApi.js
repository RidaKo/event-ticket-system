import { apiFetch } from './client';

export function getEvent(eventId) {
  return apiFetch(`/events/${eventId}`);
}

export function getTicketTypes(eventId) {
  return apiFetch(`/events/${eventId}/ticket-types`);
}

export function createEvent(eventData) {
  return apiFetch('/events', {
    method: 'POST',
    body: JSON.stringify(eventData),
    headers: {
      'Content-Type': 'application/json',
    },
  });
}
export function getEventsByOrganizerId(
    organizerId
) {
  return apiFetch(
      `/organizers/${organizerId}/events`
  );
}

export function updateEventStatus(eventId, status) {
  return apiFetch(`/events/${eventId}/status`, {
    method: "PATCH",
    body: JSON.stringify({ status }),
  });
}
