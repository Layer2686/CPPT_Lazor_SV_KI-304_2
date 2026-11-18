package lab05;
import java.time.LocalDate;
import java.util.Objects;
import java.util.List;
import java.time.DateTimeException;

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

    /**
     * Повертає CSV-заголовок у порядку Java-імен анотованих полів.
     * @return незмінний список п’яти назв
     */
    public static List<String> csvHeader()
    {
        return List.of("дата", "вологість", "тиск", "температура", "вітер");
    }

    /**
     * Відновлює запис із клітинок нового CSV у порядку date/humidity/pressure/temperature/wind.
     * @param fields п’ять клітинок
     * @return перевірений запис
     * @throws DataStorageException якщо кількість, значення або інваріанти порушені
     */
    public static WeatherReadingRecord fromFields(List<String> fields) throws DataStorageException
    {
        if (fields == null || fields.size() != 5 || fields.stream().anyMatch(Objects::isNull))
        {
            throw new DataStorageException("Очікується 5 непорожніх посилань на CSV-поля");
        }
        LocalDate parsedDate;
        try
        {
            parsedDate = LocalDate.parse(fields.get(0));
        }
        catch (DateTimeException exception)
        {
            throw new DataStorageException("Некоректне поле дата: " + fields.get(0), exception);
        }
        double parsedHumidity = parseNumber(fields.get(1), "вологість");
        double parsedPressure = parseNumber(fields.get(2), "тиск");
        double parsedTemperature = parseNumber(fields.get(3), "температура");
        double parsedWind = parseNumber(fields.get(4), "вітер");
        try
        {
            return new WeatherReadingRecord(parsedDate, parsedTemperature, parsedHumidity, parsedPressure, parsedWind);
        }
        catch (IllegalArgumentException exception)
        {
            throw new DataStorageException("Некоректне вимірювання: " + exception.getMessage(), exception);
        }
    }

    /**
     * Відновлює один CSV-запис без заголовка, включно з quoted-полями.
     * @param csv текст одного логічного запису
     * @return відновлене вимірювання
     * @throws DataStorageException якщо синтаксис або значення неправильні
     */
    public static WeatherReadingRecord fromCsv(String csv) throws DataStorageException
    {
        List<List<String>> rows = CsvParser.parseDocument(csv);
        if (rows.size() != 1)
        {
            throw new DataStorageException("Очікується один CSV-запис без заголовка");
        }
        return fromFields(rows.get(0));
    }

    /**
     * Повертає вимірювання попередньої поліморфної моделі через її фабрику.
     * @return звичайний або штормовий підтип за швидкістю вітру
     */
    public WeatherReading toReading()
    {
        return WeatherReading.builder().date(date.toString()).temperature(temperature)
                .humidity(humidity).pressure(pressure).wind(wind).build();
    }

    private static double parseNumber(String value, String name) throws DataStorageException
    {
        try
        {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException exception)
        {
            throw new DataStorageException("Некоректне число у полі " + name + ": " + value, exception);
        }
    }

}
