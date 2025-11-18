package kameleon.test.task.services;

import kameleon.test.task.model.CityCoordinates;
import kameleon.test.task.model.CurrentWeather;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class PollingService {
    private static final Logger logger = Logger.getLogger(PollingService.class.getName());

    private final AtomicBoolean isRunning;
    private final long pollingIntervalMinutes;
    private final ScheduledExecutorService scheduler;
    private final CacheService cacheService;
    private final OpenWeatherMapRequestsService openWeatherMapRequestsService;

    public PollingService(CacheService cacheService,
                          long pollingIntervalMinutes,
                          OpenWeatherMapRequestsService openWeatherMapRequestsService) {
        this.openWeatherMapRequestsService = openWeatherMapRequestsService;
        this.cacheService = cacheService;
        this.pollingIntervalMinutes = pollingIntervalMinutes;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.isRunning = new AtomicBoolean(false);
    }

    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            scheduler.scheduleAtFixedRate(this::updateAllCachedCities,
                    0, pollingIntervalMinutes, TimeUnit.MINUTES);
        }
    }

    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void updateAllCachedCities() {
        try {
            Map<String, CityCoordinates> cities = cacheService.getCachedCityCoordinates();

            for (String city : cities.keySet()) {
                try {
                    updateCityWeather(cities.get(city));
                    // Небольшая пауза между запросами чтобы не нагружать API
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.warning("Failed to update weather for city: " + city + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.warning(("Critical error in polling service: " + e.getMessage()));
        }
    }

    private void updateCityWeather(CityCoordinates city) {
        try {
            CurrentWeather currentWeather = openWeatherMapRequestsService.getCurrentWeather(city);
            cacheService.cache(currentWeather, city.getCityName());
        } catch (Exception e) {
            logger.warning(("Failed to fetch weather for " + city.getCityName() + ": " + e.getMessage()));
        }
    }
}