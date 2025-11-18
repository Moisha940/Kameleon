package kameleon.test.task.model;

public class CityCoordinates {
    private final String cityName;
    private final double latitude;
    private final double longitude;

    public CityCoordinates(String cityName, double latitude, double longitude) {
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getCityName() {
        return cityName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
