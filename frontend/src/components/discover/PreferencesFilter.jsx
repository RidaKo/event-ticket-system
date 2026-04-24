import { useEffect, useState } from "react";
import Chip from "../ui/Chip.jsx";
import { EMPTY_FILTERS } from "../../lib/catalog.js";

export default function PreferencesFilter({
  value,
  onApply,
  onReset,
  categories,
  tags,
  isCatalogLoading,
  isCatalogError,
}) {
  const [draft, setDraft] = useState(value);

  useEffect(() => {
    setDraft(value);
  }, [value]);

  function toggleInArray(key, item) {
    setDraft((prev) => {
      const exists = prev[key].includes(item);
      return {
        ...prev,
        [key]: exists ? prev[key].filter((x) => x !== item) : [...prev[key], item],
      };
    });
  }

  function setField(key, val) {
    setDraft((prev) => ({ ...prev, [key]: val }));
  }

  function apply() {
    onApply(draft);
  }

  function reset() {
    setDraft(EMPTY_FILTERS);
    onReset();
  }

  return (
    <aside className="h-fit w-full rounded-xl border border-slate-200 bg-surface-card p-5 shadow-card">
      <h2 className="text-base font-semibold text-ink">Filter by Preferences</h2>

      <Section title="Category">
        <div className="flex flex-wrap gap-2">
          {categories.map((c) => (
            <Chip
              key={c.value}
              interactive
              selected={draft.categories.includes(c.value)}
              onClick={() => toggleInArray("categories", c.value)}
              size="sm"
            >
              {c.label}
            </Chip>
          ))}
        </div>
        {isCatalogLoading && <MetaText>Loading categories...</MetaText>}
        {isCatalogError && <MetaText>Could not load categories.</MetaText>}
      </Section>

      <Section title="Interest Tags">
        <div className="flex flex-wrap gap-2">
          {tags.map((t) => (
            <Chip
              key={t.slug}
              interactive
              selected={draft.tags.includes(t.slug)}
              onClick={() => toggleInArray("tags", t.slug)}
              size="sm"
            >
              {t.label}
            </Chip>
          ))}
        </div>
        {isCatalogLoading && <MetaText>Loading tags...</MetaText>}
        {isCatalogError && <MetaText>Could not load tags.</MetaText>}
      </Section>

      <Section title="Date Range">
        <div className="flex flex-col gap-2">
          <DateInput
            value={draft.startDate}
            placeholder="Start date"
            onChange={(v) => setField("startDate", v)}
          />
          <DateInput
            value={draft.endDate}
            placeholder="End date"
            onChange={(v) => setField("endDate", v)}
          />
        </div>
      </Section>

      <Section title="Location">
        <input
          type="text"
          value={draft.location}
          placeholder="City or venue"
          onChange={(e) => setField("location", e.target.value)}
          className="w-full rounded-md border border-transparent bg-transparent px-1 py-1 text-sm text-ink placeholder:text-ink-subtle focus:border-slate-300 focus:outline-none"
        />
      </Section>

      <div className="mt-6 grid grid-cols-2 gap-3 border-t border-slate-100 pt-5">
        <button
          type="button"
          onClick={reset}
          className="rounded-md border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-ink hover:bg-surface-sunken"
        >
          Reset
        </button>
        <button
          type="button"
          onClick={apply}
          className="rounded-md bg-accent px-4 py-2 text-sm font-medium text-white hover:opacity-95"
        >
          Apply
        </button>
      </div>
    </aside>
  );
}

function Section({ title, children }) {
  return (
    <div className="mt-5 border-t border-slate-100 pt-4 first-of-type:mt-6">
      <h3 className="mb-3 text-xs font-semibold uppercase tracking-wider text-ink-subtle">
        {title}
      </h3>
      {children}
    </div>
  );
}

function DateInput({ value, placeholder, onChange }) {
  return (
    <input
      type="date"
      value={value}
      placeholder={placeholder}
      onChange={(e) => onChange(e.target.value)}
      className="w-full rounded-md border border-transparent bg-transparent px-1 py-1 text-sm text-ink placeholder:text-ink-subtle focus:border-slate-300 focus:outline-none"
      aria-label={placeholder}
    />
  );
}

function MetaText({ children }) {
  return <p className="mt-3 text-xs text-ink-muted">{children}</p>;
}
