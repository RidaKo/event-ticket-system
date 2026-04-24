import Chip from "../ui/Chip.jsx";
import BookmarkButton from "../ui/BookmarkButton.jsx";

export default function EventListRow({ event }) {
  return (
    <article className="flex items-center gap-4 rounded-xl border border-slate-200 bg-surface-card p-3 shadow-card transition-shadow hover:shadow-cardHover">
      <div className="h-24 w-32 flex-none overflow-hidden rounded-lg bg-surface-sunken">
        {event.imageUrl ? (
          <img src={event.imageUrl} alt="" className="h-full w-full object-cover" loading="lazy" />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-xs text-ink-subtle">
            IMG
          </div>
        )}
      </div>

      <div className="flex min-w-0 flex-1 flex-col gap-2">
        <h3 className="truncate text-base font-semibold text-ink">{event.title}</h3>
        <p className="line-clamp-2 text-sm text-ink-muted">{event.description}</p>
        <div className="flex flex-wrap gap-2">
          {event.tags?.slice(0, 1).map((t) => (
            <Chip key={t} size="sm">
              {t}
            </Chip>
          ))}
        </div>
      </div>

      <BookmarkButton />
    </article>
  );
}
