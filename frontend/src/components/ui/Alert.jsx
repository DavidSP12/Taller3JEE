import styles from './Alert.module.css';

const TYPE_CLASS = {
  error: styles.error,
  success: styles.success,
  info: styles.info,
  warning: styles.warning,
};

export default function Alert({ type = 'info', message }) {
  if (!message) return null;
  return (
    <div className={`${styles.alert} ${TYPE_CLASS[type] ?? styles.info}`}>
      {message}
    </div>
  );
}
