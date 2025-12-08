# Как писал интеграционный gRPC-тест

Ниже — пошаговый план того, как я бы действовал, если бы впервые пришёл на проект и мне нужно было добавить интеграционный тест
уровня «service-to-service» между Order Service (gRPC-сервер) и клиентом.

1. **Запрашиваю proto-файл.** Беру `analytics-proto/src/main/proto/order_aggregation.proto` у команды или из репозитория и
   складываю его в модуль, с которым пишу тесты.
2. **Генерирую модели и клиент.** Из корня запускаю `./gradlew :analytics-proto:build` и подключаю зависимость на получившийся артефакт,
   чтобы получить `OrderAggregationServiceGrpc` и модели `SellerAggregateRequest`/`SellerAggregateResponse`.
3. **Поднимаю инфраструктуру.** Запускаю `docker compose up` из корня, чтобы стартовали PostgreSQL для обоих сервисов, Kafka и сам
   order-service с открытым gRPC-портом (`9090`).
4. **Заполняю базу Order Service.** Через JDBC (см. `OrderTables.kt`) записываю связанные строки в таблицы `orders`, `order_items`,
   `order_geo`, чтобы Order Service имел «боевые» данные для агрегации.
5. **Настраиваю gRPC-клиент.** В тесте строю канал `ManagedChannelBuilder.forTarget("localhost:9090").usePlaintext()` и получаю
   `OrderAggregationServiceGrpc.OrderAggregationServiceBlockingStub`.
6. **Делаю gRPC-вызов.** Формирую `SellerAggregateRequest` с нужным sellerId и вызываю `getSellerAggregate`, который читает и агрегирует
   данные Order Service без записи в другие базы.
7. **Проверяю агрегат.** Убеждаюсь, что вернулся агрегированный ответ с ожидаемыми полями (`ordersCount`, `totalItems`, `totalAmount`).
8. **Прибираю за собой.** Чищу тестовые записи из базы заказов (`deleteOrder`) в `@AfterEach`, чтобы тест оставался идемпотентным.

## Allure: как включить и посмотреть шаги теста

1. **Зависимость уже подключена.** В `grpc-e2e/build.gradle.kts` добавлен `io.qameta.allure:allure-junit5`, а сам тест помечен `@ExtendWith(AllureJunit5::class)`,
   поэтому шаги (`Allure.step`) пишутся автоматически.
2. **Запусти тесты.** Выполни `./gradlew :grpc-e2e:test` (в окружении должны быть подняты сервисы и БД, см. шаги выше). В каталоге `grpc-e2e/build/allure-results`
   появятся json-файлы с результатами.
3. **Сгенерируй отчёт.** Убедись, что установлен CLI Allure (например, `brew install allure` или `scoop install allure`). Затем запусти:
   - `allure generate grpc-e2e/build/allure-results -o grpc-e2e/build/allure-report --clean`
4. **Открой отчёт локально.** Выполни `allure open grpc-e2e/build/allure-report` и перейди по открывшемуся адресу. Там будут видны тесты и детальные шаги с описанием подключения,
   запроса, ответа и закрытия канала.
