export function formatCategoryLabel(value) {
  if (!value) return "Event";
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

export function formatEventDate(isoDate) {
  if (!isoDate) return "Date TBA";
  const date = new Date(isoDate);
  if (Number.isNaN(date.getTime())) return "Date TBA";
  return date.toLocaleDateString(undefined, {
    weekday: "short",
    month: "short",
    day: "numeric",
  });
}

export function formatTagLabel(value) {
  if (!value) return "Event";
  return value
    .toLowerCase()
    .split(/[\s_-]+/)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

export function mapRecommendedEvent(event) {
  const tags = (event.tags ?? []).map((tag) => formatTagLabel(tag)).filter(Boolean);

  return {
    id: event.id,
    title: event.title,
    date: formatEventDate(event.startAt),
    venue: event.venue || event.city || "Venue TBA",
    tags,
    tag: tags[0] ?? "Event",
    category: formatCategoryLabel(event.category),
  };
}
