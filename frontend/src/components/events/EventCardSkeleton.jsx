export default function EventCardSkeleton() {
  return (
    <div className="animate-pulse overflow-hidden rounded-xl border border-slate-200 bg-surface-card shadow-card">
      <div className="aspect-[16/10] w-full bg-surface-sunken" />
      <div className="flex flex-col gap-3 p-4">
        <div className="h-4 w-3/4 rounded bg-slate-300/70" />
        <div className="h-3 w-5/6 rounded bg-slate-200" />
        <div className="h-3 w-2/3 rounded bg-slate-200" />
        <div className="mt-2 flex gap-2">
          <div className="h-6 w-14 rounded bg-slate-200" />
          <div className="h-6 w-20 rounded bg-slate-200" />
        </div>
      </div>
    </div>
  );
}
