# Лабораторна №5 — сховище, винятки та CSV-експорт

Варіант 18 — метеостанція; Java 21, пакет ua.lpnu.kzp.lab05.
Гілка lab05, версія **1.4.0-SNAPSHOT**.

**Стан: основа LAB_04 та підключене Repository<T>.**
Анотація, рефлексивний експорт, новий CSV-парсер, імпорт,
JSON і кеш ще не реалізовані. --danger та --queries успадковані й працюють.
Зелений CI основи не означає готовності п’ятої лабораторної.

## Збірка

Потрібні Git, JDK 21 та інтернет для першої збірки. Maven 3.9.9 завантажує
Wrapper. Команди **з кореня репозиторію**:

Linux/macOS:

```sh
./mvnw -B -pl LAB_05 -am verify
```

Windows PowerShell:

```powershell
.\mvnw.cmd -B -pl LAB_05 -am verify
```

Verify включає компіляцію, JUnit, пакування JAR та SpotBugs. Для всіх п’яти
модулів — `./mvnw -B verify` (Windows: `.\mvnw.cmd -B verify`).
Clean за потреби видаляє target, але не обов’язковий перед кожною збіркою.
Окремі фази: `./mvnw -B -pl LAB_05 -am test` та `./mvnw -B -pl LAB_05 -am package`.

## Запуск робочої основи

Ці java-команди підходять Linux/macOS та PowerShell:

```sh
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_05/target/lab05.jar --input LAB_05/data/input.csv --output LAB_05/out/report.txt
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_05/target/lab05.jar --input LAB_05/data/danger-example.csv --output LAB_05/out/report.txt --danger
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_05/target/lab05.jar --input LAB_05/data/queries-example.csv --output LAB_05/out/report.txt --queries
java -jar LAB_05/target/lab05.jar --version
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_05/target/lab05.jar --help
```

Перед українським виводом у Windows PowerShell:

```powershell
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
```

Без аргументів запускати з папки LAB_05: типовий вхід data/input.csv,
звіт out/report.txt. Шляхи відносні до поточного каталогу, батьківські папки
звіту створюються автоматично. Із папки LAB_05 збірка запускається через
`../mvnw -B verify`, JAR — `java -jar target/lab05.jar`.

Поки доступні лише попередні параметри: --input, --output, --danger,
--queries, --help та --version. Нові параметри експорту/імпорту погодимо
після реалізації компонентів. Локальна версія —
`lab05 1.4.0-SNAPSHOT (build local)`, у CI — номер збірки.

## Старий вхід та новий експорт — два контракти

Старі input.csv, danger-example.csv та queries-example.csv залишаються
UTF-8 без заголовка, роздільник `;`, порядок date;temperature;humidity;pressure;wind.
Валідація, LocalDate, підтипи й результати запитів четвертої роботи збережені.

Новий CSV-експорт за методичкою матиме **коми, заголовок та екранування**.
Планована окрема плоска модель WeatherReadingRecord: date LocalDate,
temperature/humidity/pressure/wind double. Вона представляє зберігання,
а попередня sealed-модель лишається для роботи й запитів. Це не вимога
замінити WeatherReading на Java record; анотації FIELD зручно додати до
полів окремого незмінного final класу з equals/hashCode.

При сортуванні рефлексивних полів за Java-іменем порядок буде:
date, humidity, pressure, temperature, wind. Відповідний заголовок:

```text
дата,вологість,тиск,температура,вітер
```

[Ручний еталон export-example.csv](data/export-example.csv) — специфікація,
а не результат вже реалізованого експорту. Він не є входом старого --input.
Порожній експорт має містити заголовок завдяки явному Class<T>.
Ключ демонстрації round-trip — date; порівнюється весь список за equals
із збереженням порядку й дублікатів, а не карта за унікальною датою.

Метеозапис не містить довільного тексту чи enum у своїх п’яти колонках.
Тому універсальність, коми, лапки, multiline й помилки enum перевірятимемо
на допоміжних тестових типах, не додаючи штучних полів до варіанта.
[quoted-example.csv](src/test/resources/csv/quoted-example.csv) — ручний
приклад: 4 фізичних рядки, 3 CSV-записи разом із заголовком, 2 об’єкти.
Документ потрібно читати цілком: readAllLines/split не розбирають quoted multiline.

## Збережений контрольний вивід

У [src/test/resources/baseline](src/test/resources/baseline) збережено
фактичні результати LAB_04 до змін:

- standard-console.txt / standard-report.txt — input.csv;
- danger-console.txt / danger-report.txt — danger-example.csv з --danger;
- queries-console.txt / queries-report.txt — queries-example.csv з --queries.

Це еталони Linux із LF; на інших ОС у порівняннях слід враховувати системні
закінчення рядків. Старі дані/звіти перших чотирьох робіт збережені.

## Javadoc

Linux/macOS:

```sh
javadoc -Werror -encoding UTF-8 -charset UTF-8 -docencoding UTF-8 -d LAB_05/target/apidocs LAB_05/src/main/java/ua/lpnu/kzp/lab05/*.java
```

Windows PowerShell:

```powershell
$sources = @(Get-ChildItem LAB_05/src/main/java -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
javadoc -Werror -encoding UTF-8 -charset UTF-8 -docencoding UTF-8 -d LAB_05/target/apidocs @sources
```

Відкрити LAB_05/target/apidocs/index.html. Target/out та локальні методички
не комітяться. План і критерії — у [PLAN.md](PLAN.md).
Фінальний REPORT готується після реалізації й спочатку надається студенту
для перегляду; звіт попередньої роботи не копіюється як доказ готовності цієї.

## Перевірка основи

Локальний clean verify успішний: 269 тестів п’яти модулів, із них 83
у LAB_05; failures/errors/skipped — 0, SpotBugs без зауважень. Javadoc
-Werror проходить. Реальні JAR і збережені контрольні звіти перевірені.
Віддалений CI та його артефакти — у [Issue #68](https://github.com/Layer2686/CPPT_Lazor_SV_KI-304_2/issues/68).
Це результати підготовки, нових компонентів п’ятої роботи ще немає.

## Сховище Repository<T>

Контролер зберігає коректні вимірювання в Repository<WeatherReading>.
Add відхиляє null, all повертає незмінний незалежний знімок списку,
find приймає Predicate<? super T> і повертає збіги у початковому порядку.
Дублікати збережені. Самі об’єкти при отриманні знімка не клонуються.
Null-умова пошуку відхиляється NullPointerException.

Додано 4 тести Repository: LAB_05 — 87 тестів, локальні verify/SpotBugs
і Javadoc -Werror успішні. Перевірка незмінності попередніх звітів
пройшла для стандартного режиму, --danger та --queries.

## Власний виняток DataStorageException

Створено checked-виняток із двома конструкторами: повідомлення та
повідомлення з Throwable cause. GetMessage повертає контекст операції,
getCause зберігає початковий виняток. Додано 2 тести конструкторів;
LAB_05 — 89 тестів. Валідація існуючої моделі залишається IllegalArgumentException.
Контракт майбутніх помилок експорту/імпорту наведено в [плані](PLAN.md).
Власний виняток ще не підключений до нових CSV-операцій: ці компоненти
реалізуються в наступних Issues.
