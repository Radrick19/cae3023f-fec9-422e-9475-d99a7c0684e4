# CodeConfirmApp

Простой backend-сервис для защиты операций одноразовыми OTP-кодами. Приложение построено на Spring Boot, Spring MVC, Spring Security, JDBC и PostgreSQL.

## Что реализовано

- регистрация и логин пользователей с ролями `ADMIN` и `USER`;
- запрет на создание второго администратора;
- JWT-аутентификация и разграничение доступа;
- настройка длины OTP-кода и времени жизни через API администратора;
- генерация OTP-кодов, привязанных к `operationId`;
- валидация OTP-кода и перевод статусов `ACTIVE -> USED/EXPIRED`;
- периодическая задача, которая помечает просроченные коды как `EXPIRED`;
- доставка OTP по `EMAIL`, `SMS`, `TELEGRAM` или сохранение в файл `otp-codes.txt` в корне проекта;
- логирование запросов и внутренних ошибок через стандартный стек логирования Spring Boot.

## Запуск

1. Создайте PostgreSQL-базу:

```sql
CREATE DATABASE code_confirm;
```

2. При необходимости измените настройки в [src/main/resources/application.properties](/Users/radrick/IdeaProjects/CodeConfirmApp/src/main/resources/application.properties), а также:

- [src/main/resources/email.properties](/Users/radrick/IdeaProjects/CodeConfirmApp/src/main/resources/email.properties)
- [src/main/resources/sms.properties](/Users/radrick/IdeaProjects/CodeConfirmApp/src/main/resources/sms.properties)
- [src/main/resources/telegram.properties](/Users/radrick/IdeaProjects/CodeConfirmApp/src/main/resources/telegram.properties)

3. Запустите приложение:

```bash
mvn spring-boot:run
```

Сервер стартует на `http://localhost:8080`.

При старте приложения Spring Boot автоматически выполняет файл `schema.sql`, потому что в `application.properties` включено `spring.sql.init.mode=always`.
Это значит, что таблицы и начальная запись в `otp_config` создаются автоматически при запуске приложения, если база данных уже доступна.

## API

### Auth

`POST /api/auth/register`

```json
{
  "username": "admin",
  "password": "admin123",
  "role": "ADMIN",
  "email": "admin@example.com",
  "phone": "+79990000000",
  "telegramChatId": "123456"
}
```

`POST /api/auth/login`

```json
{
  "username": "admin",
  "password": "admin123"
}
```

### Admin

Во все admin-запросы передавайте `Authorization: Bearer <token>`.

- `GET /api/admin/config`
- `PUT /api/admin/config`
- `GET /api/admin/users`
- `DELETE /api/admin/users/{userId}`

Пример тела для `PUT /api/admin/config`:

```json
{
  "codeLength": 6,
  "ttlSeconds": 300
}
```

### OTP

Во все user-запросы передавайте `Authorization: Bearer <token>`.

`POST /api/otp/generate`

```json
{
  "operationId": "payment-1001",
  "channel": "FILE",
  "destination": "test123"
}
```

`POST /api/otp/validate`

```json
{
  "operationId": "payment-1001",
  "code": "123456"
}
```

## Как тестировать

- ручной сценарий:
1. Зарегистрируйте администратора.
2. Выполните логин и получите JWT.
3. Создайте обычного пользователя.
4. Сгенерируйте OTP с каналом `FILE`.
5. Откройте `otp-codes.txt` в корне проекта и возьмите код.
6. Провалидируйте код через `/api/otp/validate`.