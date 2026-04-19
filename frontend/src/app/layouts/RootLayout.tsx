import { Outlet } from 'react-router-dom';
import styles from './RootLayout.module.css';

export function RootLayout() {
  return (
    <div className={styles.shell}>
      <header className={styles.header}>
        <span className={styles.brand}>Event Ticket System</span>
      </header>
      <main className={styles.main}>
        <Outlet />
      </main>
    </div>
  );
}
