import { useQuery } from '@tanstack/react-query';
import { getMyEvents } from '../api/events.api';
import { eventKeys } from '../api/events.keys';
import type { EventSummary } from '../types';

export function useMyEvents() {
  return useQuery<EventSummary[]>({
    queryKey: eventKeys.mine(),
    queryFn: getMyEvents,
    staleTime: 30_000,
  });
}
