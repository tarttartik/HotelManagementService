# Distributed Hotel Booking System

Распределённая бэкенд-система, поддерживающая функции управления базой отелей и бронирования комнат.

## Архитектура

Система состоит из нескольких микросервисов:

- **Hotel Service** — CRUD для отелей и API для проверки доступности комнат  
- **Booking Service** — управление пользователями и бронированиями  
- **Eureka Server** — динамическое обнаружение сервисов  
- **API Gateway** — маршрутизация запросов и проксирование JWT  

## Основные возможности

- JWT-аутентификация  
- Встроенная in-memory база данных  
- Идемпотентность бронирования по `requestId`  
- Компенсация при сбоях  
- Получение статистики по популярности отелей и комнат  
- Повторы и таймауты при межсервисных вызовах  
- Автоматический подбор комнаты  
- Разграничение ролей:
  - `ADMIN` — управление пользователями и отелями  
  - `USER` — создание и просмотр бронирований  

---

## Запуск

1. **Запустить Eureka Server**
   ```bash
   mvn -pl eureka-server spring-boot:run
   ```

2. **Запустить API Gateway**
   ```bash
   mvn -pl api-gateway spring-boot:run
   ```

3. **Запустить Hotel Service и Booking Service**
   ```bash
   mvn -pl hotel-service spring-boot:run
   mvn -pl booking-service spring-boot:run
   ```

4. **Отправлять запросы согласно эндпоинтам ниже**

---

## Порты

| Сервис | Порт |
|--------|------|
| Eureka Server | 8761 |
| API Gateway | 8080 |
| Hotel Service | случайный (через Eureka) |
| Booking Service | случайный (через Eureka) |

> API Gateway маршрутизирует запросы к сервисам через **Eureka**, проксируя заголовок `Authorization` (JWT).

---

## Эндпоинты

### Hotel Service

#### `/api/hotels`
| Метод | Эндпоинт | Описание |
|--------|-----------|-----------|
| `POST` | `/api/hotels` | Добавить отель |
| `PUT` | `/api/hotels/{id}` | Обновить данные отеля |
| `DELETE` | `/api/hotels/{id}` | Удалить отель |
| `GET` | `/api/hotels` | Получить все отели |
| `GET` | `/api/hotels/{id}` | Получить отель по ID |
| `GET` | `/api/hotels/stats` | Статистика популярности отелей |
| `GET` | `/api/hotels/availableRooms/{id}` | Список доступных комнат в отеле |

#### `/api/rooms`
| Метод | Эндпоинт | Описание |
|--------|-----------|-----------|
| `POST` | `/api/rooms` | Добавить комнату |
| `POST` | `/api/rooms/hold/{id}` | Удержание комнаты |
| `POST` | `/api/rooms/confirm/{id}` | Подтверждение удержания |
| `POST` | `/api/rooms/release/{id}` | Отмена удержания |
| `PUT` | `/api/rooms/{id}` | Обновить данные комнаты |
| `DELETE` | `/api/rooms/{id}` | Удалить комнату |
| `GET` | `/api/rooms/{id}` | Получить данные комнаты |
| `GET` | `/api/rooms/stats` | Статистика по популярности комнат |
| `GET` | `/api/rooms/topAvailable` | Самая популярная доступная комната |

---

### Booking Service

#### `/auth`
| Метод | Эндпоинт | Описание |
|--------|-----------|-----------|
| `POST` | `/auth/register` | Регистрация пользователя |
| `POST` | `/auth/login` | Получение JWT токена |

#### `/admin/users` (только для ADMIN)
| Метод | Эндпоинт | Описание |
|--------|-----------|-----------|
| `GET` | `/admin/users` | Получить всех пользователей |
| `GET` | `/admin/users/{id}` | Получить пользователя по ID |
| `PUT` | `/admin/users/{id}` | Обновить данные пользователя |
| `DELETE` | `/admin/users/{id}` | Удалить пользователя |

#### `/bookings`
| Метод | Эндпоинт | Описание |
|--------|-----------|-----------|
| `POST` | `/bookings` | Создать бронирование |
| `GET` | `/bookings` | Получить все бронирования пользователя |

---

## Примеры использования

### Регистрация администратора
```bash
curl -X POST http://localhost:8080/auth/register   -H 'Content-Type: application/json'   -d '{"username":"sofya","password":"123", "admin":true}'
```

### Вход и получение JWT
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login   -H 'Content-Type: application/json'   -d '{"username":"user1","password":"pass"}' | jq -r .access_token)
```

### Создание отеля и комнаты
```bash
curl -X POST http://localhost:8080/api/hotels   -H "Authorization: Bearer $TOKEN"   -H 'Content-Type: application/json'   -d '{"name":"Paradise Capital","city":"Moscow","address":"Tverskaya, 20"}'

curl -X POST http://localhost:8080/api/rooms   -H "Authorization: Bearer $TOKEN"   -H 'Content-Type: application/json'   -d '{"number":"110", "available":true}'
```

### Создание бронирования
```bash
curl -X POST http://localhost:8080/bookings   -H "Authorization: Bearer $TOKEN"   -H 'Content-Type: application/json'   -d '{"roomId":1, "startDate":"2025-11-05", "endDate":"2025-11-10", "requestId":"req-123"}'
```

### Просмотр истории бронирований
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/bookings
```

---

## Аутентификация

Для демонстрации используется HMAC JWT.  
Секрет задаётся свойством `security.jwt.secret`.

Для реального использования необходимо заменить секрет (по умолчанию `dev-secret-please-change`)  
в файлах:
- `hotel-service/src/main/resources/application.yml`
- `booking-service/src/main/resources/application.yml`

---

## Тестирование

Запуск юнит-тестов:
```bash
mvn -q -DskipTests=false test
```

---

## Дальнейшее развитие

- [ ] Добавить фронтенд  
- [ ] Перейти на продвинутую аутентификацию (например, OAuth2)  
- [ ] Добавить отзывы и рейтинги для отелей и комнат  
- [ ] Повысить отказоустойчивость, возможно с использованием брокера сообщений  
- [ ] Расширить тестовое покрытие (юнит и интеграционные тесты)
