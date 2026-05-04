import { useState, type FormEvent } from 'react';
import { ApiError } from '@/shared/lib/api-error';
import { useCategories } from '../hooks/useCategories';
import { useCreateEvent } from '../hooks/useCreateEvent';
import type { CategoryOption, CreateEventPayload } from '../types';

interface CreateEventFormProps {
  onCreated?: () => void;
}

interface FormState {
  title: string;
  description: string;
  date: string;
  time: string;
  venueName: string;
  city: string;
  categoryValue: string;
}

const EMPTY_FORM: FormState = {
  title: '',
  description: '',
  date: '',
  time: '',
  venueName: '',
  city: '',
  categoryValue: '',
};

type FieldErrors = Partial<Record<keyof FormState, string>>;

export function CreateEventForm({ onCreated }: CreateEventFormProps) {
  const categories = useCategories();
  const createEvent = useCreateEvent();
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [errors, setErrors] = useState<FieldErrors>({});

  function setField<K extends keyof FormState>(key: K, value: FormState[K]) {
    setForm((prev) => ({ ...prev, [key]: value }));
    setErrors((prev) => (prev[key] ? { ...prev, [key]: undefined } : prev));
  }

  function validate(): FieldErrors {
    const next: FieldErrors = {};
    if (!form.title.trim()) next.title = 'Title is required';
    if (!form.date) next.date = 'Date is required';
    if (!form.time) next.time = 'Time is required';
    if (!form.venueName.trim()) next.venueName = 'Venue is required';
    if (!form.city.trim()) next.city = 'City is required';
    if (!form.categoryValue) next.categoryValue = 'Pick a category';
    return next;
  }

  async function submit(publish: boolean) {
    const fieldErrors = validate();
    setErrors(fieldErrors);
    if (Object.keys(fieldErrors).length > 0) return;

    const payload: CreateEventPayload = {
      title: form.title.trim(),
      description: form.description.trim() || undefined,
      categorySlug: form.categoryValue.toLowerCase(),
      venueName: form.venueName.trim(),
      city: form.city.trim(),
      date: form.date,
      time: form.time.length === 5 ? `${form.time}:00` : form.time,
      publish,
    };

    try {
      await createEvent.mutateAsync(payload);
      setForm(EMPTY_FORM);
      onCreated?.();
    } catch {
      /* surfaced via createEvent.error */
    }
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    void submit(true);
  }

  const serverError = createEvent.error instanceof ApiError ? createEvent.error.message : null;
  const isBusy = createEvent.isPending;

  return (
    <form
      onSubmit={handleSubmit}
      noValidate
      className="rounded-xl border border-slate-200 bg-surface-card shadow-card"
    >
      <header className="border-b border-slate-100 px-6 py-5">
        <h2 className="text-lg font-semibold text-ink">Create Event</h2>
      </header>

      <div className="flex flex-col gap-5 px-6 py-6">
        <Field label="Event Title" htmlFor="evt-title" error={errors.title}>
          <input
            id="evt-title"
            type="text"
            value={form.title}
            placeholder="Enter event name"
            onChange={(e) => setField('title', e.target.value)}
            className={inputClasses(Boolean(errors.title))}
          />
        </Field>

        <Field label="Description" htmlFor="evt-desc" error={errors.description}>
          <div className="relative">
            <textarea
              id="evt-desc"
              value={form.description}
              placeholder="Description placeholder..."
              onChange={(e) => setField('description', e.target.value)}
              rows={5}
              className="w-full resize-y rounded-md border border-slate-200 bg-white px-3 py-2 text-sm text-ink placeholder:text-ink-subtle focus:border-slate-300 focus:outline-none"
            />
            <button
              type="button"
              className="pointer-events-none absolute right-3 top-3 rounded-md bg-ink px-2 py-0.5 text-xs font-medium text-white opacity-90"
              tabIndex={-1}
              aria-hidden
            >
              Preview
            </button>
          </div>
        </Field>

        <div className="grid grid-cols-2 gap-4">
          <Field label="Date" htmlFor="evt-date" error={errors.date}>
            <input
              id="evt-date"
              type="date"
              value={form.date}
              onChange={(e) => setField('date', e.target.value)}
              className={inputClasses(Boolean(errors.date))}
            />
          </Field>
          <Field label="Time" htmlFor="evt-time" error={errors.time}>
            <input
              id="evt-time"
              type="time"
              value={form.time}
              onChange={(e) => setField('time', e.target.value)}
              className={inputClasses(Boolean(errors.time))}
            />
          </Field>
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-[minmax(0,1fr)_180px]">
          <Field label="Venue" htmlFor="evt-venue" error={errors.venueName}>
            <input
              id="evt-venue"
              type="text"
              value={form.venueName}
              placeholder="Venue or address"
              onChange={(e) => setField('venueName', e.target.value)}
              className={inputClasses(Boolean(errors.venueName))}
            />
          </Field>
          <Field label="City" htmlFor="evt-city" error={errors.city}>
            <input
              id="evt-city"
              type="text"
              value={form.city}
              placeholder="City"
              onChange={(e) => setField('city', e.target.value)}
              className={inputClasses(Boolean(errors.city))}
            />
          </Field>
        </div>

        <Field label="Category" htmlFor="evt-cat" error={errors.categoryValue}>
          <CategoryPicker
            options={categories.data ?? []}
            value={form.categoryValue}
            isLoading={categories.isLoading}
            isError={categories.isError}
            onChange={(v) => setField('categoryValue', v)}
          />
        </Field>

        {serverError ? (
          <p role="alert" className="text-sm text-red-600">
            {serverError}
          </p>
        ) : null}
      </div>

      <footer className="grid grid-cols-1 gap-3 border-t border-slate-100 px-6 py-4 sm:grid-cols-2">
        <button
          type="button"
          disabled={isBusy}
          onClick={() => void submit(false)}
          className="rounded-md border border-slate-200 bg-surface-sunken px-4 py-2 text-sm font-medium text-ink hover:bg-slate-200 disabled:opacity-60"
        >
          {isBusy ? 'Saving…' : 'Save as Draft'}
        </button>
        <button
          type="submit"
          disabled={isBusy}
          className="inline-flex items-center justify-center gap-2 rounded-md bg-accent px-4 py-2 text-sm font-medium text-white hover:opacity-95 disabled:opacity-60"
        >
          <PlusIcon /> {isBusy ? 'Publishing…' : 'Publish Event'}
        </button>
      </footer>
    </form>
  );
}

function Field({
  label,
  htmlFor,
  error,
  children,
}: {
  label: string;
  htmlFor: string;
  error?: string;
  children: React.ReactNode;
}) {
  return (
    <div>
      <label htmlFor={htmlFor} className="mb-1.5 block text-sm font-semibold text-ink">
        {label}
      </label>
      {children}
      {error ? <p className="mt-1 text-xs text-red-600">{error}</p> : null}
    </div>
  );
}

function inputClasses(hasError: boolean) {
  const base =
    'w-full rounded-md border bg-white px-3 py-2 text-sm text-ink placeholder:text-ink-subtle focus:outline-none';
  return `${base} ${hasError ? 'border-red-400 focus:border-red-500' : 'border-slate-200 focus:border-slate-300'}`;
}

function CategoryPicker({
  options,
  value,
  isLoading,
  isError,
  onChange,
}: {
  options: CategoryOption[];
  value: string;
  isLoading: boolean;
  isError: boolean;
  onChange: (next: string) => void;
}) {
  if (isLoading) {
    return <p className="text-xs text-ink-muted">Loading categories…</p>;
  }
  if (isError) {
    return <p className="text-xs text-red-600">Could not load categories.</p>;
  }
  if (options.length === 0) {
    return <p className="text-xs text-ink-muted">No categories available.</p>;
  }
  return (
    <div className="flex flex-wrap gap-2">
      {options.map((opt) => {
        const selected = opt.value === value;
        return (
          <button
            key={opt.value}
            type="button"
            onClick={() => onChange(opt.value)}
            aria-pressed={selected}
            className={
              selected
                ? 'inline-flex items-center rounded-md border border-accent bg-accent px-3 py-1 text-sm font-medium text-white'
                : 'inline-flex items-center rounded-md border border-slate-200 bg-white px-3 py-1 text-sm text-ink hover:border-slate-300'
            }
          >
            {opt.label}
          </button>
        );
      })}
    </div>
  );
}

function PlusIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      className="h-4 w-4"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden
    >
      <path d="M12 5v14M5 12h14" />
    </svg>
  );
}
