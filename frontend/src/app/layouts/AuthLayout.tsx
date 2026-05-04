import { Outlet } from 'react-router-dom';
import styles from './AuthLayout.module.css';

export function AuthLayout() {
  return (
    <div className={styles.shell}>
      <div className={styles.card}>
        <Outlet />
      </div>
    </div>
  );
}
