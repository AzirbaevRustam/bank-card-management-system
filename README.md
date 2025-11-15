# Система управления банковскими картами

Это REST API приложение на Spring Boot, предназначенное для управления банковскими картами. Оно позволяет администраторам создавать, управлять и просматривать все карты, а пользователям — просматривать свои карты и осуществлять переводы между ними.

## Технологии

*   Java 21
*   Spring Boot 3.2.0
*   Spring Security
*   Spring Data JPA
*   PostgreSQL
*   Liquibase
*   Docker
*   JWT (JSON Web Tokens)
*   Swagger UI / OpenAPI 3.0
*   JUnit 5 / Mockito (для тестирования)

## Функциональность

### Аутентификация и Авторизация

*   Регистрация новых пользователей (`/api/auth/signup`).
*   Аутентификация пользователей и выдача JWT токена (`/api/auth/signin`).
*   Использование JWT токенов для авторизации в защищённых эндпоинтах (через заголовок `Authorization: Bearer <token>`).
*   Ролевая модель: `ADMIN`, `USER`.

### Для Администратора (`ADMIN`)

*   **Управление пользователями:**
    *   Просмотр всех пользователей (`GET /api/admin/users`).
    *   Просмотр конкретного пользователя (`GET /api/admin/users/{userId}`).
    *   Удаление пользователя (`DELETE /api/admin/users/{userId}`).
    *   Блокировка пользователя (`PUT /api/admin/users/{userId}/block`).
*   **Управление картами:**
    *   Просмотр всех карт (`GET /api/admin/cards`).
    *   Создание новой карты (`POST /api/admin/cards`).
    *   Блокировка карты (`PUT /api/admin/cards/{cardId}/block`).
    *   Активация карты (`PUT /api/admin/cards/{cardId}/activate`).
    *   Удаление карты (`DELETE /api/admin/cards/{cardId}`).

### Для Пользователя (`USER`)

*   **Управление своими картами:**
    *   Просмотр своих карт (`GET /api/user/cards`).
    *   Просмотр конкретной своей карты (`GET /api/user/cards/{cardId}`).
    *   Блокировка своей карты (`PUT /api/user/cards/{cardId}/block`).
    *   Просмотр баланса своей карты (`GET /api/user/cards/{cardId}/balance`).
*   **Переводы:**
    *   Осуществление перевода между своими картами (`POST /api/user/transfers`).

### Общая функциональность

*   **Валидация входных данных:** Используется `@Valid` и `@Validated` для проверки корректности данных в DTO.
*   **Обработка ошибок:** Централизованная обработка исключений через `@RestControllerAdvice`.
*   **Маскирование номеров карт:** Номера карт отображаются в формате `**** **** **** XXXX` в DTO.
*   **Безопасность:** Защита эндпоинтов с помощью `@PreAuthorize` и Spring Security.

## Инструкция по запуску

1.  **Клонировать репозиторий:**
    ```bash
    git clone <your-repo-url>
    cd <your-repo-directory-name>
    ```

2.  **Запустить базу данных PostgreSQL с помощью Docker Compose:**
    ```bash
    docker-compose up -d
    ```
    Это запустит PostgreSQL на порту `5432` с именем базы данных `bank_db`, именем пользователя `user` и паролем `password` (или теми, что указаны в `.env` файле, если он используется). Liquibase автоматически применит миграции при первом запуске приложения.

3.  **(Опционально) Настроить переменные окружения:**
    Убедитесь, что переменные `DB_USERNAME` и `DB_PASSWORD` (и, при необходимости, `JWT_SECRET`) установлены в вашей среде или в файле `.env` рядом с `docker-compose.yml`, чтобы они совпадали с настройками в `docker-compose.yml` и `application.yml`.

4.  **Собрать и запустить приложение Spring Boot:**
    ```bash
    mvn spring-boot:run
    ```
    Или сначала собрать JAR:
    ```bash
    mvn clean install
    ```
    А затем запустить JAR файл:
    ```bash
    java -jar target/<your-app-name>.jar # Замените <your-app-name> на фактическое имя JAR файла
    ```
    Приложение будет доступно на `http://localhost:8080`.

5.  **Доступ к API и документации:**
    *   Swagger UI: Откройте `http://localhost:8080/swagger-ui.html` в браузере для интерактивного просмотра и тестирования API.
    *   OpenAPI JSON: Спецификация доступна по адресу `http://localhost:8080/v3/api-docs`.

## Тестирование

*   Для запуска всех unit-тестов выполните:
    ```bash
    mvn test
    ```
    В проекте есть тесты как для слоя сервиса (`src/test/java/com/example/bankcards/service`), так и для REST API (`src/test/java/com/example/bankcards/controller`).
