package ua.lpnu.kzp.lab03;

/** Точка запуску: передає параметри парсеру та запускає контролер. */
public final class Main {
    private Main() {
    }

    /**
     * Запускає програму або повідомляє про помилку параметрів.
     *
     * @param args параметри командного рядка
     */
    public static void main(String[] args) {
        try {
            CommandLineOptions options = ArgumentParser.parse(args);
            WeatherController controller = new WeatherController();
            controller.run(options);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}
