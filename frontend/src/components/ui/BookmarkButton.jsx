import { useState } from "react";
import clsx from "../../lib/clsx.js";

export default function BookmarkButton({ initial = false, onChange }) {
  const [on, setOn] = useState(initial);

  function toggle(e) {
    e.stopPropagation();
    const next = !on;
    setOn(next);
    onChange?.(next);
  }

  return (
    <button
      type="button"
      aria-pressed={on}
      aria-label={on ? "Remove bookmark" : "Bookmark event"}
      onClick={toggle}
      className={clsx(
        "inline-flex h-8 w-8 items-center justify-center rounded-md text-ink-subtle hover:bg-surface-sunken hover:text-ink",
        on && "text-accent hover:text-accent"
      )}
    >
      <svg
        viewBox="0 0 24 24"
        className="h-5 w-5"
        fill={on ? "currentColor" : "none"}
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <path d="M6 3.5h12a1 1 0 0 1 1 1V21l-7-4-7 4V4.5a1 1 0 0 1 1-1z" />
      </svg>
    </button>
  );
}
