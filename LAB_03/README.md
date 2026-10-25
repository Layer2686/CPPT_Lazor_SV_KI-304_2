# Лабораторна №3 — спадкування та поліморфізм

Варіант 18 — метеостанція. Окремий модуль LAB_03, Java 21, версія 1.2.0.
Реалізовано DailyReading/StormReading, WeatherKind, поліморфний індекс
небезпеки, equals/hashCode, HashSet та sealed/pattern switch.

Потрібні Git, JDK 21 та інтернет для першої збірки. Maven 3.9.9 завантажує
спільний Wrapper. Встановлення середовища — у [кореневому README](../README.md).
Усі команди нижче виконуються **з кореня репозиторію**.

Реліз: [v1.2.0](https://github.com/Layer2686/CPPT_Lazor_SV_KI-304_2/tree/v1.2.0) · [CI трьох ОС та JAR-артефакти](https://github.com/Layer2686/CPPT_Lazor_SV_KI-304_2/actions/runs/40239715250) ·
[PR #50](https://github.com/Layer2686/CPPT_Lazor_SV_KI-304_2/pull/50). Докладні результати — у [звіті](REPORT.md).

## Збірка

Linux/macOS:

```sh
./mvnw -B -pl LAB_03 -am verify
```

Windows PowerShell:

```powershell
.\mvnw.cmd -B -pl LAB_03 -am verify
```

Verify включає компіляцію, JUnit, виконуваний JAR та SpotBugs.
`-pl LAB_03` вибирає третю роботу, `-am` додає потрібні проєкти реактора.
Для всіх трьох модулів — `./mvnw -B verify` або `.\mvnw.cmd -B verify`.
`clean` за потреби додається перед verify та видаляє попередній target.

## Звичайний запуск, версія та довідка

```sh
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_03/target/lab03.jar --input LAB_03/data/input.csv --output LAB_03/out/report.txt
java -jar LAB_03/target/lab03.jar --version
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_03/target/lab03.jar --help
```

Команди java підходять і для PowerShell. Перед українським консольним виводом
на Windows виконайте:

```powershell
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
```

| Параметр | Призначення |
| --- | --- |
| --input ШЛЯХ | Вхідний CSV; типово data/input.csv |
| --output ШЛЯХ | Стандартний звіт; типово out/report.txt |
| --danger | Додатковий аналіз у консолі; без значення |
| --help | Довідка, окремий режим запуску |
| --version | Версія та номер збірки, окремий режим |

Шляхи відносні до поточного робочого каталогу. Тому запуск без параметрів
виконуйте з папки LAB_03. Папки звіту створюються автоматично;
`--output report.txt` без батьківського каталогу також працює.
Локально версія має вигляд `lab03 1.2.0 (build local)`, у CI — з номером запуску.

## Додатковий аналіз через --danger

Для демонстрації обох підтипів підготовлено [data/danger-example.csv](data/danger-example.csv):

```text
2026-09-01;10;70;1013;7.5
2026-09-02;10;70;1013;20
```

Запуск:

```sh
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_03/target/lab03.jar --input LAB_03/data/danger-example.csv --output LAB_03/out/report.txt --danger
```

Після стандартного виводу з’явиться:

```text
Додатковий аналіз небезпеки:
2026-09-01: Звичайне вимірювання, індекс 7.50
2026-09-02: Штормове вимірювання, індекс 40.00
Максимальний індекс небезпеки: 40.00
```

Прапорець дозволено перед, між або після пар --input/--output.
Повторний --danger просто залишає режим увімкненим; значення true/false
після нього не передається. `--input --danger` є помилкою відсутнього шляху.
Help і version залишаються окремими режимами й не поєднуються з --danger.

**У файлі залишається тільки стандартний звіт.** Без прапорця консоль
зберігає попередню поведінку. Якщо коректних записів немає, додатковий
режим повідомляє «Немає даних для аналізу небезпеки» без вигаданого максимуму.

## Дані, модель та сумісність

CSV — UTF-8, без заголовка, поля date;temperature;humidity;pressure;wind.
Дата — текст YYYY-MM-DD без календарної перевірки. Температура скінченна,
вологість 0–100 %, тиск додатний скінченний у гПа, вітер скінченний
невід’ємний у м/с. NaN, нескінченності та порожні поля відхиляються.

| Підтип | Вітер | WeatherKind | dangerIndex |
| --- | --- | --- | --- |
| DailyReading | 0 <= wind < 15 м/с | DAILY | wind |
| StormReading | wind >= 15 м/с | STORM | wind * 2 |

Це навчальні поріг і формули, не метеорологічний стандарт. Індекс не має
фізичної одиниці виміру; швидкість вітру в об’єкті не змінюється.
Спільний стан і перевірки — в abstract sealed WeatherReading; підтипи final.
Температура й вологість зберігаються композицією з record TemperatureHumidity.
CreateReading вибирає підтип для fromCsv та Builder.build.

Main лише запускає парсер і контролер. ArgumentParser просувається на один
аргумент для прапорця й на два для ключа зі шляхом. CommandLineOptions
передає прапорець контролеру. WeatherReport.formatDangerReport використовує
describeReading, dangerIndex та maxDangerIndex без дублювання формул.

Equals/hashCode враховують конкретний клас і date, temperatureHumidity,
pressure, wind. HashSet демонструє усунення логічних дублікатів; звичайна
статистика зберігає всі коректні записи зі списку, включно з однаковими.

[data/input.csv](data/input.csv) лишився контрольним файлом попередніх робіт:
3 коректні записи, мінімум −5.50 °C, вологість 70.00 %, вітер 7.50 м/с,
дві помилки. CI порівнює стандартні консолі трьох JAR між собою й окремо
їхні файлові звіти. --danger додатково перевіряється на змішаному прикладі.

## Тести та демонстрація зірочки

У LAB_03 — 58 тестів: MainTest (15), WeatherReadingTest (12),
ArgumentParserTest (10), WeatherFileServiceTest (3), WeatherSubtypeTest (5),
WeatherReportTest (7), WeatherEqualityTest (6). Попередні перевірки збережені,
виклики конструкторів адаптовані до абстрактної бази й п’ятого параметра record.
Нові тести охоплюють парсер прапорця, його вивід, незмінність файла,
порожній/хибний вхід та незалежність чисел від локалі.

DescribeReading має case DailyReading і case StormReading без default.
Окремий тимчасовий приклад без другої гілки перевірено javac --release 21:
компілятор відхиляє невичерпний switch. Обидві гілки компілюються без preview.
Непрацездатний приклад не зберігається в основних джерелах.

Окремий тест:

```sh
./mvnw -B -pl LAB_03 -Dtest=WeatherReportTest#describesBothPermittedSubtypes test
```

Для ручного виклику методів також можна запустити
`jshell --class-path LAB_03/target/classes` і виконати:

```java
import ua.lpnu.kzp.lab03.*;
WeatherReading reading = WeatherReading.fromCsv("2026-09-01;10;70;1013;20");
System.out.println(WeatherReport.describeReading(reading));
System.out.println(reading.dangerIndex());
```

Для виходу — `/exit`. Основна демонстрація нового аналізу доступна через --danger.

## Javadoc

Linux/macOS:

```sh
javadoc -Werror -encoding UTF-8 -charset UTF-8 -docencoding UTF-8 -d LAB_03/target/apidocs LAB_03/src/main/java/ua/lpnu/kzp/lab03/*.java
```

Windows PowerShell:

```powershell
$sources = @(Get-ChildItem LAB_03/src/main/java -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
javadoc -Werror -encoding UTF-8 -charset UTF-8 -docencoding UTF-8 -d LAB_03/target/apidocs @sources
```

Відкрити LAB_03/target/apidocs/index.html. Згенерована довідка не комітиться.

[Звіт](REPORT.md) · [План та Issues](PLAN.md).
