package ua.lpnu.kzp.lab03;

import java.util.Locale;
import java.util.Objects;

/**
 * Одне незмінне й перевірене вимірювання метеостанції.
 * Температура та вологість зберігаються разом у {@link TemperatureHumidity}.
 */
public abstract sealed class WeatherReading permits DailyReading, StormReading
{
    private final String date;
    private final TemperatureHumidity temperatureHumidity;
    private final double pressure;
    private final double wind;

    /**
     * Створює вимірювання після перевірки всіх полів.
     *
     * @param date текстова дата YYYY-MM-DD; календарний розбір не виконується
     * @param temperature скінченна температура, °C
     * @param humidity вологість від 0 до 100, %
     * @param pressure додатний скінченний тиск, гПа
     * @param wind невід’ємний скінченний вітер, м/с
     * @throws IllegalArgumentException якщо хоча б одне поле некоректне
     */
    public WeatherReading(String date, double temperature, double humidity, double pressure, double wind)
    {
        this.temperatureHumidity = new TemperatureHumidity(temperature, humidity);
        this.wind = validateWind(wind);
        this.pressure = validatePressure(pressure);
        if (date == null || !date.trim().matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))
        {
            throw new IllegalArgumentException("Дата задана неправильно");
        }
        this.date = date.trim();
    }
    /** Повертає навчальний індекс небезпеки.
     * @return індекс, обчислений конкретним підтипом
     */
    public abstract double dangerIndex();

    /** Повертає категорію вимірювання.
     * @return категорія конкретного підтипу
     */
    public abstract WeatherKind getKind();

    private static WeatherReading createReading(String date, double temperature, double humidity, double pressure, double wind)
    {
        if (wind < 15)
        {
            return new DailyReading(date, temperature, humidity, pressure, wind);
        }
        else
        {
            return new StormReading(date, temperature, humidity, pressure, wind);
        }
    }
    /**
     * Розбирає один CSV-рядок date;temperature;humidity;pressure;wind.
     * Спільні правила перевіряються в порядку LAB_01, щоб зберегти першу причину
     * відхилення навіть для рядка з кількома помилками. Конструктор перевіряє
     * стан повторно, тому обхід фабрики не дозволяє створити хибний об’єкт.
     *
     * @param line рядок без заголовка; крайні пробіли полів прибираються
     * @return коректне вимірювання
     * @throws IllegalArgumentException якщо рядок null, структура або значення хибні
     */
    public static WeatherReading fromCsv(String line)
    {
        if (line == null)
        {
            throw new IllegalArgumentException("Рядок не може бути null");
        }
        String[] fields = line.split(";", -1);
        if (fields.length != 5)
        {
            throw new IllegalArgumentException("очікується 5 полів");
        }
        String currentField = "вологість";
        try
        {
            double humidity = TemperatureHumidity.validateHumidity(
                    Double.parseDouble(fields[2].trim()));
            currentField = "вітер";
            double wind = validateWind(Double.parseDouble(fields[4].trim()));
            currentField = "температура";
            double temperature = TemperatureHumidity.validateTemperature(
                    Double.parseDouble(fields[1].trim()));
            currentField = "тиск";
            double pressure = validatePressure(Double.parseDouble(fields[3].trim()));
            return createReading(fields[0].trim(), temperature, humidity, pressure, wind);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException(currentField + " не є числом", e);
        }
    }

    private static double validatePressure(double pressure)
    {
        if (!Double.isFinite(pressure) || pressure <= 0)
        {
            throw new IllegalArgumentException("тиск має бути додатним скінченним числом");
        }
        return pressure;
    }

    private static double validateWind(double wind)
    {
        if (!Double.isFinite(wind) || wind < 0)
        {
            throw new IllegalArgumentException("вітер має бути скінченним невід’ємним числом");
        }
        return wind;
    }

    /**
     * Повертає дата вимірювання у форматі YYYY-MM-DD.
     *
     * @return дата вимірювання у форматі YYYY-MM-DD
     */
    public String getDate()
    {
        return date;
    }

    /**
     * Повертає температура у °C.
     *
     * @return температура у °C
     */
    public double getTemperature()
    {
        return temperatureHumidity.temperature();
    }

    /**
     * Повертає відносна вологість у %.
     *
     * @return відносна вологість у %
     */
    public double getHumidity()
    {
        return temperatureHumidity.humidity();
    }

    /**
     * Повертає перевірена незмінна пара температури й вологості.
     *
     * @return перевірена незмінна пара температури й вологості
     */
    public TemperatureHumidity getTemperatureHumidity()
    {
        return temperatureHumidity;
    }

    /**
     * Повертає атмосферний тиск у гПа.
     *
     * @return атмосферний тиск у гПа
     */
    public double getPressure()
    {
        return pressure;
    }

    /**
     * Повертає швидкість вітру у м/с.
     *
     * @return швидкість вітру у м/с
     */
    public double getWind()
    {
        return wind;
    }

    /**
     * Повертає опис вимірювання з двома десятковими знаками та одиницями.
     *
     * @return опис вимірювання з двома десятковими знаками та одиницями
     */
    @Override
    public String toString()
    {
        return String.format(Locale.ROOT, "%s: %.2f °C, %.2f %%, %.2f гПа, %.2f м/с",
                date, getTemperature(), getHumidity(), pressure, wind);
    }

    /**
     * Порівнює вимірювання за конкретним класом і всіма полями стану.
     * @param other об’єкт для порівняння
     * @return true для вимірювання того самого підтипу з однаковими даними
     */
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null || getClass() != other.getClass())
        {
            return false;
        }
        WeatherReading reading = (WeatherReading)other;
        return date.equals(reading.date) && temperatureHumidity.equals(reading.temperatureHumidity) && Double.compare(wind, reading.wind) == 0 && Double.compare(pressure, reading.pressure) == 0;
    }
    /**
     * Обчислює хеш за тими самими даними, які використовує equals.
     * @return хеш-код класу, дати, тиску, вітру й пари температури та вологості
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(getClass(), date, pressure, wind, temperatureHumidity);
    }
    /**
     * Повертає новий будівник, у якому потрібно задати всі п’ять полів.
     *
     * @return новий будівник, у якому потрібно задати всі п’ять полів
     */
    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * Поетапно збирає параметри; {@link #build()} завжди використовує конструктор.
     * Сам будівник змінний і призначений для використання в одному потоці.
     */
    public static final class Builder
    {
        private String date;
        private double temperature = Double.NaN;
        private double humidity = Double.NaN;
        private double pressure = Double.NaN;
        private double wind = Double.NaN;

        private Builder()
        {
        }

        /**
         * Задає значення для майбутнього вимірювання.
         *
         * @param value дата YYYY-MM-DD
         * @return цей будівник
         */
        public Builder date(String value)
        {
            date = value;
            return this;
        }

        /**
         * Задає значення для майбутнього вимірювання.
         *
         * @param value температура у °C
         * @return цей будівник
         */
        public Builder temperature(double value)
        {
            temperature = value;
            return this;
        }

        /**
         * Задає значення для майбутнього вимірювання.
         *
         * @param value вологість у %
         * @return цей будівник
         */
        public Builder humidity(double value)
        {
            humidity = value;
            return this;
        }

        /**
         * Задає значення для майбутнього вимірювання.
         *
         * @param value тиск у гПа
         * @return цей будівник
         */
        public Builder pressure(double value)
        {
            pressure = value;
            return this;
        }

        /**
         * Задає значення для майбутнього вимірювання.
         *
         * @param value вітер у м/с
         * @return цей будівник
         */
        public Builder wind(double value)
        {
            wind = value;
            return this;
        }

        /**
         * Створює окремий незмінний об’єкт із поточних параметрів.
         *
         * @return перевірене вимірювання
         * @throws IllegalArgumentException якщо поле не задано або воно некоректне
         */
        public WeatherReading build()
        {
            return createReading(date, temperature, humidity, pressure, wind);
        }
    }
}
