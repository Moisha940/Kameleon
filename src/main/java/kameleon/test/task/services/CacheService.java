package kameleon.test.task.services;

import kameleon.test.task.model.CityCoordinates;
import kameleon.test.task.model.CurrentWeather;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheService {
    private static final int MAX_SIZE = 10;
    private final Map<String, CurrentWeather> cachedCurrentWeather;
    private final Map<String, CityCoordinates> cachedCityCoordinates;

    public CacheService() {
        this.cachedCurrentWeather = new ConcurrentHashMap<>();
        this.cachedCityCoordinates = new ConcurrentHashMap<>();
    }

    // конечно, можно реализовать более элегантоно, но поскольку кэш максимум 10 элементов, то кмк O(n) здесь приемлимо
    private String findOldestEntry() {
        return cachedCurrentWeather.entrySet().stream()
                .min(Comparator.comparing(entry -> entry.getValue().getTimestamp()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public synchronized void cache(CurrentWeather currentWeather, CityCoordinates cityCoordinates) {
        String city = currentWeather.getCityName();

        if (!cachedCurrentWeather.containsKey(city) && cachedCurrentWeather.size() >= MAX_SIZE) {
            String oldestKey = findOldestEntry();
            if (oldestKey != null) {
                cachedCurrentWeather.remove(oldestKey);
                cachedCityCoordinates.remove(oldestKey);
            }
        }
        cachedCurrentWeather.put(city, currentWeather);
        cachedCityCoordinates.put(city, cityCoordinates);
    }

    public synchronized void cache(CurrentWeather currentWeather, String city) {
        cachedCurrentWeather.put(city, currentWeather);
    }

    public boolean containsKey(String city) {
        return cachedCurrentWeather.containsKey(city);
    }

    public CurrentWeather getCurrentWeather(String city) {
        return cachedCurrentWeather.get(city);
    }

    public CityCoordinates getCityCoordinates(String city) {
        return cachedCityCoordinates.get(city);
    }

    public Map<String, CityCoordinates> getCachedCityCoordinates() {
        return cachedCityCoordinates;
    }
}
