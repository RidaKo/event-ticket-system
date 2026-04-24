import EventListRow from "../events/EventListRow.jsx";

export default function BrowseEvents({ items, isLoading, isError }) {
  return (
    <section className="mt-12">
      <h2 className="mb-4 text-2xl font-semibold text-ink">Browse Events</h2>

      {isLoading && (
        <div className="flex flex-col gap-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <div
              key={i}
              className="h-28 animate-pulse rounded-xl border border-slate-200 bg-surface-card shadow-card"
            />
          ))}
        </div>
      )}

      {isError && !isLoading && (
        <div className="rounded-xl border border-slate-200 bg-surface-card p-6 text-center text-sm text-ink-muted">
          Could not load events.
        </div>
      )}

      {!isLoading && !isError && items.length === 0 && (
        <div className="rounded-xl border border-dashed border-slate-300 bg-surface-card p-8 text-center text-sm text-ink-muted">
          No events to show.
        </div>
      )}

      {!isLoading && !isError && items.length > 0 && (
        <div className="flex flex-col gap-3">
          {items.map((e) => (
            <EventListRow key={e.id} event={e} />
          ))}
        </div>
      )}
    </section>
  );
}
