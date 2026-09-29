# Ролі ШІ для лабораторної № 1

Опис ролей відповідає розділу «Використання агентів ШІ»
[методички](../md/lab_01_1.md). Кожен файл містить роль, вміння,
обмеження й промпт. Профілі Copilot читають ці файли як основні інструкції.

| Роль | Інструкції | Профіль Copilot | Інструменти |
| --- | --- | --- | --- |
| Менеджер | [manager.md](manager.md) | [manager.agent.md](../.github/agents/manager.agent.md) | читання, пошук |
| DevOps | [devops.md](devops.md) | [devops.agent.md](../.github/agents/devops.agent.md) | читання, пошук, редагування, команди |
| Розробник | [developer.md](developer.md) | [developer.agent.md](../.github/agents/developer.agent.md) | читання, пошук |
| Валідатор | [validator.md](validator.md) | [validator.agent.md](../.github/agents/validator.agent.md) | читання, пошук |
| Документатор | [documenter.md](documenter.md) | [documenter.agent.md](../.github/agents/documenter.agent.md) | читання, пошук, редагування |
| Рецензент | [reviewer.md](reviewer.md) | [reviewer.agent.md](../.github/agents/reviewer.agent.md) | читання, пошук |

Загальні правила — у
[copilot-instructions.md](../.github/copilot-instructions.md).
DevOps змінює доручену інфраструктуру; Документатор — доручену документацію.
Предметний Java-код студент пише самостійно.

## Як використати

У GitHub Copilot вибери потрібний профіль `Lab01 …` для завдання.
У VS Code відкрий корінь репозиторію та вибери його в списку агентів Copilot Chat.
Профілі вибираються вручну; автоматичне залучення вимкнено.
Для іншого ШІ передай текст вибраного файла з `ai/` разом із поточним Issue
та потрібними фрагментами власного коду.

Приклади запитів:

- **Менеджер:** «Перевір критерії поточного Issue та назви залежності».
- **DevOps:** «За Issue налаштуй Maven Wrapper; поясни файли й команди перевірки».
- **Розробник:** «Ось моя перевірка рядка. Поясни помилки без готової реалізації».
- **Валідатор:** «Ось вхід і результат. Звір чотири показники та запропонуй крайові випадки».
- **Документатор:** «Онови README за цими підтвердженими командами й результатами».
- **Рецензент:** «Перевір diff PR за його Issues та постав запитання до коду».

Після консультації доповни [журнал](USAGE.md): інструмент, роль,
запит, рекомендацію, перевірку та прийняте рішення. Записи стануть
основою розділу академічної доброчесності в `REPORT.md`.

## Формат профілів

Методичка згадує `.github/chatmodes`. Поточний формат профілів —
`.github/agents/*.agent.md`, YAML-заголовок і Markdown-промпт.
Визначення `name`, `description`, `tools` і ручного вибору відповідають
[документації GitHub](https://docs.github.com/en/copilot/reference/custom-agents-configuration).
Міграція зі старих chat modes описана в
[документації VS Code](https://code.visualstudio.com/docs/agent-customization/custom-agents).

Під час створення перевірено структуру й посилання файлів.
Фактичний запуск профілів у Copilot ще не перевірено.
