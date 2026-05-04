import { LoginForm } from '../components/LoginForm';
import styles from './LoginPage.module.css';

export function LoginPage() {
  return (
    <div className={styles.page}>
      <LoginForm />
    </div>
  );
}
