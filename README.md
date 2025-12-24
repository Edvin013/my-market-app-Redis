# My Market App - Multi-Module Project with Spring Security & OAuth2

Мультимодульное реактивное приложение интернет-магазина на основе Spring Boot WebFlux с интеграцией Redis для кеширования, Spring Security для авторизации пользователей и OAuth2 для защиты взаимодействия между сервисами.

## Архитектура проекта

Проект состоит из трёх модулей:

### 1. Market App (основное приложение)
- Витрина товаров с поиском, сортировкой и пагинацией
- **Авторизация пользователей по логину/паролю** с помощью Spring Security
- Управление корзиной (привязана к пользователю)
- Оформление заказов с интеграцией сервиса платежей
- **Кеширование товаров в Redis** для повышения производительности
- Просмотр истории заказов (привязана к пользователю)
- **OAuth2 клиент** для авторизованных запросов к сервису платежей

### 2. Payment Service (сервис платежей)
- RESTful API для обработки платежей
- Проверка баланса (привязан к пользователю)
- Списание средств при оформлении заказа
- Реализован на основе **OpenAPI спецификации**
- **OAuth2 Resource Server** - защита эндпоинтов

### 3. Auth Server (сервер авторизации OAuth2)
- **Spring Authorization Server** для OAuth2
- Авторизация по Client Credentials Flow
- Выдача JWT токенов для взаимодействия между сервисами

## Возможности приложения

### Market App
- ✅ Просмотр каталога товаров с поиском и сортировкой (доступно всем)
- ✅ **Авторизация/регистрация пользователей**
- ✅ Кеширование списка товаров в Redis (TTL: 2 минуты)
- ✅ Кеширование отдельных товаров в Redis
- ✅ Просмотр карточки товара с детальной информацией (доступно всем)
- ✅ **Управление корзиной: добавление, удаление и изменение количества товаров (только для авторизованных)**
- ✅ **Проверка баланса перед оформлением заказа (только для авторизованных)**
- ✅ **Оформление заказа с автоматической оплатой через Payment Service (только для авторизованных)**
- ✅ **Просмотр истории заказов (только для авторизованных)**
- ✅ **Разделение доступа: анонимные пользователи могут только просматривать товары**

### Payment Service
- ✅ REST API для получения баланса (привязан к пользователю)
- ✅ REST API для обработки платежей (привязан к пользователю)
- ✅ Автоматическая генерация клиентского и серверного кода из OpenAPI спецификации
- ✅ Валидация достаточности средств
- ✅ Генерация уникальных идентификаторов транзакций
- ✅ **OAuth2 защита эндпоинтов - доступ только для авторизованных клиентов**

### Auth Server
- ✅ OAuth2 Authorization Server на базе Spring Authorization Server
- ✅ Client Credentials Flow для взаимодействия между сервисами
- ✅ Выдача JWT токенов
- ✅ Регистрация клиентов (market-app-client)

## Технологический стек

### Market App
- **Java 21**
- **Spring Boot 3.5.6**
- **Spring WebFlux** (реактивный веб-фреймворк)
- **Spring Security** (авторизация пользователей)
- **Spring Security OAuth2 Client** (OAuth2 клиент)
- **Spring Data R2DBC** (реактивный доступ к данным)
- **Spring Data Redis Reactive** (реактивное кеширование)
- **R2DBC PostgreSQL** (реактивный драйвер для PostgreSQL)
- **Redis** (кеширование товаров)
- **Lettuce** (реактивный Redis клиент)
- **OpenAPI Generator** (генерация клиента для Payment Service)
- **Thymeleaf** (шаблонизатор)
- **WebClient** (HTTP клиент для Payment Service)

### Payment Service
- **Java 21**
- **Spring Boot 3.5.6**
- **Spring WebFlux** (реактивный веб-фреймворк)
- **Spring Security OAuth2 Resource Server** (защита эндпоинтов)
- **OpenAPI Generator** (генерация серверного кода)
- **Jackson** (JSON сериализация)

### Auth Server
- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Security**
- **Spring Authorization Server** (OAuth2 сервер авторизации)

### Общее
- **Lombok** (уменьшение boilerplate кода)
- **JUnit 5** (тестирование)
- **Reactor Test** (тестирование реактивных потоков)
- **Embedded Redis** (для интеграционных тестов)
- **MockWebServer** (для тестирования HTTP клиента)
- **Maven** (сборка мультимодульного проекта)
- **Docker & Docker Compose** (контейнеризация)

## Безопасность и авторизация

### Авторизация пользователей в Market App
- **Форма логина/логаута** на базе Spring Security
- **Хранение пользователей в БД** (PostgreSQL)
- **Шифрование паролей** с помощью BCryptPasswordEncoder
- **Разделение прав доступа**:
  - Анонимные пользователи: просмотр товаров
  - Авторизованные пользователи: корзина, заказы, покупки
- **Привязка корзины и заказов к пользователю**

### OAuth2 авторизация между сервисами
- **Auth Server** выдаёт JWT токены
- **Market App** (OAuth2 Client) получает токен для запросов к Payment Service
- **Payment Service** (OAuth2 Resource Server) проверяет токен
- **Client Credentials Flow** для machine-to-machine авторизации
- **Балансы привязаны к пользователям** в Payment Service

### Тестовые учётные записи
- **Логин:** `user`, **Пароль:** `password`
- **Логин:** `admin`, **Пароль:** `admin123`
- **Логин:** `test`, **Пароль:** `test`

## Как собрать и запустить

### Предварительные требования
- Java 21
- Maven 3.8+
- Docker и Docker Compose (для запуска в контейнерах)

### Сборка мультипроекта

Из корневой директории проекта:

```bash
./mvnw clean package
```

Это соберет все три модуля:
- `market-app/target/market-app-0.0.1-SNAPSHOT.jar`
- `payment-service/target/payment-service-0.0.1-SNAPSHOT.jar`
- `auth-server/target/auth-server-0.0.1-SNAPSHOT.jar`

### Запуск с помощью Docker Compose (рекомендуется)

Docker Compose автоматически запустит все необходимые сервисы:
- PostgreSQL (порт 5432)
- Redis (порт 6379)
- Auth Server (порт 9000)
- Payment Service (порт 8081)
- Market App (порт 8080)

```bash
docker compose up -d --build
```

Приложение будет доступно по адресу: http://localhost:8080

Для остановки:
```bash
docker compose down
```

### Запуск локально

#### 1. Запустите PostgreSQL и Redis

```bash
docker run -d --name postgres \
  -e POSTGRES_DB=marketdb \
  -e POSTGRES_USER=market \
  -e POSTGRES_PASSWORD=market \
  -p 5432:5432 \
  postgres:15-alpine

docker run -d --name redis \
  -p 6379:6379 \
  redis:7-alpine
```

#### 2. Запустите Auth Server

```bash
cd auth-server
java -jar target/auth-server-0.0.1-SNAPSHOT.jar
```

Auth Server будет доступен на порту 9000.

#### 3. Запустите Payment Service

Обновите `payment-service/src/main/resources/application.properties`:
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9000
```

```bash
cd payment-service
java -jar target/payment-service-0.0.1-SNAPSHOT.jar
```

Payment Service будет доступен на порту 8081.

#### 4. Запустите Market App

Обновите `market-app/src/main/resources/application.properties`:
```properties
spring.r2dbc.url=r2dbc:postgresql://localhost:5432/marketdb
spring.data.redis.host=localhost
payment.service.url=http://localhost:8081
spring.security.oauth2.client.provider.auth-server.token-uri=http://localhost:9000/oauth2/token
```

```bash
cd market-app
java -jar target/market-app-0.0.1-SNAPSHOT.jar
```

Market App будет доступен на порту 8080.

### Запуск тестов

#### Запуск всех тестов проекта
```bash
./mvnw test
```

#### Запуск тестов только для Market App
```bash
./mvnw test -pl market-app
```

#### Запуск тестов только для Payment Service
```bash
./mvnw test -pl payment-service
```

#### Запуск тестов только для Auth Server
```bash
./mvnw test -pl auth-server
```

Тесты используют:
- H2 in-memory базу данных (вместо PostgreSQL)
- Embedded Redis (для тестов кеширования)
- MockWebServer (для тестов интеграции с Payment Service)
- Spring Security Test (для тестов авторизации)

## API эндпоинты

### Market App (порт 8080)
- `GET /` или `GET /items` - список товаров с поддержкой поиска, сортировки и пагинации (доступно всем)
- `GET /items/{id}` - карточка конкретного товара (доступно всем)
- `GET /login` - страница входа
- `POST /login` - авторизация пользователя
- `POST /logout` - выход из системы
- `GET /cart/items` - содержимое корзины (только для авторизованных)
- `POST /cart/items` - обновление корзины (только для авторизованных)
- `POST /buy` - оформление заказа (только для авторизованных)
- `GET /orders` - список всех заказов (только для авторизованных)
- `GET /orders/{id}` - детали конкретного заказа (только для авторизованных)

### Payment Service (порт 8081)
- `GET /api/v1/payments/balance?username={username}` - получить текущий баланс (требует OAuth2 токен)
- `POST /api/v1/payments/process` - обработать платеж (требует OAuth2 токен)

### Auth Server (порт 9000)
- `POST /oauth2/token` - получить OAuth2 токен (Client Credentials Flow)
- `GET /.well-known/openid-configuration` - OpenID Connect конфигурация
- `GET /.well-known/jwks.json` - JSON Web Key Set

## Кеширование в Redis

### Стратегия кеширования товаров

1. **Список товаров** (ключ: `items:all`)
   - Кешируется весь список товаров при первом запросе
   - TTL: 120 секунд (настраивается в `cache.item.ttl`)
   - При поиске кеш не используется, запрос идет напрямую в БД

2. **Отдельные товары** (ключ: `item:{id}`)
   - Кешируется при первом обращении к товару по ID
   - TTL: 120 секунд
   - При отсутствии в кеше загружается из БД и сохраняется в кеш

### Настройка кеша

В `application.properties`:
```properties
cache.item.ttl=120  # TTL в секундах
```

## OpenAPI спецификация

OpenAPI схема Payment Service находится в файле:
```
api-specs/payment-service-api.yaml
```

На основе этой спецификации автоматически генерируются:
- **HTTP клиент** для Market App (WebClient)
- **REST контроллер** для Payment Service

Генерация происходит автоматически при сборке проекта через `openapi-generator-maven-plugin`.


### Auth Server (application.properties)
```properties
# Порт сервера
server.port=9000
```

### Получение токена

```bash
curl -X POST http://localhost:9000/oauth2/token \
  -u market-app-client:market-app-secret \
  -d "grant_type=client_credentials&scope=payment.read payment.write"
```

### Использование токена

```bash
curl -X GET "http://localhost:8081/api/v1/payments/balance?username=user" \
  -H "Authorization: Bearer {access_token}"
```
