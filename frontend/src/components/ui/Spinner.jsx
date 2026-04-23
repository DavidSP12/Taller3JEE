import styles from './Spinner.module.css';

export default function Spinner({ message = 'Cargando…' }) {
  return (
    <div className={styles.wrapper}>
      <div className={styles.spin} />
      <p className={styles.msg}>{message}</p>
    </div>
  );
}
