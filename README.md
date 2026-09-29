# LAB_01 — структура Maven-проєкту

Кореневий `pom.xml` є Maven-агрегатором і містить модуль `LAB_01`.
Предметний код лабораторної зберігається в `LAB_01/src/main`, тести — у
`LAB_01/src/test`, дані — у `LAB_01/data`, ресурси — у
`LAB_01/src/main/resources`.

## Збірка

Потрібен JDK 21.

Linux/macOS:

```sh
./mvnw -B clean package
```

Windows PowerShell:

```powershell
.\mvnw.cmd -B clean package
```

Java та UTF-8 зафіксовані в POM через `maven.compiler.release` і властивості
кодування. Налаштування IDE, `target/`, `out/` та скомпільовані класи
ігноруються Git.

Maven Wrapper використовує Maven 3.9.9. Після отримання змін у робочій копії
перевірте право виконання `mvnw` на Unix (`chmod +x mvnw`) та наявність
`.mvn/wrapper/maven-wrapper.jar`, який має бути доданий генератором офіційного
Wrapper перед першою збіркою.
