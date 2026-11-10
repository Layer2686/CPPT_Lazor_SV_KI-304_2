package ua.lpnu.kzp.lab05;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
/** Назва CSV-колонки для поля, доступна під час виконання через рефлексію. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvColumn
{
    /**
     * Повертає назву колонки в заголовку CSV.
     * @return назва колонки
     */
    String value();
}
