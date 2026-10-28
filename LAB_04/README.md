# Лабораторна №4 — підготовка Stream API

Варіант 18 — метеостанція. Java 21, пакет ua.lpnu.kzp.lab04,
робоча гілка lab04, версія **1.3.0-SNAPSHOT**.

**Стан: робоча основа LAB_03 з календарною датою LocalDate.**
Реалізовано filter, map дат, середню температуру за місяцями й температурну
статистику; основні циклові обчислення звіту замінено потоками.
Додано top-N, пошук Optional та зірочку — дворівневе групування.
Користувацька демонстрація нових запитів ще не підключена. Наявний --danger
успадковано з третьої роботи. Зелена збірка цієї основи не означає завершення LAB_04.

## Збірка

Потрібні Git і JDK 21; Maven 3.9.9 завантажує спільний Wrapper.
Встановлення середовища — у [кореневому README](../README.md).
Усі команди виконуються **з кореня репозиторію**.

Linux/macOS:

```sh
./mvnw -B -pl LAB_04 -am verify
```

Windows PowerShell:

```powershell
.\mvnw.cmd -B -pl LAB_04 -am verify
```

Verify компілює програму, запускає JUnit, пакує виконуваний JAR та перевіряє
SpotBugs. Для всіх чотирьох робіт — `./mvnw -B verify` або `.\mvnw.cmd -B verify`.
Clean можна додати перед verify для видалення попередніх результатів.
Перші три роботи можна й далі збирати окремо з -pl LAB_01, LAB_02 або LAB_03.

## Запуск поточної основи

Java-команди однакові для Linux/macOS і PowerShell:

```sh
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_04/target/lab04.jar --input LAB_04/data/input.csv --output LAB_04/out/report.txt
java -jar LAB_04/target/lab04.jar --version
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_04/target/lab04.jar --help
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_04/target/lab04.jar --input LAB_04/data/danger-example.csv --output LAB_04/out/report.txt --danger
```

Перед українським консольним виводом на Windows:

```powershell
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
```

Параметри: --input ШЛЯХ, --output ШЛЯХ, необов’язковий --danger без значення;
--help та --version — окремі режими. --danger показує індекси в консолі,
файловий звіт лишається стандартним. Папки вихідного файла створюються автоматично.
Локальна версія: `lab04 1.3.0-SNAPSHOT (build local)`, у CI замість local — номер збірки.

Без параметрів програма читає data/input.csv та пише out/report.txt відносно
поточного каталогу. Якщо вже перейшли всередину LAB_04:

```sh
../mvnw -B verify
java -jar target/lab04.jar --input data/input.csv --output out/report.txt
```

## Контрольні дані

- [data/input.csv](data/input.csv) — незмінний попередній контроль: 3 коректні й 2 хибні записи.
- [data/danger-example.csv](data/danger-example.csv) — обидва підтипи з індексами 7.50/40.00.
- [data/queries-example.csv](data/queries-example.csv) — 7 коректних записів для майбутніх запитів.
- [baseline/console.txt](src/test/resources/baseline/console.txt) та [baseline/report.txt](src/test/resources/baseline/report.txt)
  — фактичний вивід LAB_03 до змін на input.csv, записаний під час підготовки на Linux.
  При порівнянні на Windows враховуйте системні закінчення рядків.

Формат CSV: date;temperature;humidity;pressure;wind, UTF-8, без заголовка.
Дата зберігається як LocalDate з календарною перевіркою. Конструктори
та Builder приймають String YYYY-MM-DD як вхідний текст; базовий конструктор
перетворює його на LocalDate. GetDate повертає LocalDate. Неіснуючі
дати відхиляються з повідомленням «Дата задана неправильно».
Температура скінченна; вологість 0–100 %, тиск >0 гПа, вітер >=0 м/с;
NaN/Infinity відхиляються. Підтипи лишаються DailyReading (вітер <15)
та StormReading (>=15), формули dangerIndex — wind та wind*2.

## Запити та очікувані результати

Усі п’ять запитів реалізовані у WeatherQueries: filterByWind, dates,
averageTemperatureByMonth, temperatureStatistics та topByTemperature.
Пошук findByDate повертає Optional: за повтореної дати — перше входження,
за відсутньої дати, порожнього списку або null-дати — порожній результат.
Поріг фільтра пропонується зафіксувати як 15 м/с; умова строго >15.
Для місяця запропоновано YearMonth, щоб не змішувати різні роки.

| Запит | Планований результат на queries-example.csv |
| --- | --- |
| filter: wind >15 | Рядки 3, 5, 7, у початковому порядку |
| map: дати | Усі 7 дат у початковому порядку, повторення збережені |
| groupingBy: середня температура | 2026-09 → 18.00 °C; 2026-10 → 6.00 °C |
| summarizingDouble: температура | count=7, sum=90.00, min=-5.00, max=25.00, avg≈12.86 |
| top-5: температура ↓, дата ↑ | Рядки 2, 3, 5, 7, 1 |
| Optional: 2026-09-02 | Перший запис цієї дати: температура 25.00, вітер 15.00 |
| Optional: 2026-12-31 | Optional.empty() |

Top-N: N<0 — IllegalArgumentException, N=0 — порожній список, N>розміру — всі
елементи у визначеному порядку. На межі wind=15 запис не проходить новий
фільтр, хоча вже належить StormReading. Це дві різні умови.

Зірочка реалізована методом averageTemperatureByMonthAndKind:
місяць → WeatherKind → середня температура через вкладене groupingBy.
На queries-example.csv: 2026-09 → DAILY=10.00, STORM≈20.67;
2026-10 → DAILY=0.00, STORM=18.00. Відсутні категорії не додаються
як вигадані нульові значення; порожній вхід дає порожню карту.
Демонстрація нових запитів через CLI ще не підключена; методи перевіряються тестами.

## Javadoc

Linux/macOS:

```sh
javadoc -Werror -encoding UTF-8 -charset UTF-8 -docencoding UTF-8 -d LAB_04/target/apidocs LAB_04/src/main/java/ua/lpnu/kzp/lab04/*.java
```

Windows PowerShell:

```powershell
$sources = @(Get-ChildItem LAB_04/src/main/java -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
javadoc -Werror -encoding UTF-8 -charset UTF-8 -docencoding UTF-8 -d LAB_04/target/apidocs @sources
```

Відкрити LAB_04/target/apidocs/index.html. Згенеровані target/out не комітяться.

## Перевірки й подальша робота

Збережено 58 тестів LAB_03, очікування getDate адаптовані до LocalDate.
Додано 2 календарні та 15 тестів Stream-запитів: LAB_04 — 75 тестів.
Перевіряються поріг, порядок, дублікати, місяці різних років, порожній
вхід, статистика, top-N, другий критерій сортування, межі N,
пошук першого збігу/відсутньої дати, обидва рівні групування,
відсутні категорії та незмінність списку. Локальний verify
і Javadoc -Werror успішні. Після заміни циклів реальні контрольні console/file
побайтово збігаються зі збереженим baseline LAB_03.
Повний локальний clean verify підготовки успішний: 161 тест у чотирьох
модулях, помилок/пропусків немає, SpotBugs без зауважень. Реальні JAR,
контрольні звіти, --danger і Javadoc LAB_04 також перевірені.
Перед початком перенесення успішно виконано verify попередніх трьох модулів:
12 + 33 + 58 = 103 тести, SpotBugs без зауважень.
Підсумок перевірок підготовчого модуля фіксується в [Issue #54](https://github.com/Layer2686/CPPT_Lazor_SV_KI-304_2/issues/54)
після локальної збірки та фактичного CI. CI налаштований на чотири модулі,
три ОС, Javadoc, порівняння звітів і 12 JAR-артефактів.

[План та Issues](PLAN.md) · [Чернетка майбутнього звіту](REPORT.md).
