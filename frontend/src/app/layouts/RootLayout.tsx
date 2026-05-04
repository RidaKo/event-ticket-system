import { NavLink, Outlet } from 'react-router-dom';
import clsx from '@/lib/clsx.js';

export function RootLayout() {
  return (
    <div className="min-h-full">
      <header className="sticky top-0 z-10 border-b border-slate-200 bg-white/90 backdrop-blur">
        <div className="mx-auto flex h-16 max-w-7xl items-center gap-6 px-6">
          <div className="flex h-8 w-20 items-center justify-center rounded bg-surface-sunken text-[11px] font-semibold tracking-wider text-ink-subtle">
            LOGO
          </div>

          <nav className="flex items-center gap-2">
            <NavItem to="/discover">Discover Events</NavItem>
            <NavItem to="/publish">Publish Event</NavItem>
          </nav>

          <div className="ml-auto flex items-center gap-4">
            <SearchBox />
            <ProfileBubble />
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-6 py-8">
        <Outlet />
      </main>
    </div>
  );
}

function NavItem({ to, children }: { to: string; children: React.ReactNode }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        clsx(
          'rounded-md px-4 py-1.5 text-sm font-medium transition-colors',
          isActive
            ? 'border border-slate-300 bg-white text-ink shadow-sm'
            : 'text-ink-muted hover:text-ink',
        )
      }
    >
      {children}
    </NavLink>
  );
}

function SearchBox() {
  return (
    <div className="relative">
      <svg
        viewBox="0 0 24 24"
        className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-subtle"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden
      >
        <circle cx="11" cy="11" r="7" />
        <path d="m20 20-3.5-3.5" />
      </svg>
      <input
        type="search"
        placeholder="Search events..."
        className="h-9 w-72 rounded-md border border-transparent bg-transparent pl-9 pr-3 text-sm text-ink placeholder:text-ink-subtle focus:border-slate-300 focus:outline-none"
      />
    </div>
  );
}

function ProfileBubble() {
  return (
    <div className="flex h-9 w-9 items-center justify-center rounded-full bg-surface-sunken text-ink-subtle">
      <svg
        viewBox="0 0 24 24"
        className="h-5 w-5"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden
      >
        <circle cx="12" cy="8" r="4" />
        <path d="M4 21c1.5-4 5-6 8-6s6.5 2 8 6" />
      </svg>
    </div>
  );
}
