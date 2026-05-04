import { useMyEvents } from '../hooks/useMyEvents';
import type { EventStatus, EventSummary } from '../types';

export function PublishedEventsList() {
  const { data, isLoading, isError, refetch } = useMyEvents();

  return (
    <aside className="rounded-xl border border-slate-200 bg-surface-card shadow-card">
      <header className="border-b border-slate-100 px-6 py-5">
        <h2 className="text-lg font-semibold text-ink">Published Events</h2>
      </header>

      <div className="flex flex-col gap-3 px-6 py-6">
        {isLoading ? (
          <SkeletonList />
        ) : isError ? (
          <ErrorState onRetry={() => void refetch()} />
        ) : !data || data.length === 0 ? (
          <EmptyState />
        ) : (
          data.map((event) => <EventRow key={event.id} event={event} />)
        )}
      </div>
    </aside>
  );
}

function EventRow({ event }: { event: EventSummary }) {
  const venueDetails = [event.venueName, event.city].filter(Boolean).join(', ');

  return (
    <article className="flex items-center gap-4 rounded-lg border border-slate-100 bg-surface-sunken/40 p-3">
      <div className="flex min-w-0 flex-1 flex-col gap-2">
        <h3 className="truncate text-sm font-semibold text-ink" title={event.title}>
          {event.title}
        </h3>
        <div className="flex items-center gap-2">
          <StatusPill status={event.status} />
          {venueDetails ? (
            <span className="truncate text-xs text-ink-subtle">{venueDetails}</span>
          ) : null}
          {event.startDatetime ? (
            <span className="hidden text-xs text-ink-subtle sm:inline">
              {formatEventDate(event.startDatetime)}
            </span>
          ) : null}
        </div>
      </div>
      <button
        type="button"
        aria-label={`Edit ${event.title}`}
        className="inline-flex h-8 w-8 items-center justify-center rounded-md text-ink-subtle hover:bg-surface-sunken hover:text-ink"
      >
        <PencilIcon />
      </button>
    </article>
  );
}

function StatusPill({ status }: { status: EventStatus }) {
  const isPublished = status === 'PUBLISHED';
  const className = isPublished
    ? 'inline-flex items-center rounded-md bg-ink px-2.5 py-0.5 text-xs font-medium text-white'
    : 'inline-flex items-center rounded-md border border-slate-300 bg-white px-2.5 py-0.5 text-xs font-medium text-ink';
  return <span className={className}>{isPublished ? 'published' : 'draft'}</span>;
}

function formatEventDate(value: string) {
  return new Intl.DateTimeFormat(undefined, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

function SkeletonList() {
  return (
    <>
      {Array.from({ length: 4 }).map((_, idx) => (
        <div
          key={idx}
          className="flex animate-pulse items-center gap-4 rounded-lg border border-slate-100 bg-surface-sunken/40 p-3"
        >
          <div className="flex flex-1 flex-col gap-2">
            <div className="h-3 w-2/3 rounded bg-slate-300/70" />
            <div className="h-5 w-20 rounded bg-slate-200" />
          </div>
          <div className="h-8 w-8 rounded bg-slate-200" />
        </div>
      ))}
    </>
  );
}

function EmptyState() {
  return (
    <div className="rounded-lg border border-dashed border-slate-200 bg-surface-card px-4 py-8 text-center text-sm text-ink-muted">
      No events yet. Use the form on the left to create your first one.
    </div>
  );
}

function ErrorState({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-6 text-center text-sm text-red-700">
      Could not load your events.{' '}
      <button type="button" onClick={onRetry} className="underline hover:no-underline">
        Try again
      </button>
    </div>
  );
}

function PencilIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      className="h-4 w-4"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden
    >
      <path d="M12 20h9" />
      <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4Z" />
    </svg>
  );
}
