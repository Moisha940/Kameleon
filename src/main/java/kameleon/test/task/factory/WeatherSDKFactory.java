package kameleon.test.task.factory;

import kameleon.test.task.modes.Mode;
import kameleon.test.task.services.WeatherSDK;

import java.util.concurrent.ConcurrentHashMap;

public final class WeatherSDKFactory {
    private static final WeatherSDKFactory INSTANCE = new WeatherSDKFactory();
    private final ConcurrentHashMap<String, WeatherSDK> instances = new ConcurrentHashMap<>();

    private WeatherSDKFactory() {
    }

    public static WeatherSDKFactory getInstance() {
        return INSTANCE;
    }

    public WeatherSDK getNewWeatherSDK(String apiKey, Mode mode) {
        return instances.computeIfAbsent(apiKey, key -> {
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalArgumentException("apiKey cannot be null or empty");
            }
            if (instances.containsKey(apiKey)) {
                throw new IllegalArgumentException("apiKey " + apiKey + " already exists");
            }
            return new WeatherSDK(apiKey, mode);
        });
    }

    public WeatherSDK getWeatherSDK(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("apiKey cannot be null or empty");
        }
        return instances.get(apiKey);
    }

    public void deleteWeatherSDK(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("apiKey cannot be null or empty");
        }
        if (!instances.containsKey(apiKey)) {
            throw new IllegalArgumentException("apiKey " + apiKey + " doesn't  exist");
        }
        instances.get(apiKey).shutDown();
        instances.remove(apiKey);
    }
}
