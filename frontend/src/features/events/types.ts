export type EventStatus = 'DRAFT' | 'PUBLISHED';

export interface EventSummary {
  id: number;
  title: string;
  status: EventStatus;
  slug: string;
  categorySlug: string | null;
  venueName: string | null;
  city: string | null;
  startDatetime: string | null;
}

export interface CreateEventPayload {
  title: string;
  description?: string;
  categorySlug: string;
  venueName: string;
  city: string;
  date: string;
  time: string;
  publish: boolean;
}

export interface CategoryOption {
  /** uppercase slug from the backend, e.g. "MUSIC" */
  value: string;
  label: string;
}
