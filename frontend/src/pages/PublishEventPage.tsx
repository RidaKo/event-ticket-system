import { CreateEventForm, PublishedEventsList } from '@/features/events';

export function PublishEventPage() {
  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1fr)_360px]">
      <CreateEventForm />
      <PublishedEventsList />
    </div>
  );
}
