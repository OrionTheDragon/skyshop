# HW25 — Введение в базы данных

Это самостоятельное Maven-приложение школы в пакете `ru.hogwarts.school`.
Исходного приложения школы не было, поэтому оно создано с нуля в отдельной
папке `school`. Приложение магазина и предыдущие домашние работы не изменены.

## PostgreSQL

Нужен запущенный PostgreSQL на `localhost:5432`. В Windows после установки
PostgreSQL 18 выполните из корня репозитория:

```powershell
& 'C:\Program Files\PostgreSQL\18\bin\psql.exe' -h localhost -U postgres -d postgres -W -v ON_ERROR_STOP=1 -f '.\school\database\setup.sql'
```

Введите локально пароль администратора `postgres`, заданный при установке.
При вводе пароля символы и звёздочки не отображаются.

Скрипт создаёт пользователя `student` с учебным паролем `chocolatefrog` и базу
`hogwarts`, владельцем которой является `student`. Существующие роли и базы
он не изменяет. Если они уже существуют, проверьте их настройки самостоятельно.
Альтернатива — запустить `school/database/setup.ps1`.

Подключение задаётся в `src/main/resources/application.properties`.
Рабочее свойство Hibernate — `spring.jpa.hibernate.ddl-auto=update`
(в тексте домашнего задания пропущено `-auto`).
Этот режим сохраняет данные после перезапуска. Учебный пароль не используйте
для рабочих баз данных.

## Запуск

Откройте `school/pom.xml` в IntelliJ IDEA как Maven-проект и запустите
`ru.hogwarts.school.SchoolApplication`. Либо из корня репозитория:

```shell
mvn -f school/pom.xml spring-boot:run
```

Нужны JDK 17+ и Maven. По умолчанию приложение слушает порт 8080.
Если магазин уже запущен на этом порту, остановите его либо используйте
`--server.port=8081` при запуске школы.

## API

| Метод | Путь | Назначение |
| --- | --- | --- |
| POST | /student | Создать студента: `{"name":"Harry","age":11}` |
| GET | /student/{id} | Получить студента |
| GET | /student | Получить всех студентов |
| GET | /student?age=11 | Фильтр по возрасту |
| PUT | /student | Изменить студента: `{"id":1,"name":"Harry","age":12}` |
| DELETE | /student/{id} | Удалить студента |
| POST | /faculty | Создать факультет: `{"name":"Gryffindor","color":"Red"}` |
| GET | /faculty/{id} | Получить факультет |
| GET | /faculty | Получить все факультеты |
| GET | /faculty?color=red | Фильтр по цвету без учёта регистра |
| PUT | /faculty | Изменить факультет: `{"id":1,"name":"Gryffindor","color":"Gold"}` |
| DELETE | /faculty/{id} | Удалить факультет |

Создание возвращает 201; чтение и изменение — 200; удаление — 204.
Отсутствующий идентификатор возвращает 404, PUT без id — 400.
При POST переданный id игнорируется: новый идентификатор генерирует БД.

## Проверка

```shell
mvn -f school/pom.xml verify
```

Unit-тесты проверяют вызовы репозиториев, JPA-тесты — сохранение и фильтры,
HTTP-тесты — полный CRUD и ошибки. Автотесты используют отдельную H2 в профиле
`test` и не изменяют локальную PostgreSQL.

Импортируйте `postman/Hogwarts.postman_collection.json` в Postman и запустите
Collection Runner. Укажите переменную `baseUrl` (по умолчанию
`http://localhost:8080`). Коллекция содержит 24 запроса с проверками,
сохраняет выданные БД id и удаляет только созданные ею записи.
