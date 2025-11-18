package kameleon.test.task.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kameleon.test.task.exceptions.APIKeyException;
import kameleon.test.task.exceptions.CoordinateException;
import kameleon.test.task.model.CityCoordinates;
import kameleon.test.task.model.CurrentWeather;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class OpenWeatherMapRequestsService {
    private final String URL_FOR_WEATHER;
    private final String URL_FOR_COORDINATES;
    private final ObjectMapper mapper;
    private final HttpClient httpClient;

    public OpenWeatherMapRequestsService(String apiKey) {
        this.mapper = new ObjectMapper();
        this.URL_FOR_WEATHER = String.format("https://api.openweathermap.org/data/3.0/onecall" +
                "?exclude=minutely,hourly,daily" +
                "&appid=%s", apiKey);
        this.URL_FOR_COORDINATES = String.format("http://api.openweathermap.org/geo/1.0/direct?limit=1&appid=%s", apiKey);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private JsonNode checkResponse(String response, String city) throws JsonProcessingException, CoordinateException, APIKeyException {
        JsonNode jsonNode = mapper.readTree(response);
        if (jsonNode.has("cod")) {
            if (jsonNode.get("cod").asText().equals("401")) {
                throw new APIKeyException(jsonNode.path("message").asText());
            } else {
                throw new CoordinateException(jsonNode.path("message").asText());
            }
        }

        if (jsonNode.get(0) == null) {
            throw new CoordinateException(String.format("The city %s was not found", city));
        }

        return jsonNode.get(0);
    }

    private CurrentWeather getCurrentWeatherFromJSON(String json, String city) throws JsonProcessingException {
        JsonNode root = mapper.readTree(json);
        JsonNode current = root.path("current");
        JsonNode weather = current.path("weather").get(0);

        return CurrentWeather.builder()
                .weather(weather.path("main").asText(), weather.path("description").asText())
                .visibility(current.path("visibility").asInt())
                .temperature(current.path("temp").asDouble(), current.path("feels_like").asDouble())
                .wind(current.path("wind_speed").asDouble())
                .datetime(current.path("dt").asLong())
                .sys(current.path("sunrise").asLong(), current.path("sunset").asLong())
                .timezone(root.path("timezone_offset").asInt())
                .cityName(city)
                .build();
    }

    private CityCoordinates getCoordinatesFromJSON(JsonNode json) {
        String cityName = json.path("name").asText();
        double lat = json.path("lat").asDouble();
        double lon = json.path("lon").asDouble();

        return new CityCoordinates(cityName, lat, lon);
    }

    public CurrentWeather getCurrentWeather(CityCoordinates city) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(URL_FOR_WEATHER + "&lat=%s&lon=%s", city.getLatitude(), city.getLongitude())))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return getCurrentWeatherFromJSON(response.body(), city.getCityName());
    }

    public CityCoordinates getCityCoordinates(String city) throws IOException, CoordinateException, InterruptedException, APIKeyException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(URL_FOR_COORDINATES + "&q=%s", city)))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode json = this.checkResponse(response.body(), city);
        return this.getCoordinatesFromJSON(json);
    }

    public void shutDown() {
        httpClient.close();
    }
}
