import EventCard from "../events/EventCard.jsx";
import EventCardSkeleton from "../events/EventCardSkeleton.jsx";

export default function RecommendedForYou({
  items,
  isLoading,
  isError,
  fallbackUsed,
  onReset,
  categoryLabelMap,
}) {
  return (
    <section>
      <div className="mb-4 flex items-end justify-between gap-4">
        <h2 className="text-2xl font-semibold text-ink">Recommended for you</h2>
        <span className="text-sm text-ink-muted">
          {fallbackUsed ? "Showing popular upcoming events" : "Based on your preferences"}
        </span>
      </div>

      {isLoading && (
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <EventCardSkeleton key={i} />
          ))}
        </div>
      )}

      {isError && !isLoading && (
        <div className="rounded-xl border border-slate-200 bg-surface-card p-8 text-center text-sm text-ink-muted">
          Could not load recommendations. Please try again.
        </div>
      )}

      {!isLoading && !isError && items.length === 0 && (
        <div className="rounded-xl border border-dashed border-slate-300 bg-surface-card p-10 text-center">
          <p className="text-sm text-ink-muted">
            No matches for the selected filters.
          </p>
          <button
            type="button"
            onClick={onReset}
            className="mt-4 rounded-md border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-ink hover:bg-surface-sunken"
          >
            Clear filters
          </button>
        </div>
      )}

      {!isLoading && !isError && items.length > 0 && (
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {items.map((e) => (
            <EventCard key={e.id} event={e} categoryLabelMap={categoryLabelMap} />
          ))}
        </div>
      )}
    </section>
  );
}
