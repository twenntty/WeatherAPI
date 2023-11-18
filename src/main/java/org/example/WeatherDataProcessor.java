package org.example;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.json.JsonArray;

public class WeatherDataProcessor {

    private static final String API_KEY = "68639c488edbe733688c755d8120e3bd";
    private static final String CITY_NAME = "London";
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q=" + CITY_NAME + "&appid=" + API_KEY;

    public static void main(String[] args) {
        List<WeatherObservation> weatherObservations = fetchDataFromApi();

        analyzeExtremeWeatherConditions(weatherObservations);
        recognizePatterns(weatherObservations);
        calculateAggregations(weatherObservations);
        displayResults(weatherObservations);
    }

    private static List<WeatherObservation> fetchDataFromApi() {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                return parseJsonData(response.toString());
            } else {
                System.out.println("HTTP request failed with response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static List<WeatherObservation> parseJsonData(String jsonData) {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonData));
        JsonObject jsonObject = jsonReader.readObject();

        JsonArray weatherArray = jsonObject.getJsonArray("weather");
        String description = weatherArray.getJsonObject(0).getString("description");

        double temperatureKelvin = jsonObject.getJsonObject("main").getJsonNumber("temp").doubleValue();
        double temperatureCelsius = temperatureKelvin - 273.15;

        int humidity = jsonObject.getJsonObject("main").getInt("humidity");
        double windSpeed = jsonObject.getJsonObject("wind").getJsonNumber("speed").doubleValue();

        LocalDate date = LocalDate.now();

        return List.of(new WeatherObservation(date, description, temperatureCelsius, humidity, 0, windSpeed));
    }

    private static void analyzeExtremeWeatherConditions(List<WeatherObservation> observations) {
        if (observations == null || observations.isEmpty()) {
            System.out.println("No weather observations available.");
            return;
        }

        Map<String, Double> averageTemperatureByStation = observations.stream()
                .collect(Collectors.groupingBy(WeatherObservation::getStationId,
                        Collectors.averagingDouble(WeatherObservation::getTemperature)));

        List<String> hottestStations = averageTemperatureByStation.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<String> coldestStations = averageTemperatureByStation.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        System.out.println("Top 10 hottest stations: " + hottestStations);
        System.out.println("Top 10 coldest stations: " + coldestStations);

        Map<String, Double> averageHumidityByStation = observations.stream()
                .collect(Collectors.groupingBy(WeatherObservation::getStationId,
                        Collectors.averagingDouble(WeatherObservation::getHumidity)));

        List<String> mostHumidStations = averageHumidityByStation.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        System.out.println("Top 10 most humid stations: " + mostHumidStations);
    }

    private static void recognizePatterns(List<WeatherObservation> observations) {
        if (observations == null || observations.isEmpty()) {
            System.out.println("No weather observations available.");
            return;
        }

        String currentStationId = null;
        int consecutiveDays = 0;
        double temperatureIncrease = 0;

        for (WeatherObservation observation : observations) {
            if (currentStationId == null || !currentStationId.equals(observation.getStationId())) {
                currentStationId = observation.getStationId();
                consecutiveDays = 1;
                temperatureIncrease = observation.getTemperature();
            } else {
                if (observation.getTemperature() - temperatureIncrease >= 5) {
                    consecutiveDays++;
                } else {
                    consecutiveDays = 1;
                    temperatureIncrease = observation.getTemperature();
                }

                if (consecutiveDays >= 5) {
                    System.out.println("Station with at least 5 consecutive days of temperature increase: " + currentStationId);
                }
            }
        }
    }

    private static void calculateAggregations(List<WeatherObservation> observations) {
        double averageTemperature = observations.stream()
                .mapToDouble(WeatherObservation::getTemperature)
                .average()
                .orElse(0);

        int averageHumidity = (int) observations.stream()
                .mapToDouble(WeatherObservation::getHumidity)
                .average()
                .orElse(0);

        double averageWindSpeed = observations.stream()
                .mapToDouble(WeatherObservation::getWindSpeed)
                .average()
                .orElse(0);

        System.out.println("Average Temperature: " + averageTemperature);
        System.out.println("Average Humidity: " + averageHumidity);
        System.out.println("Average Wind Speed: " + averageWindSpeed);
    }

    private static void displayResults(List<WeatherObservation> observations) {
        System.out.println("Weather Observations:");
        observations.forEach(System.out::println);
    }
}
