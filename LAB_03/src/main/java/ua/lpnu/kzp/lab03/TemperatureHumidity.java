package ua.lpnu.kzp.lab03;

/**
 * Незмінна пара температури та вологості одного вимірювання.
 *
 * @param temperature скінченна температура у °C; може бути від’ємною
 * @param humidity скінченна вологість від 0 до 100 %
 */
public record TemperatureHumidity(double temperature, double humidity)
{
    /**
     * Перевіряє значення до створення пари.
     *
     * @throws IllegalArgumentException якщо температура або вологість некоректна
     */
    public TemperatureHumidity
    {
        validateHumidity(humidity);
        validateTemperature(temperature);
    }

    // Спільні правила викликаються і конструктором, і CSV-фабрикою.
    static double validateHumidity(double humidity)
    {
        if (!Double.isFinite(humidity) || humidity < 0 || humidity > 100)
        {
            throw new IllegalArgumentException("вологість має бути від 0 до 100");
        }
        return humidity;
    }

    static double validateTemperature(double temperature)
    {
        if (!Double.isFinite(temperature))
        {
            throw new IllegalArgumentException("температура має бути скінченним числом");
        }
        return temperature;
    }
}
