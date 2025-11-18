package kameleon.test.task.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import kameleon.test.task.exceptions.APIKeyException;
import kameleon.test.task.exceptions.CoordinateException;
import kameleon.test.task.model.CityCoordinates;
import kameleon.test.task.model.CurrentWeather;
import kameleon.test.task.modes.Mode;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public final class WeatherSDK {
    private static final int UPDATE_INTERVAL_MINUTES = 10;

    private final Mode mode;
    private final String API_KEY;
    private final ObjectMapper mapper;
    private final CacheService cacheService;
    private final PollingService pollingService;
    private final OpenWeatherMapRequestsService openWeatherMapRequestsService;

    public WeatherSDK(String apiKey, Mode mode) {
        this.mode = mode;
        this.API_KEY = apiKey;
        this.mapper = new ObjectMapper();
        this.cacheService = new CacheService();
        this.openWeatherMapRequestsService = new OpenWeatherMapRequestsService(apiKey);
        if (mode.equals(Mode.POLLING)) {
            this.pollingService = new PollingService(cacheService, UPDATE_INTERVAL_MINUTES, openWeatherMapRequestsService);
            pollingService.start();
        } else {
            this.pollingService = null;
        }
    }

    private CurrentWeather makeRequest(String city) throws IOException, InterruptedException, CoordinateException, APIKeyException {
        CityCoordinates cityCoordinates = cacheService.getCityCoordinates(city);
        if (cityCoordinates == null) {
            cityCoordinates = openWeatherMapRequestsService.getCityCoordinates(city);
        }

        CurrentWeather currentWeather = openWeatherMapRequestsService.getCurrentWeather(cityCoordinates);
        cacheService.cache(currentWeather, cityCoordinates);

        return currentWeather;
    }

    public String getCurrentWeatherInCity(String city) throws APIKeyException, IOException, InterruptedException, CoordinateException {
        if (cacheService.containsKey(city)) {
            CurrentWeather cached = cacheService.getCurrentWeather(city);
            if (Duration.between(cached.getTimestamp(), LocalDateTime.now()).toMinutes() < UPDATE_INTERVAL_MINUTES) {
                return mapper.writeValueAsString(cached);
            }
        }

        CurrentWeather currentWeather = makeRequest(city);
        return mapper.writeValueAsString(currentWeather);
    }

    public void shutDown() {
        if (pollingService != null) {
            pollingService.stop();
        }
        openWeatherMapRequestsService.shutDown();
    }

    public Mode getMode() {
        return mode;
    }

    public String getAPI_KEY() {
        return API_KEY;
    }
}
