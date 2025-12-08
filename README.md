# Kotlin gRPC Kafka Example

Учебный пример с двумя Kotlin gRPC микросервисами, Kafka, PostgreSQL и внешним geo-service на WireMock. Проект включает:

- **Order Service**: читает заказы из Kafka, получает геоданные из WireMock и отправляет обогащённые заказы в Analytics Service по gRPC.
- **Analytics Service**: принимает заказы по gRPC, агрегирует статистику по продавцам и сохраняет агрегаты в PostgreSQL.
- **WireMock**: внешний geo-service, отдающий регион, город, таймзону и коэффициент.
- **grpc-e2e**: модуль с полным e2e-тестом.

## Сборка

```bash
./gradlew clean build
```

## Локальный стенд

Поднимите все компоненты через Docker Compose:

```bash
docker compose up --build
```

Будут запущены:

- Kafka (внутренний порт `29092`, внешний `9092`)
- PostgreSQL (`5433` -> `5432` в контейнере)
- WireMock (`8031` -> `8080` в контейнере) с ручкой `/geo?lat=...&lon=...`
- Analytics Service (gRPC порт `9091`)
- Order Service (читает Kafka, обращается к WireMock и Analytics Service)

Проверка WireMock:

```bash
curl "http://localhost:8031/geo?lat=55&lon=37"
```

## E2E-тест

Откройте новую консоль, когда Docker Compose уже работает:

```bash
./gradlew :grpc-e2e:test
```

Тест отправляет 3 заказа в Kafka-топик `big-communal-orders-topic`, ждёт обработки цепочкой **Order Service → WireMock → Analytics Service**, затем читает агрегат продавца из базы Analytics Service и проверяет сумму заказов.

### gRPC-интеграционный тест order-service

Интеграционный сценарий `grpc-e2e` проверяет публичное gRPC API order-service: напрямую создаёт три заказа одного продавца в базе order-service, подключается к gRPC-серверу order-service (по умолчанию `localhost:9090` или переменная окружения `ORDER_GRPC_TARGET`), вызывает метод получения агрегата продавца и проверяет, что вернулись три заказа и сумма 175.0. В этом тесте **не используется analytics-service**.

Чтобы сгенерировать gRPC-клиентские классы из `.proto`, запусти `gradlew.bat :analytics-proto:generateProto ` — артефакты будут доступны для тестов через модуль `analytics-proto`.
