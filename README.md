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

Отдельный интеграционный сценарий, запускаемый тем же модулем `grpc-e2e`, проверяет публичное gRPC API order-service. Он напрямую создаёт три заказа одного продавца в базе order-service, подключается к gRPC-серверу (по умолчанию `localhost:9091` или `ANALYTICS_GRPC_TARGET`), отправляет данные через `processOrder` и затем вызывает метод агрегации заказов продавца. Тест ожидает, что сервис вернёт агрегат с тремя заказами и корректной суммой по выручке и количеству товаров.
