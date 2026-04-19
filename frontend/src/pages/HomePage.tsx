import styles from './HomePage.module.css';

export function HomePage() {
  return (
    <section className={styles.page}>
      <h1 className={styles.title}>Welcome</h1>
      <p className={styles.body}>Scaffold ready. Features land here.</p>
    </section>
  );
}
