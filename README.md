# Кросплатформенні засоби програмування — Метеостанція

Варіант 18 · Лазор С. В. · КІ-304, підгрупа 2.

| Робота | Реалізація | Версія | Документація |
| --- | --- | --- | --- |
| [LAB_01](LAB_01) | Обробка CSV через рядки та змінні | 1.0.0 | [Звіт №1](REPORT.md) |
| [LAB_02](LAB_02) | Класи, record, контролер, парсер параметрів і builder | 1.1.1 | [Запуск №2](LAB_02/README.md), [звіт №2](LAB_02/REPORT.md) |
| [LAB_03](LAB_03) | Підтипи, enum, рівність та консольний аналіз --danger | 1.2.0 | [Запуск №3](LAB_03/README.md), [звіт №3](LAB_03/REPORT.md), [план №3](LAB_03/PLAN.md) |
| [LAB_04](LAB_04) | Підготовча копія №3; Stream API ще в роботі | 1.3.0-SNAPSHOT | [Запуск №4](LAB_04/README.md), [план №4](LAB_04/PLAN.md) |

`./mvnw -B verify` (Windows: `.\mvnw.cmd -B verify`) перевіряє всі чотири модулі в гілці lab04.
Окрема перевірка четвертої: `./mvnw -B -pl LAB_04 -am verify`.
Окрема перевірка третьої роботи: `./mvnw -B -pl LAB_03 -am verify`.
Команди нижче демонструють LAB_01; окремі команди LAB_02–LAB_04 — у таблиці.
Для аналізу індексів у LAB_03 використовуйте прапорець `--danger`.
Фінальна версія №3: [v1.2.0](https://github.com/Layer2686/CPPT_Lazor_SV_KI-304_2/tree/v1.2.0), [CI трьох ОС та артефакти](https://github.com/Layer2686/CPPT_Lazor_SV_KI-304_2/actions/runs/40239715250).
Фінальна версія №2: [v1.1.1](https://github.com/Layer2686/CPPT_Lazor_SV_KI-304_2/tree/v1.1.1), [CI трьох ОС](https://github.com/Layer2686/CPPT_Lazor_SV_KI-304_2/actions/runs/39641384246).

Консольна програма на Java читає вимірювання метеостанції з CSV-файла,
пропускає некоректні записи з поясненням і обчислює показники за коректними
записами: їхню кількість, мінімальну температуру, середню вологість і
максимальну швидкість вітру. Один звіт із показниками та помилками
виводиться в консоль і записується у файл UTF-8.

## Встановлення та отримання проєкту

Потрібні Git, JDK 21 та інтернет для першої збірки. Maven встановить Wrapper.
Якщо `JAVA_HOME` вже задана, вона має вказувати на JDK 21.

**Ubuntu 24.04+** ([пакет JDK](https://packages.ubuntu.com/noble/openjdk-21-jdk)):

```sh
sudo apt update
sudo apt install git openjdk-21-jdk
```

**Arch / EndeavourOS** ([пакет JDK](https://archlinux.org/packages/extra/x86_64/jdk21-openjdk/)):

```sh
sudo pacman -Syu --needed git jdk21-openjdk
sudo archlinux-java set java-21-openjdk
```

**macOS** із встановленим [Homebrew](https://brew.sh/)
([Temurin 21](https://formulae.brew.sh/cask/temurin%4021)):

```sh
brew install git
brew install --cask temurin@21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

**Windows PowerShell** із WinGet
([Git](https://git-scm.com/install/windows), [JDK](https://learn.microsoft.com/en-us/windows/dev-environment/java)):

```powershell
winget install --id Git.Git --exact
winget install --id EclipseAdoptium.Temurin.21.JDK --exact
```

Після встановлення на Windows відкрийте новий термінал. Без пакетного менеджера
встановіть [Git](https://git-scm.com/install/) та
[Temurin JDK 21](https://adoptium.net/temurin/releases/?version=21) через інсталятори.
Перевірте, що `java` та `javac` показують версію 21:

```sh
git --version
java -version
javac -version
git clone https://github.com/Layer2686/CPPT_Lazor_SV_KI-304_2.git
cd CPPT_Lazor_SV_KI-304_2
```

## Збірка

Усі наступні команди виконуються **з кореня репозиторію**.
Linux / macOS:

```sh
./mvnw -B clean verify
```

Windows PowerShell:

```powershell
.\mvnw.cmd -B clean verify
```

Команда запускає тести й SpotBugs та створює виконуваний
`LAB_01/target/lab01.jar`. Глобально встановлювати Maven не потрібно.
Якщо на Linux/macOS виникло `Permission denied`, виконайте `chmod +x mvnw`.

JUnit перевіряє валідацію CSV, чотири показники, CLI, UTF-8, помилки файлів
і незалежність від локалі. Тестові файли створюються в тимчасових папках.

HTML-документація коду (команда однакова для трьох ОС):

```sh
javadoc -private -encoding UTF-8 -charset UTF-8 -docencoding UTF-8 -d LAB_01/target/apidocs LAB_01/src/main/java/Main.java
```

Відкрийте `LAB_01/target/apidocs/index.html` у браузері. `clean` видаляє
згенеровані файли, тому документацію створюють після збірки.

## Запуск

Із кореня репозиторію:

```sh
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_01/target/lab01.jar --input LAB_01/data/input.csv --output out/report.txt
```

Без параметрів запуск із директорії `LAB_01` читає `data/input.csv`
і записує `out/report.txt`. Шляхи відносні до робочої директорії;
папки для звіту створюються автоматично. Працює також `--output report.txt`.

Для українського виводу в Windows PowerShell використовуйте UTF-8:

```powershell
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_01/target/lab01.jar --input LAB_01/data/input.csv --output out/report.txt
```

Довідка й версія (команди однакові для трьох ОС):

```sh
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_01/target/lab01.jar --help
java "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar LAB_01/target/lab01.jar --version
```

Версія програми — `1.0.0`; локальна збірка показує `build local`,
збірка GitHub Actions — номер запуску CI. Звіт після запуску лежить у `out/report.txt`.
Фінальна версія: [тег v1.0.0](https://github.com/Layer2686/CPPT_Lazor_SV_KI-304_2/tree/v1.0.0),
[CI тега на трьох ОС](https://github.com/Layer2686/CPPT_Lazor_SV_KI-304_2/actions/runs/38596059115).

У демонстраційному файлі є три коректні записи й дві помилки. Програма
показує коректні рядки, а підсумковий звіт у консолі та файлі містить
чотири показники й причини пропуску рядків 4 і 5. Без коректних записів
звіт містить «Немає даних» і кількість 0. Помилки читання та запису
повідомляються окремо із зазначенням шляху.

## Формат вхідного файла

[Приклад CSV](LAB_01/data/input.csv) має кодування UTF-8, не містить заголовка
чи коментарів. Один рядок — один запис; поля розділено крапкою з комою:

```text
date;temperature;humidity;pressure;wind
2026-09-01;-5.5;70.0;1013.0;3.2
```

Перший рядок блока описує порядок полів і **не входить** до CSV. У кожному
записі має бути рівно п'ять полів. Крайні пробіли прибираються; десятковий
роздільник чисел — крапка.

| Поле | Одиниця | Допустиме значення |
| --- | --- | --- |
| `date` | — | Текст `YYYY-MM-DD`; календарну дату не розбираємо |
| `temperature` | °C | Скінченне число, може бути від'ємним |
| `humidity` | % | Скінченне число від 0 до 100 включно |
| `pressure` | гПа | Скінченне число більше 0 |
| `wind` | м/с | Скінченне число від 0 і вище |

`NaN`, нескінченність і порожні числові поля вважаються помилками. Некоректний
рядок не впливає на підрахунок; повідомлення містить його номер і причину.
Для прикладу очікувані показники: мінімальна температура −5.50 °C,
середня вологість 70.00 % і максимальна швидкість вітру 7.50 м/с.

## Матеріали

- [План лабораторної роботи](PLAN.md) — етапи й GitHub Issues.
- [Звіт лабораторної роботи](REPORT.md).
- [GitHub Actions](https://github.com/Layer2686/CPPT_Lazor_SV_KI-304_2/actions) — перевірки на Ubuntu, Windows і macOS та JAR-артефакти.
