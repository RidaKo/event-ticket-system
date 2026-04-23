export default function CheckoutStepLayout({ title, children, sidebar }) {
  return (
    <main className="checkout-shell">
      <section className="checkout-main">
        <h1>{title}</h1>
        {children}
      </section>
      <aside className="checkout-sidebar">{sidebar}</aside>
    </main>
  );
}
