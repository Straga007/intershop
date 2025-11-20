# Интернет-магазин с системой авторизации

## Описание

Проект представляет собой интернет-магазин с системой авторизации пользователей и OAuth2 для взаимодействия между сервисами. 
Основные компоненты:

1. Основное приложение (витрина интернет-магазина)
2. Сервис платежей
3. Сервер авторизации Keycloak

## Требования

- Java 17+ (проверялось на 21)
- Docker и Docker Compose
- Gradle

## Переменные окружения

Проект использует переменные окружения для конфигурации секретов. Вы можете установить их вручную или использовать файл [.env](.env) в корневом каталоге проекта.

### Переменные окружения:

- `KEYCLOAK_CLIENT_ID` - Client ID для основного приложения (по умолчанию: myclient)
- `KEYCLOAK_CLIENT_SECRET` - Client Secret для основного приложения (по умолчанию: myclientsecret)
- `PAYMENT_SERVICE_CLIENT_ID` - Client ID для сервиса платежей (по умолчанию: paymentservice)
- `PAYMENT_SERVICE_CLIENT_SECRET` - Client Secret для сервиса платежей (по умолчанию: paymentserviceclientsecret)
- `KEYCLOAK_ADMIN_USER` - Имя пользователя администратора Keycloak (по умолчанию: admin)
- `KEYCLOAK_ADMIN_PASSWORD` - Пароль администратора Keycloak (по умолчанию: admin)

## Запуск приложения

### 1. Запуск Keycloak сервера авторизации

```bash
cd keycloak
mkdir data
docker-compose up -d
```

Keycloak будет доступен по адресу: http://localhost:8080
Админ-панель: http://localhost:8080/admin
Логин/пароль администратора: admin/admin (если не переопределены переменными окружения)

### 2. Запуск основного приложения

```bash
cd main-app
../gradlew bootRun
```

Основное приложение будет доступно по адресу: http://localhost:8080

### 3. Запуск сервиса платежей

```bash
cd payment-service
../gradlew bootRun
```

Сервис платежей будет доступен по адресу: http://localhost:8081

## Пользователи системы

### Пользователи для входа в основное приложение:
- Логин: user, Пароль: password (роль: USER)
- Логин: admin, Пароль: admin (роль: ADMIN)

### Клиенты OAuth2:
- Клиент для основного приложения:
  - Client ID: ${KEYCLOAK_CLIENT_ID} (по умолчанию: myclient)
  - Client Secret: ${KEYCLOAK_CLIENT_SECRET} (по умолчанию: myclientsecret)
  
- Клиент для сервиса платежей:
  - Client ID: ${PAYMENT_SERVICE_CLIENT_ID} (по умолчанию: paymentservice)
  - Client Secret: ${PAYMENT_SERVICE_CLIENT_SECRET} (по умолчанию: paymentserviceclientsecret)

## Функциональность

### Основное приложение (витрина интернет-магазина)

- Просмотр каталога товаров
- Просмотр деталей товара
- Добавление товаров в корзину
- Оформление заказов
- Просмотр истории заказов

Функции доступны только авторизованным пользователям:
- Добавление товаров в корзину
- Оформление заказов
- Просмотр корзины и истории заказов

### Сервис платежей

- Получение баланса пользователя
- Обработка платежей

Доступ к сервису только по токену OAuth2 (Client Credentials Flow)

## Архитектура безопасности

### Авторизация пользователей

Используется Spring Security с авторизацией через Keycloak (OpenID Connect).
Пользователи хранятся в Keycloak.

### Межсервисная авторизация

Используется OAuth2 Client Credentials Flow:
1. Основное приложение получает токен у Keycloak
2. При обращении к сервису платежей передает токен
3. Сервис платежей проверяет токен через Keycloak

## Технологии

- Spring Boot 3
- Spring Security
- Spring WebFlux (реактивное программирование)
- OAuth2/OpenID Connect
- Thymeleaf (шаблонизация)
- H2 Database
- Redis (кеширование)
- Keycloak (сервер авторизации)
- Docker

## Структура проекта

```
├── main-app/                 # Основное приложение (витрина)
├── payment-service/          # Сервис платежей
├── keycloak/                 # Конфигурация Keycloak
│   ├── realm-export.json     # Экспорт realm для Keycloak
│   └── docker-compose.yml    # Docker-конфигурация Keycloak
└── teory.txt                 # Теоретический материал
```