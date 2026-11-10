package ua.lpnu.kzp.lab05;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Незмінне плоске представлення вимірювання для зберігання.
 * Рівність враховує дату й усі чотири числові показники.
 */
public final class WeatherReadingRecord
{
    @CsvColumn("дата")
    private final LocalDate date;

    @CsvColumn("температура")
    private final double temperature;

    @CsvColumn("вологість")
    private final double humidity;

    @CsvColumn("тиск")
    private final double pressure;

    @CsvColumn("вітер")
    private final double wind;
    /**
     * Створює запис із перевіркою обмежень метеостанції.
     * @param date дата вимірювання
     * @param temperature скінченна температура у градусах Цельсія
     * @param humidity скінченна вологість від 0 до 100 відсотків
     * @param pressure скінченний додатний тиск у гПа
     * @param wind скінченна невід’ємна швидкість вітру у м/с
     * @throws IllegalArgumentException якщо дата відсутня або показник некоректний
     */
    public WeatherReadingRecord(LocalDate date, double  temperature, double humidity, double pressure, double wind)
    {
        if (date == null)
        {
            throw new IllegalArgumentException ("Дата не може бути null");
        }
        this.date = date;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        this.wind = wind;
        TemperatureHumidity.validateTemperature(temperature);
        TemperatureHumidity.validateHumidity(humidity);
        if (!Double.isFinite(wind) || wind < 0)
        {
            throw new IllegalArgumentException("Вітер має бути скінченним і невід’ємним");
        }
        if (!Double.isFinite(pressure) || pressure <= 0)
        {
            throw new IllegalArgumentException("Тиск має бути скінченним та більшим за нуль");
        }
    }
    /**
     * Повертає дату вимірювання.
     * @return дату вимірювання
     */
    public LocalDate getDate()
    {
        return date;
    }
    /**
     * Повертає температуру у градусах Цельсія.
     * @return температуру у градусах Цельсія
     */
    public double getTemperature()
    {
        return temperature;
    }
    /**
     * Повертає вологість у відсотках.
     * @return вологість у відсотках
     */
    public double getHumidity()
    {
        return humidity;
    }
    /**
     * Повертає тиск у гПа.
     * @return тиск у гПа
     */
    public double getPressure()
    {
        return pressure;
    }
    /**
     * Повертає швидкість вітру у м/с.
     * @return швидкість вітру у м/с
     */
    public double getWind()
    {
        return wind;
    }
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
        WeatherReadingRecord record = (WeatherReadingRecord) other;
        return date.equals(record.date) && Double.compare(temperature, record.temperature) == 0 && Double.compare(humidity, record.humidity) == 0
            && Double.compare(wind, record.wind) == 0 && Double.compare(pressure, record.pressure) == 0;
    }
    @Override
    public int hashCode()
    {
        return Objects.hash(getClass(), date, pressure, wind, temperature, humidity);
    }
    /**
     * Копіює значення вимірювання будь-якого погодного підтипу.
     * @param reading початкове вимірювання, не {@code null}
     * @return новий плоский запис із тими самими значеннями
     * @throws NullPointerException якщо вимірювання відсутнє
     */
    public static WeatherReadingRecord fromReading(WeatherReading reading)
    {
        return new WeatherReadingRecord(reading.getDate(), reading.getTemperature(), reading.getHumidity(), reading.getPressure(), reading.getWind());
    }
}
