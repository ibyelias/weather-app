import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Retrieves weather data from API for display in the GUI.
 *
 * @author Michael Broughton & Ibrahim Elias
 * @version 1.0
 */
public class WeatherApp {
    /**
     * Fetches weather data for a given location.
     *
     * @param locationName the location to fetch the weather data for
     * @return the weather data as a JSONObject, or null if no data is available
     */
    public static JSONObject getWeatherData(String locationName) {
        // Get location coordinates using the geolocation API
        JSONArray locationData = getLocationData(locationName);
        if (locationData == null || locationData.isEmpty()) {
            System.out.println("Error: Location data not found.");
            return null;
        }

        // Extract latitude, longitude, state, and country data
        JSONObject location = (JSONObject) locationData.get(0);
        String name = (String) location.get("name");
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");
        String state = (String) location.get("admin1");
        String country = (String) location.get("country");
        String stateType = (String) location.get("feature_code");

        // Build API request URL with location coordinates
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,wind_speed_10m_max" +
                "&temperature_unit=fahrenheit&wind_speed_unit=mph&precipitation_unit=inch&timezone=America%2FNew_York";

        try {
            // Call API and get response
            HttpURLConnection conn = fetchApiResponse(urlString);

            // Check response status; 200 means success
            if (conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            }

            // Store resulting JSON data
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect();

            // Parse JSON response
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(resultJson.toString());

            // Retrieve hourly data
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");
            JSONArray timeData = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(timeData);

            // Format current time for display
            String time = getCurrentTime();
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDateTime dateTime = LocalDateTime.parse(time, inputFormatter);
            String formattedDate = dateTime.format(outputFormatter);

            // Retrieve other hourly weather data
            double temperature = (double) ((JSONArray) hourly.get("temperature_2m")).get(index);
            String weatherCondition = convertWeatherCode((long) ((JSONArray) hourly.get("weather_code")).get(index));
            long humidity = (long) ((JSONArray) hourly.get("relative_humidity_2m")).get(index);
            double windspeed = (double) ((JSONArray) hourly.get("wind_speed_10m")).get(index);

            // Create JSON object for hourly weather data
            JSONObject hourlyWeatherData = new JSONObject();
            hourlyWeatherData.put("temperature", temperature);
            hourlyWeatherData.put("weather_condition", weatherCondition);
            hourlyWeatherData.put("humidity", humidity);
            hourlyWeatherData.put("windspeed", windspeed);

            // Retrieve daily data
            JSONObject daily = (JSONObject) resultJsonObj.get("daily");
            JSONArray dailyTime = (JSONArray) daily.get("time");
            JSONArray maxTemperatureData = (JSONArray) daily.get("temperature_2m_max");
            JSONArray minTemperatureData = (JSONArray) daily.get("temperature_2m_min");
            JSONArray dailyWeatherCode = (JSONArray) daily.get("weather_code");

            // Create array to hold each day's weather data
            JSONArray dailyWeatherArray = new JSONArray();
            for (int i = 0; i < dailyTime.size(); i++) {
                JSONObject dayWeather = new JSONObject();
                dayWeather.put("date", dailyTime.get(i));
                dayWeather.put("temperature_max", maxTemperatureData.get(i));
                dayWeather.put("temperature_min", minTemperatureData.get(i));
                dayWeather.put("weather_code", convertWeatherCode((long) dailyWeatherCode.get(i)));
                dailyWeatherArray.add(dayWeather);
            }

            // Build main weather JSON object containing hourly and daily data
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("time", formattedDate);
            weatherData.put("state", state);
            weatherData.put("country", country);
            weatherData.put("type", stateType);
            weatherData.put("name", name);
            weatherData.put("hourly", hourlyWeatherData);
            weatherData.put("daily", dailyWeatherArray);

            return weatherData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves location data for a given location name.
     *
     * @param locationName the location name to retrieve data for
     * @return a JSONArray of location data, or null if no data is available
     */
    public static JSONArray getLocationData(String locationName) {
        locationName = locationName.replaceAll(" ", "+");
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);
            if (conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            }

            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect();

            JSONParser parser = new JSONParser();
            JSONObject resultsJsonObj = (JSONObject) parser.parse(resultJson.toString());
            return (JSONArray) resultsJsonObj.get("results");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sends a GET request to the API and returns the response.
     *
     * @param urlString the URL of the API endpoint to call
     * @return the HttpURLConnection object representing the API response
     * @throws IOException if an error occurs while sending the request
     */
    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Finds the index of the current time in the hourly weather data.
     *
     * @param timeList the hourly weather data to search
     * @return the index of the current time, or -1 if not found
     */
    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();
        for (int i = 0; i < timeList.size(); i++) {
            if (timeList.get(i).equals(currentTime)) {
                return i;
            }
        }
        return 0;
    }
    /**
     * Gets the current time in the format "HH:mm".
     *
     * @return the current time as a string
     */
    private static String getCurrentTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");
        return currentDateTime.format(formatter);
    }
    /**
     * Converts a weather code to a human-readable description.
     *
     * @param weathercode the weather code to convert
     * @return the human-readable description of the weather code
     */
    static String convertWeatherCode(long weathercode) {
        if (weathercode == 0L) return "Clear";
        if (weathercode <= 3L) return "Cloudy";
        if (weathercode >= 51L && weathercode <= 67L || weathercode >= 80L && weathercode <= 99L) return "Rain";
        if (weathercode >= 71L && weathercode <= 77L) return "Snow";
        return "Unknown";
    }
}
