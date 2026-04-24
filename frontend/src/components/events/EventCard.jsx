import Chip from "../ui/Chip.jsx";
import BookmarkButton from "../ui/BookmarkButton.jsx";
import { formatCategoryLabel } from "../../lib/catalog.js";

export default function EventCard({ event, categoryLabelMap = {} }) {
  const categoryLabel =
    categoryLabelMap[event.category] ?? formatCategoryLabel(event.category);

  return (
    <article className="group flex flex-col overflow-hidden rounded-xl border border-slate-200 bg-surface-card shadow-card transition-shadow hover:shadow-cardHover">
      <div className="aspect-[16/10] w-full bg-surface-sunken">
        {event.imageUrl ? (
          <img
            src={event.imageUrl}
            alt=""
            className="h-full w-full object-cover"
            loading="lazy"
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-sm text-ink-subtle">
            IMAGE
          </div>
        )}
      </div>

      <div className="flex flex-1 flex-col gap-3 p-4">
        <h3 className="line-clamp-2 text-base font-semibold leading-snug text-ink">
          {event.title}
        </h3>
        <p className="line-clamp-2 text-sm text-ink-muted">
          {event.description}
        </p>

        <div className="mt-auto flex items-end justify-between gap-3 pt-2">
          <div className="flex flex-wrap gap-2">
            {event.tags?.slice(0, 1).map((t) => (
              <Chip key={t} size="sm">
                {t}
              </Chip>
            ))}
            <Chip size="sm">{categoryLabel}</Chip>
          </div>
          <BookmarkButton />
        </div>
      </div>
    </article>
  );
}
