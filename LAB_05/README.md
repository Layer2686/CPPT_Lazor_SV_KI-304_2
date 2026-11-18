# Лабораторна №5 — сховище та експорт метеоданих

Варіант 18, Java 21, Maven 3.9.9 Wrapper, версія **1.4.0**.
Окремий модуль LAB_05; LAB_01–LAB_04 запускаються незалежно.

Реалізовано Repository<T>, власний виняток, @CsvColumn, універсальні CSV/JSON
експортери, документний CSV-парсер, імпорт і round-trip. Зірочка: спільний
Exporter<T>, кеш полів і [вимірювання](BENCHMARK.md).

## Збірка

Усі команди — **з кореня репозиторію**. Потрібний JDK 21;
для першого завантаження Maven і залежностей потрібен інтернет.

Linux/macOS:

```sh
./mvnw -B -pl LAB_05 -am test
./mvnw -B -pl LAB_05 -am package
./mvnw -B -pl LAB_05 -am verify
```

Windows PowerShell:

```powershell
.\mvnw.cmd -B -pl LAB_05 -am test
.\mvnw.cmd -B -pl LAB_05 -am package
.\mvnw.cmd -B -pl LAB_05 -am verify
```

Verify включає компіляцію, JUnit, пакування й SpotBugs. `clean` необов’язковий:
видаляє target перед збіркою. Усі модулі: `./mvnw -B clean verify`
або `.\mvnw.cmd -B clean verify`. Із каталогу LAB_05 Wrapper — `../mvnw`
або `..\mvnw.cmd`, JAR — `target/lab05.jar`.

## Запуск

Java-команди однакові для Linux, macOS та PowerShell. Для українського
консольного виводу в Windows перед запуском:

```powershell
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
```

Стандартний аналіз, як у попередній роботі:

```sh
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_05/target/lab05.jar --input LAB_05/data/input.csv --output LAB_05/out/report.txt
```

Експорт усіх коректних записів у два формати та перевірка читання назад:

```sh
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_05/target/lab05.jar --input LAB_05/data/queries-example.csv --output LAB_05/out/report.txt --export-csv LAB_05/out/weather.csv --export-json LAB_05/out/weather.json --round-trip
```

Очікується `Round-trip: true (записів: 7)`. Папки створюються автоматично.
Імпорт нового CSV та попередні аналізи:

```sh
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_05/target/lab05.jar --import-csv LAB_05/out/weather.csv --output LAB_05/out/imported-report.txt --danger --queries
java -jar LAB_05/target/lab05.jar --version
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_05/target/lab05.jar --help
```

| Параметр | Призначення |
| --- | --- |
| --input ШЛЯХ | Старий CSV без заголовка з `;` |
| --import-csv ШЛЯХ | Новий CSV із заголовком і комами; замість --input |
| --output ШЛЯХ | Стандартний текстовий звіт |
| --export-csv ШЛЯХ | Експорт коректних вимірювань у новий CSV |
| --export-json ШЛЯХ | Експорт тих самих значень у JSON |
| --round-trip | Прочитати --export-csv назад і порівняти повні списки |
| --danger / --queries | Попередні додаткові аналізи в консолі |
| --help / --version | Окремий запуск довідки чи версії |

Повторений шлях використовує останнє значення. --round-trip потребує
--export-csv. Шляхи джерела, звіту й експортів мають відрізнятися.
Без аргументів запускати з LAB_05: вхід data/input.csv, звіт out/report.txt.
Відносні шляхи рахуються від поточного каталогу. Помилки пояснюються в stderr;
успішність нових операцій у скриптах слід підтверджувати також створеними файлами
й повідомленням round-trip, оскільки збережено попередню обробку помилок Main.

## Два різні CSV-контракти

Старий --input: UTF-8, без заголовка, `date;temperature;humidity;pressure;wind`.
Новий --export-csv / --import-csv: UTF-8 без BOM, коми, заголовок, LF.
Порядок за Java-іменами полів: date, humidity, pressure, temperature, wind.

```csv
дата,вологість,тиск,температура,вітер
2026-09-01,70.0,1013.0,10.0,7.5
```

Фактично згенеровані JAR приклади: [CSV](data/export-example.csv),
[JSON](data/export-example.json). Вони містять 7 об’єктів, зокрема повторену дату.
[Приклад іншого анотованого класу](data/quoted-export-example.csv) згенерований
тим самим експортером: кома/лапки/multiline; 4 фізичних рядки, 2 об’єкти.

Коми, лапки та CR/LF усередині значення вимагають зовнішніх лапок;
внутрішні лапки подвоюються. Парсер читає весь документ, не split/readAllLines.
Переноси всередині клітинки зберігаються точно; межі нових записів — LF
на всіх ОС. При імпорті також дозволено CRLF/CR. Заголовок точний, порядок
не переставляється. Порожній файл/header-only дають порожній список.
Неправильний заголовок, лапки, число, дата або інваріант — DataStorageException.

## Модель, універсальність та помилки

WeatherReadingRecord — final-клас, п’ять private final полів з @CsvColumn:
LocalDate та чотири double. Null-дата заборонена, температура скінченна,
вологість 0–100 %, тиск >0, вітер >=0; NaN/Infinity відхиляються.
Equals/hashCode враховують усі значення. FromReading/toReading зберігають
попередню sealed-модель і вибір підтипу через Builder.

Repository.all — незмінний незалежний знімок структури списку;
find приймає Predicate<? super T>. Об’єкти не клонуються.
Експортери не знають погоди: анотовані declared-поля відбираються через
рефлексію, static/неанотовані пропускаються. Успадковані/вкладені поля
не обходяться. Назви колонок непорожні й унікальні, доступ перевірений.
CSV null-поле означає порожню клітинку, JSON — null; null-запис відхиляється.
JSON зберігає числа/boolean; текст, дати й enum — рядки. NaN/Infinity та
вкладені JSON-об’єкти відхиляються. Файли перезаписуються (не append).
DataStorageException зберігає контекст і cause технічної помилки.

Ключ прикладу — date; порівнюються повні списки, тому повторені дати не
втрачаються в Map. Enum не є полем варіанта 18; помилки enum перевірені
на незалежному допоміжному типі в тестах.

## Перевірки, пояснення та Javadoc

Локальний clean verify: 302 тести всіх модулів, з них 116 у LAB_05;
failures/errors/skipped — 0, SpotBugs без зауважень. Збережені 83 попередні
тести LAB_05, додано 33 нові. Старі standard/danger/queries console/file
перевіряються за [baseline](src/test/resources/baseline).
CI перевіряє всі модулі на Ubuntu/Windows/macOS, запускає JAR, парсить JSON,
порівнює CSV й звіти та публікує JAR і згенеровані файли.

- [Пояснення всієї реалізації](CODE_GUIDE.md).
- [План і критерії](PLAN.md).
- [Benchmark кешу](BENCHMARK.md).
- [GitHub Actions](https://github.com/Layer2686/CPPT_Lazor_SV_KI-304_2/actions).

Linux/macOS:

```sh
javadoc -Werror -encoding UTF-8 -charset UTF-8 -docencoding UTF-8 -d LAB_05/target/apidocs LAB_05/src/main/java/ua/lpnu/kzp/lab05/*.java
```

PowerShell:

```powershell
$sources = @(Get-ChildItem LAB_05/src/main/java -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
javadoc -Werror -encoding UTF-8 -charset UTF-8 -docencoding UTF-8 -d LAB_05/target/apidocs @sources
```

Відкрити LAB_05/target/apidocs/index.html. Target/out, локальні методички
та приватні матеріали не публікуються.
