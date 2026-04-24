export default function clsx(...parts) {
  return parts.filter(Boolean).join(" ");
}
