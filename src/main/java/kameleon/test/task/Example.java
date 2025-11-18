package kameleon.test.task;

import kameleon.test.task.factory.WeatherSDKFactory;
import kameleon.test.task.modes.Mode;
import kameleon.test.task.services.WeatherSDK;

public class Example {
    public static void main(String[] args) {
        String API_KEY = "INSERT_API_KEY";
        WeatherSDKFactory factory = WeatherSDKFactory.getInstance();
        try {
            WeatherSDK weather = factory.getNewWeatherSDK(API_KEY, Mode.POLLING);
            System.out.println(weather.getCurrentWeatherInCity("Москва"));
            System.out.println(weather.getCurrentWeatherInCity("Долгопрудный"));
            System.out.println(weather.getCurrentWeatherInCity("Париж"));
            System.out.println(weather.getCurrentWeatherInCity("Лондон"));
            System.out.println(weather.getCurrentWeatherInCity("Берлин"));
            System.out.println(weather.getCurrentWeatherInCity("Кишинев"));
            System.out.println(weather.getCurrentWeatherInCity("Киев"));
            System.out.println(weather.getCurrentWeatherInCity("Минск"));
            System.out.println(weather.getCurrentWeatherInCity("Сидней"));
            System.out.println(weather.getCurrentWeatherInCity("Бразилиа"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
