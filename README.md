# Weather SDK for OpenWeatherMap

## Overview

This SDK provides a simple and efficient way to access the OpenWeatherMap API for retrieving current weather data by city name. It supports two operational modes: **On-Demand** (updates weather only when requested) and **Polling** (periodically updates cached cities in the background for zero-latency responses). The SDK includes caching for up to 10 cities, with data considered fresh if less than 10 minutes old. It handles errors gracefully by throwing descriptive exceptions.

Key features:
- Initialization with API key and mode.
- Caching to reduce API calls.
- Unique instances per API key via a factory pattern.
- JSON output matching the specified structure.
- Thread-safe operations for polling mode.

This SDK is built using Java 21 and Maven, with dependencies on Jackson for JSON handling.

## Requirements

- Java 21 or higher.
- Maven for building and dependency management.
- A valid OpenWeatherMap API key (sign up at [openweathermap.org](https://openweathermap.org) for a free key).

## Installation

1. **Clone the Repository** (if applicable):
   ```
   git clone git@github.com:Moisha940/Kameleon.git
   cd <repository-directory>
   ```

2. **Build the Project**:
   Use Maven to compile and package the SDK:
   ```
   mvn clean install
   ```

3. **Add as a Dependency**:
   If you're using Maven in your project, add the following to your `pom.xml` (assuming the SDK is installed in your local Maven repository):
   ```xml
   <dependency>
       <groupId>kameleon.test.task</groupId>
       <artifactId>kameleon</artifactId>
       <version>1.0-SNAPSHOT</version>
   </dependency>
   ```

   Alternatively, copy the generated JAR from `target/kameleon-1.0-SNAPSHOT.jar` into your project's classpath.

## Usage

### Initialization

Use the `WeatherSDKFactory` to create or retrieve SDK instances. Each instance is tied to a unique API key to prevent duplicates.

- **Create a New Instance**:
  ```java
  import kameleon.test.task.factory.WeatherSDKFactory;
  import kameleon.test.task.modes.Mode;

  WeatherSDKFactory factory = WeatherSDKFactory.getInstance();
  WeatherSDK sdk = factory.getNewWeatherSDK("YOUR_API_KEY", Mode.ON_DEMAND);  // or Mode.POLLING
  ```

- **Get an Existing Instance**:
  ```java
  WeatherSDK sdk = factory.getWeatherSDK("YOUR_API_KEY");
  ```

- **Delete an Instance**:
  ```java
  factory.deleteWeatherSDK("YOUR_API_KEY");
  ```
  This shuts down any polling threads and removes the instance.

### Retrieving Weather Data

Call `getCurrentWeatherInCity(String city)` to get the current weather as a JSON string. If cached data is fresh (<10 minutes old), it returns from cache; otherwise, it fetches from the API.

- In **On-Demand Mode**: Updates only on request.
- In **Polling Mode**: Background thread updates all cached cities every 10 minutes.

Example JSON Response:
```json
{
  "weather": {
    "main": "Clouds",
    "description": "scattered clouds"
  },
  "temperature": {
    "temp": 269.6,
    "feels_like": 267.57
  },
  "visibility": 10000,
  "wind": {
    "speed": 1.38
  },
  "datetime": 1675744800,
  "sys": {
    "sunrise": 1675751262,
    "sunset": 1675787560
  },
  "timezone": 3600,
  "name": "Zocca"
}
```

### Error Handling

Methods throw exceptions for failures:
- `APIKeyException`: Invalid or unauthorized API key (e.g., HTTP 401).
- `CoordinateException`: City not found or geolocation error.
- `IOException` / `InterruptedException`: Network issues.

Wrap calls in try-catch:
```java
try {
    String weatherJson = sdk.getCurrentWeatherInCity("London");
    System.out.println(weatherJson);
} catch (APIKeyException e) {
    System.err.println("API Key error: " + e.getMessage());
} catch (CoordinateException e) {
    System.err.println("City not found: " + e.getMessage());
} catch (Exception e) {
    System.err.println("Unexpected error: " + e.getMessage());
}
```

## Examples

### Basic Usage (On-Demand Mode)

```java
package example;

import kameleon.test.task.factory.WeatherSDKFactory;
import kameleon.test.task.modes.Mode;
import kameleon.test.task.services.WeatherSDK;

public class Main {
    public static void main(String[] args) {
        WeatherSDKFactory factory = WeatherSDKFactory.getInstance();
        try {
            WeatherSDK sdk = factory.getNewWeatherSDK("YOUR_API_KEY", Mode.ON_DEMAND);
            String weather = sdk.getCurrentWeatherInCity("London");
            System.out.println("Weather in London: " + weather);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            factory.deleteWeatherSDK("YOUR_API_KEY");
        }
    }
}
```

### Polling Mode with Multiple Requests

```java
package example;

import kameleon.test.task.factory.WeatherSDKFactory;
import kameleon.test.task.modes.Mode;
import kameleon.test.task.services.WeatherSDK;

public class PollingExample {
    public static void main(String[] args) throws InterruptedException {
        WeatherSDKFactory factory = WeatherSDKFactory.getInstance();
        WeatherSDK sdk = factory.getNewWeatherSDK("YOUR_API_KEY", Mode.POLLING);

        // First request: Fetches and caches
        System.out.println(sdk.getCurrentWeatherInCity("New York"));

        // Wait 10 minutes (data should be updated in background)
        Thread.sleep(600000);

        // Second request: Should use fresh polled data
        System.out.println(sdk.getCurrentWeatherInCity("New York"));

        factory.deleteWeatherSDK("YOUR_API_KEY");
    }
}
```

### Handling Cache Limit

Request weather for 11 cities; the oldest will be evicted automatically.

## API Reference

### Key Classes

- **WeatherSDKFactory**: Singleton factory for managing SDK instances.
    - `getInstance()`: Returns the factory.
    - `getNewWeatherSDK(String apiKey, Mode mode)`: Creates a new SDK if apiKey is unique.
    - `getWeatherSDK(String apiKey)`: Retrieves an existing SDK.
    - `deleteWeatherSDK(String apiKey)`: Deletes and shuts down the SDK.

- **WeatherSDK**: Main SDK class.
    - Constructor: Private; use factory.
    - `getCurrentWeatherInCity(String city)`: Returns weather JSON; throws exceptions on error.
    - `shutDown()`: Stops polling (called automatically on delete).
    - `getMode()`: Returns current mode.
    - `getAPI_KEY()`: Returns the API key (for reference).

- **Mode**: Enum for modes (`ON_DEMAND`, `POLLING`).

### Internal Components (Not for Direct Use)

- Caching: Handles up to 10 cities, evicts oldest based on timestamp.
- Polling: Uses `ScheduledExecutorService` for background updates.
- Requests: Uses `HttpClient` for API calls to OpenWeatherMap (Geo API for coordinates, OneCall for weather).

## Limitations and Notes

- Uses OpenWeatherMap's OneCall API (requires subscription for full access; fallback to free tier may limit features).
- Temperatures in Kelvin by default (no units parameter yet).
- Caching validity: 10 minutes (configurable in code if needed).
- Thread safety: Designed for concurrent access, but use in multi-threaded environments with care.
