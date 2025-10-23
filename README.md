# Reminder App

Приложение на Spring Boot для управления напоминаниями с базой PostgreSQL.  
Запуск через Docker Compose без необходимости локальной установки Java и базы.

---

## Инструкция по запуску

1. **Скопировать `.env`**

```bash
cp .env.example .env
```

2. Заполнить переменные

Описание переменных:

```
POSTGRES_* — настройки базы данных PostgreSQL

MAIL_* — настройки SMTP для отправки почты

GOOGLE_CLIENT_* — данные для авторизации через Google

TELEGRAM_BOT_* — данные бота Telegram
```

3. Запустить контейнеры

```bash
docker compose up -d --build
```
