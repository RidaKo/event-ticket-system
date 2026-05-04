import { apiClient } from '@/shared/lib/api-client';
import type { CreateEventPayload, EventSummary } from '../types';

export function getMyEvents(): Promise<EventSummary[]> {
  return apiClient.get<EventSummary[]>('/events');
}

export function createEvent(payload: CreateEventPayload): Promise<EventSummary> {
  return apiClient.post<EventSummary>('/events', payload);
}
