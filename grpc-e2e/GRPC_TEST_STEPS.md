# Как писал интеграционный gRPC-тест

Ниже — пошаговый план того, как я бы действовал, если бы впервые пришёл на проект и мне нужно было добавить интеграционный тест уровня «service-to-service» между Order Service и Analytics Service.

1. **Запрашиваю proto-файл.** Беру `analytics-proto/src/main/proto/analytics.proto` у команды или из репозитория и складываю его в модуль, с которым пишу тесты.
2. **Генерирую модели и клиент.** Из корня запускаю `./gradlew :analytics-proto:build` и подключаю зависимость на получившийся артефакт, чтобы получить `AnalyticsServiceGrpc` и модели `EnrichedOrder`, `SellerAggregate`.
3. **Поднимаю инфраструктуру.** Поднимаю `docker compose up` из корня, чтобы стартовали PostgreSQL для обоих сервисов, Kafka и сами order/analytics сервисы с открытым gRPC-портом Analytics (`9091`).
4. **Заполняю базу Order Service.** Через JDBC (см. `OrderTables.kt`) записываю связанные строки в таблицы `orders`, `order_items`, `order_geo`, чтобы Order Service имел «боевые» данные.
5. **Настраиваю gRPC-клиент.** В тесте строю канал `ManagedChannelBuilder.forTarget("localhost:9091").usePlaintext()` и получаю `AnalyticsServiceGrpc.AnalyticsServiceBlockingStub`.
6. **Делаю gRPC-вызов.** Формирую `EnrichedOrder` на основе только что положенных в БД данных (тот же sellerId/orderId) и вызываю `processOrder`, как это делает Order Service в реальности.
7. **Проверяю агрегат.** С тем же stub вызываю `getSellerAggregate` и убеждаюсь, что вернулся один агрегированный ответ с ожидаемыми полями (`totalOrders`, `totalItems`, `totalRevenue`).
8. **Прибираю за собой.** Чищу тестовые записи из обеих баз (`deleteOrder`, `SellerAggregateRepository.deleteBySellerId`) в `@AfterEach`, чтобы тест оставался идемпотентным.
