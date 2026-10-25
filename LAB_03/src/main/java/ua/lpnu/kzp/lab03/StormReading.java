package ua.lpnu.kzp.lab03;

import java.io.IOException;

/** Штормове вимірювання зі швидкістю вітру від 15 м/с включно. */
public final class StormReading extends WeatherReading
{
    /**
     * Створює вимірювання з перевіркою спільного стану та діапазону підтипу.
     * @param date дата у форматі YYYY-MM-DD
     * @param temperature скінченна температура, °C
     * @param humidity вологість від 0 до 100, %
     * @param pressure додатний скінченний тиск, гПа
     * @param wind скінченна швидкість вітру, м/с
     * @throws IllegalArgumentException якщо поле або діапазон підтипу некоректний
     */
    public StormReading(String date, double temperature, double humidity, double pressure, double wind)
    {
        super(date, temperature, humidity, pressure, wind);
        if (wind < 15)
        {
            throw new IllegalArgumentException("Для штормового вимірювання швидкість вітру має бути більшою або рівною 15 м/с за 15 м/с");
        }
    }
    /**
     * Повертає навчальний індекс небезпеки.
     * @return індекс без фізичної одиниці виміру
     */
    @Override
    public double dangerIndex()
    {
        return getWind() * 2;
    }
    /**
     * Повертає категорію вимірювання.
     * @return категорія цього підтипу
     */
    @Override
    public WeatherKind getKind()
    {
        return WeatherKind.STORM;
    }
}
