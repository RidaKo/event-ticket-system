import clsx from "../../lib/clsx.js";

export default function Chip({
  children,
  selected = false,
  interactive = false,
  onClick,
  size = "md",
}) {
  const base =
    "inline-flex items-center rounded-md border transition-colors select-none";
  const paddings = size === "sm" ? "px-2 py-0.5 text-xs" : "px-3 py-1 text-sm";
  const state = selected
    ? "bg-accent text-white border-accent"
    : "bg-white text-ink border-slate-200 hover:border-slate-300";
  const cursor = interactive ? "cursor-pointer" : "cursor-default";

  if (interactive) {
    return (
      <button
        type="button"
        onClick={onClick}
        className={clsx(base, paddings, state, cursor)}
      >
        {children}
      </button>
    );
  }
  return (
    <span className={clsx(base, paddings, state, cursor)}>{children}</span>
  );
}
