package kameleon.test.task.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class CurrentWeather {
    @JsonProperty("weather")
    private final Weather weather;

    @JsonProperty("temperature")
    private final Temperature temperature;

    @JsonProperty("visibility")
    private final int visibility;

    @JsonProperty("wind")
    private final Wind wind;

    @JsonProperty("datetime")
    private final long datetime;

    @JsonProperty("sys")
    private final Sys sys;

    @JsonProperty("timezone")
    private final int timezone;

    @JsonProperty("name")
    private final String cityName;

    @JsonIgnore
    private final LocalDateTime timestamp;

    private CurrentWeather(Builder builder) {
        this.weather = builder.weather;
        this.temperature = builder.temperature;
        this.visibility = builder.visibility;
        this.wind = builder.wind;
        this.datetime = builder.datetime;
        this.sys = builder.sys;
        this.timezone = builder.timezone;
        this.cityName = builder.cityName;
        this.timestamp = LocalDateTime.now();
    }

    public Weather getWeather() {
        return weather;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public int getVisibility() {
        return visibility;
    }

    public Wind getWind() {
        return wind;
    }

    public long getDatetime() {
        return datetime;
    }

    public Sys getSys() {
        return sys;
    }

    public int getTimezone() {
        return timezone;
    }

    public String getCityName() {
        return cityName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "CurrentWeather{" +
                "cityName='" + cityName + '\'' +
                ", weather=" + weather +
                ", temperature=" + temperature +
                ", visibility=" + visibility +
                ", windSpeed=" + wind +
                ", datetime=" + datetime +
                ", sys=" + sys +
                ", timezone=" + timezone +
                '}';
    }

    public static class Builder {
        private Weather weather;
        private Temperature temperature;
        private Wind wind;
        private int visibility;
        private long datetime;
        private Sys sys;
        private int timezone;
        private String cityName;

        private Builder() {
        }

        public Builder cityName(String cityName) {
            this.cityName = cityName;
            return this;
        }

        public Builder weather(String main, String description) {
            this.weather = new Weather(main, description);
            return this;
        }

        public Builder temperature(double temp, double feelsLike) {
            this.temperature = new Temperature(temp, feelsLike);
            return this;
        }

        public Builder visibility(int visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder wind(double wind) {
            this.wind = new Wind(wind);
            return this;
        }

        public Builder datetime(long datetime) {
            this.datetime = datetime;
            return this;
        }

        public Builder sys(long sunrise, long sunset) {
            this.sys = new Sys(sunrise, sunset);
            return this;
        }

        public Builder timezone(int timezone) {
            this.timezone = timezone;
            return this;
        }

        public CurrentWeather build() {
            return new CurrentWeather(this);
        }
    }

    public static class Wind {
        @JsonProperty("speed")
        private final double windSpeed;

        public Wind(double windSpeed) {
            this.windSpeed = windSpeed;
        }

        public double getWindSpeed() {
            return windSpeed;
        }

        @Override
        public String toString() {
            return "Wind{windSpeed={" + windSpeed + "}";
        }
    }

    public static class Weather {
        @JsonProperty("main")
        private final String main;

        @JsonProperty("description")
        private final String description;

        public Weather(String main, String description) {
            this.main = main;
            this.description = description;
        }

        public String getMain() {
            return main;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "Weather{main='" + main + "', description='" + description + "'}";
        }
    }

    public static class Temperature {
        @JsonProperty("temp")
        private final double temp;

        @JsonProperty("feels_like")
        private final double feelsLike;

        public Temperature(double temp, double feelsLike) {
            this.temp = temp;
            this.feelsLike = feelsLike;
        }

        public double getTemp() {
            return temp;
        }

        public double getFeelsLike() {
            return feelsLike;
        }

        @Override
        public String toString() {
            return "Temperature{temp=" + temp + ", feelsLike=" + feelsLike + '}';
        }
    }

    public static class Sys {
        @JsonProperty("sunrise")
        private final long sunrise;

        @JsonProperty("sunset")
        private final long sunset;

        public Sys(long sunrise, long sunset) {
            this.sunrise = sunrise;
            this.sunset = sunset;
        }

        public long getSunrise() {
            return sunrise;
        }

        public long getSunset() {
            return sunset;
        }

        @Override
        public String toString() {
            return "Sys{sunrise=" + sunrise + ", sunset=" + sunset + '}';
        }
    }
}