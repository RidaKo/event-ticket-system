import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createEvent } from '../api/events.api';
import { eventKeys } from '../api/events.keys';
import type { CreateEventPayload, EventSummary } from '../types';

export function useCreateEvent() {
  const queryClient = useQueryClient();
  return useMutation<EventSummary, Error, CreateEventPayload>({
    mutationFn: createEvent,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: eventKeys.mine() });
    },
  });
}
