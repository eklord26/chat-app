# Flyway Migrations — Connect Messenger

## Структура миграций

```
db/migration/
├── V1__create_users.sql         — Пользователи
├── V2__create_roles.sql         — Роли в чатах
├── V3__create_rights.sql        — Права ролей
├── V4__create_tokens.sql        — Токены аутентификации
├── V5__create_chats.sql         — Чаты
├── V6__create_chat_members.sql  — Участники чатов
├── V7__create_messages.sql      — Сообщения
└── V8__create_logs.sql          — Логи событий
```

## Порядок зависимостей

```
users ──────────────────────────────────────┐
  │                                         │
roles ──────────┐                           │
  │             │                           │
rights          │                         logs
              tokens                        │
                │                           │
              chats ◄── (owner → users)     │
                │                           │
           chat_members ◄── (users, roles)  │
                │                           │
           messages                         │
```

## Подключение Flyway к проекту (Gradle)

Добавьте в `build.gradle.kts`:

```kotlin
plugins {
    id("org.flywaydb.flyway") version "10.10.0"
}

dependencies {
    implementation("org.flywaydb:flyway-core:10.10.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.10.0")
}

flyway {
    url = "jdbc:postgresql://localhost:5432/chatapp"
    user = "postgres"
    password = "postgres"
    locations = arrayOf("filesystem:db/migration")
}
```

Или в `build.gradle`:

```groovy
plugins {
    id 'org.flywaydb.flyway' version '10.10.0'
}

dependencies {
    implementation 'org.flywaydb:flyway-core:10.10.0'
    implementation 'org.flywaydb:flyway-database-postgresql:10.10.0'
}

flyway {
    url = 'jdbc:postgresql://localhost:5432/chatapp'
    user = 'postgres'
    password = 'postgres'
    locations = ['filesystem:db/migration']
}
```

## Запуск миграций

```bash
# Через Gradle
./gradlew flywayMigrate

# Через Flyway CLI
flyway -configFiles=flyway.conf migrate

# Проверка статуса
flyway -configFiles=flyway.conf info

# Откат (только Flyway Teams)
flyway -configFiles=flyway.conf undo
```

## Куда положить файлы

Скопируйте папку `db/` в корень проекта:

```
chat-app/
├── src/
├── db/
│   └── migration/
│       ├── V1__create_users.sql
│       └── ...
├── flyway.conf
└── build.gradle.kts
```

## Примечание

В `UserTable` поле `deletedAt` объявлено без `.nullable()`, что означает NOT NULL.
Если в БД это поле должно быть опциональным — измените в `UserDAO.kt`:
```kotlin
val deletedAt = timestamp("deletedAt").nullable()
```
и соответственно в `V1__create_users.sql` замените `NOT NULL` на `NULL`.
