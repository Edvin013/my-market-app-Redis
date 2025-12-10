# My Market App - Multi-Module Project

Мультимодульное реактивное приложение интернет-магазина на основе Spring Boot WebFlux с интеграцией Redis для кеширования и отдельным сервисом платежей.

## Архитектура проекта

Проект состоит из двух модулей:

### 1. Market App (основное приложение)
- Витрина товаров с поиском, сортировкой и пагинацией
- Управление корзиной
- Оформление заказов с интеграцией сервиса платежей
- **Кеширование товаров в Redis** для повышения производительности
- Просмотр истории заказов

### 2. Payment Service (сервис платежей)
- RESTful API для обработки платежей
- Проверка баланса
- Списание средств при оформлении заказа
- Реализован на основе **OpenAPI спецификации**

## Возможности приложения

### Market App
- ✅ Просмотр каталога товаров с поиском и сортировкой
- ✅ Кеширование списка товаров в Redis (TTL: 2 минуты)
- ✅ Кеширование отдельных товаров в Redis
- ✅ Просмотр карточки товара с детальной информацией
- ✅ Управление корзиной: добавление, удаление и изменение количества товаров
- ✅ Проверка баланса перед оформлением заказа
- ✅ Оформление заказа с автоматической оплатой через Payment Service
- ✅ Просмотр истории заказов

### Payment Service
- ✅ REST API для получения баланса
- ✅ REST API для обработки платежей
- ✅ Автоматическая генерация клиентского и серверного кода из OpenAPI спецификации
- ✅ Валидация достаточности средств
- ✅ Генерация уникальных идентификаторов транзакций

## Технологический стек

### Market App
- **Java 21**
- **Spring Boot 3.5.6**
- **Spring WebFlux** (реактивный веб-фреймворк)
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
- **OpenAPI Generator** (генерация серверного кода)
- **Jackson** (JSON сериализация)

### Общее
- **Lombok** (уменьшение boilerplate кода)
- **JUnit 5** (тестирование)
- **Reactor Test** (тестирование реактивных потоков)
- **Embedded Redis** (для интеграционных тестов)
- **MockWebServer** (для тестирования HTTP клиента)
- **Maven** (сборка мультимодульного проекта)
- **Docker & Docker Compose** (контейнеризация)

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

Это соберет оба модуля:
- `market-app/target/market-app-0.0.1-SNAPSHOT.jar`
- `payment-service/target/payment-service-0.0.1-SNAPSHOT.jar`

### Запуск с помощью Docker Compose (рекомендуется)

Docker Compose автоматически запустит все необходимые сервисы:
- PostgreSQL (порт 5432)
- Redis (порт 6379)
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

#### 2. Запустите Payment Service

```bash
cd payment-service
java -jar target/payment-service-0.0.1-SNAPSHOT.jar
```

Payment Service будет доступен на порту 8081.

#### 3. Запустите Market App

Обновите `market-app/src/main/resources/application.properties`:
```properties
spring.r2dbc.url=r2dbc:postgresql://localhost:5432/marketdb
spring.data.redis.host=localhost
payment.service.url=http://localhost:8081
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

Тесты используют:
- H2 in-memory базу данных (вместо PostgreSQL)
- Embedded Redis (для тестов кеширования)
- MockWebServer (для тестов интеграции с Payment Service)

## API эндпоинты

### Market App (порт 8080)
- `GET /` или `GET /items` - список товаров с поддержкой поиска, сортировки и пагинации
- `GET /items/{id}` - карточка конкретного товара
- `GET /cart/items` - содержимое корзины (с балансом и доступностью Payment Service)
- `POST /cart/items` - обновление корзины
- `POST /buy` - оформление заказа (включает проверку баланса и оплату)
- `GET /orders` - список всех заказов
- `GET /orders/{id}` - детали конкретного заказа

### Payment Service (порт 8081)
- `GET /api/v1/payments/balance` - получить текущий баланс
- `POST /api/v1/payments/process` - обработать платеж

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

## Структура проекта

```
my-market-app-Redis/
├── pom.xml                          # Родительский POM
├── api-specs/
│   └── payment-service-api.yaml     # OpenAPI спецификация
├── market-app/                      # Основное приложение
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── ru/mirakyan/mymarket/
│       │   │       ├── config/
│       │   │       │   ├── RedisConfig.java
│       │   │       │   └── PaymentClientConfig.java
│       │   │       ├── service/
│       │   │       │   ├── ItemCacheService.java
│       │   │       │   ├── PaymentClient.java
│       │   │       │   └── impl/
│       │   │       └── ...
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│           ├── java/
│           │   └── ru/mirakyan/mymarket/
│           │       ├── config/
│           │       │   └── EmbeddedRedisConfig.java
│           │       └── service/
│           │           ├── ItemCacheServiceIntegrationTest.java
│           │           └── PaymentClientIntegrationTest.java
│           └── resources/
│               └── application.properties
├── payment-service/                 # Сервис платежей
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── ru/mirakyan/mymarket/payment/
│       │   │       ├── PaymentServiceApplication.java
│       │   │       ├── controller/
│       │   │       │   └── PaymentApiDelegateImpl.java
│       │   │       └── service/
│       │   │           ├── PaymentService.java
│       │   │           └── PaymentServiceImpl.java
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│           └── java/
│               └── ru/mirakyan/mymarket/payment/
│                   └── service/
│                       └── PaymentServiceTest.java
└── docker-compose.yml
```

## База данных
- **Production**: PostgreSQL с реактивным драйвером R2DBC
- **Tests**: H2 in-memory с реактивным драйвером R2DBC

## Конфигурация

### Market App (application.properties)
```properties
# Порт сервера
server.port=8080

# PostgreSQL
spring.r2dbc.url=r2dbc:postgresql://postgres:5432/marketdb
spring.r2dbc.username=market
spring.r2dbc.password=market

# Redis
spring.data.redis.host=redis
spring.data.redis.port=6379

# Кеш
cache.item.ttl=120

# Payment Service
payment.service.url=http://payment-service:8081
payment.service.timeout=5000
```

### Payment Service (application.properties)
```properties
# Порт сервера
server.port=8081

# Начальный баланс (в копейках)
payment.initial-balance=1000000
```

## Тестирование

### Покрытие тестами

**Market App:**
- Юнит-тесты сервисов
- Интеграционные тесты контроллеров
- **Интеграционные тесты кеширования** (ItemCacheServiceIntegrationTest)
- **Интеграционные тесты Payment Client** (PaymentClientIntegrationTest)

**Payment Service:**
- Юнит-тесты сервисов (PaymentServiceTest)
- Интеграционные тесты контроллеров

### Особенности тестов

- Используется **контекстное кеширование** Spring Boot Test для ускорения выполнения
- Embedded Redis для изоляции тестов кеширования
- MockWebServer для тестирования HTTP интеграций
- StepVerifier для верификации реактивных потоков

## Производительность

### Преимущества использования Redis
- **Снижение нагрузки на БД**: Часто запрашиваемые товары берутся из кеша
- **Ускорение ответа**: Redis работает в памяти, что значительно быстрее БД
- **Масштабируемость**: При росте трафика кеш помогает обрабатывать больше запросов

### Метрики
- TTL кеша: 120 секунд (настраивается)
- Cache Hit Rate: зависит от паттернов использования
- Время ответа из кеша: ~1-5 мс
- Время ответа из БД: ~10-50 мс

## Особенности реактивной версии
- Все операции с базой данных неблокирующие (non-blocking)
- Все операции с Redis неблокирующие
- HTTP запросы в Payment Service неблокирующие
- Используется Netty вместо Tomcat как встроенный веб-сервер
- Контроллеры возвращают `Mono` и `Flux` для асинхронной обработки
- Тесты используют `StepVerifier` для верификации реактивных потоков

## Логирование

Уровни логирования настраиваются в `application.properties`:
```properties
logging.level.ru.mirakyan.mymarket=DEBUG
logging.level.org.springframework.web=INFO
```

Логируются:
- Операции кеширования (cache HIT/MISS)
- Запросы к Payment Service
- Обработка платежей
- Ошибки и исключения

## Troubleshooting

### Redis недоступен
Если Redis недоступен, приложение продолжит работать, но товары будут загружаться только из БД.

### Payment Service недоступен
Если Payment Service недоступен:
- Баланс отображается как 0
- Кнопка оформления заказа недоступна
- Показывается сообщение о недоступности сервиса

### Проблемы с портами
Убедитесь, что порты не заняты:
- 8080 - Market App
- 8081 - Payment Service
- 5432 - PostgreSQL
- 6379 - Redis

## Авторы
Market App Team

## Лицензия
MIT License

