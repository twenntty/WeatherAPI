package org.example;

import javax.json.Json;
import javax.json.JsonArray;
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

public class WeatherDataProcessor {
    private static final String API_KEY = "68639c488edbe733688c755d8120e3bd";
    private static final List<String> STATION_NAMES = List.of("London", "Paris", "Berlin", "New York", "Tokyo");
    private static final String API_URL_FORMAT = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s";

    public static void main(String[] args) {
        WeatherDataProcessor processor = new WeatherDataProcessor();
        List<WeatherObservation> weatherObservations = processor.fetchDataFromApi();

        processor.analyzeExtremeWeatherConditions(weatherObservations);
        processor.calculateMonthlyStatistics(weatherObservations);
        processor.findMonthWithHighestWindSpeed(weatherObservations);
        processor.detectWeatherPatterns(weatherObservations);
        processor.recognizePatterns(weatherObservations);
        processor.calculateAggregations(weatherObservations);
        processor.displayResults(weatherObservations);
    }

    public List<WeatherObservation> fetchDataFromApi() {
        return STATION_NAMES.stream()
                .map(this::fetchObservationForStation)
                .filter(observations -> observations != null)
                .collect(Collectors.toList());
    }


    private WeatherObservation fetchObservationForStation(String stationName) {
        try {
            URL url = new URL(String.format(API_URL_FORMAT, stationName, API_KEY));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    return parseJsonData(response.toString(), stationName);
                }
            } else {
                System.out.println("HTTP request failed with response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static WeatherObservation parseJsonData(String jsonData, String stationName) {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonData));
        JsonObject jsonObject = jsonReader.readObject();

        JsonArray weatherArray = jsonObject.getJsonArray("weather");
        String description = weatherArray.getJsonObject(0).getString("description");

        double temperatureKelvin = jsonObject.getJsonObject("main").getJsonNumber("temp").doubleValue();
        double temperatureCelsius = temperatureKelvin - 273.15;

        double roundedTemperature = Math.round(temperatureCelsius * 10.0) / 10.0;

        double humidity = jsonObject.getJsonObject("main").getInt("humidity");
        double windSpeed = jsonObject.getJsonObject("wind").getJsonNumber("speed").doubleValue();

        LocalDate date = LocalDate.now();

        return new WeatherObservation(date, stationName, description, roundedTemperature, humidity, windSpeed, 0);
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
        int consecutiveRainyDays = 0;
        int consecutiveDays = 0;
        double temperatureIncrease = 0;

        for (WeatherObservation observation : observations) {
            System.out.println("Station: " + observation.getStationId() +
                    ", Date: " + observation.getDate() +
                    ", Temperature: " + observation.getTemperature() +
                    ", Precipitation: " + observation.getPrecipitation());

            if (currentStationId == null || !currentStationId.equals(observation.getStationId())) {
                currentStationId = observation.getStationId();
                consecutiveDays = 1;
                temperatureIncrease = observation.getTemperature();
                if (observation.getPrecipitation() > 0) {
                    consecutiveRainyDays = 1;
                } else {
                    consecutiveRainyDays = 0;
                }
            } else {
                if (observation.getTemperature() - temperatureIncrease >= 5) {
                    consecutiveDays++;
                } else {
                    consecutiveDays = 1;
                    temperatureIncrease = observation.getTemperature();
                }

                if (observation.getPrecipitation() > 0) {
                    consecutiveRainyDays++;
                } else {
                    consecutiveRainyDays = 0;
                }

                if (consecutiveDays >= 5) {
                    System.out.println("Station with at least 5 consecutive days of temperature increase: " + currentStationId);
                }

                if (consecutiveRainyDays > 7) {
                    System.out.println("Station with more than 7 consecutive rainy days: " + currentStationId);
                }
            }
        }
    }

    private static void detectWeatherPatterns(List<WeatherObservation> observations) {
        if (observations == null || observations.isEmpty()) {
            System.out.println("No weather observations available.");
            return;
        }

        detectRainPattern(observations);
        detectTemperatureIncreasePattern(observations);
    }

    private static void detectRainPattern(List<WeatherObservation> observations) {
        String currentStationId = null;
        int consecutiveRainyDays = 0;

        for (WeatherObservation observation : observations) {
            if (currentStationId == null || !currentStationId.equals(observation.getStationId())) {
                currentStationId = observation.getStationId();
                consecutiveRainyDays = (observation.getPrecipitation() > 0) ? 1 : 0;
            } else {
                if (observation.getPrecipitation() > 0) {
                    consecutiveRainyDays++;
                } else {
                    consecutiveRainyDays = 0;
                }

                if (consecutiveRainyDays > 7) {
                    System.out.println("Station with more than 7 consecutive rainy days: " + currentStationId);
                }
            }
        }
    }

    private static void detectTemperatureIncreasePattern(List<WeatherObservation> observations) {
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

    private static void calculateMonthlyStatistics(List<WeatherObservation> observations) {
        if (observations == null || observations.isEmpty()) {
            System.out.println("No weather observations available.");
            return;
        }

        Map<String, List<WeatherObservation>> observationsByMonth = observations.stream()
                .collect(Collectors.groupingBy(observation -> observation.getDate().getMonth().toString()));

        for (Map.Entry<String, List<WeatherObservation>> entry : observationsByMonth.entrySet()) {
            String month = entry.getKey();
            List<WeatherObservation> monthlyObservations = entry.getValue();

            double averageTemperature = monthlyObservations.stream()
                    .mapToDouble(WeatherObservation::getTemperature)
                    .average()
                    .orElse(0);

            double averageHumidity = monthlyObservations.stream()
                    .mapToDouble(WeatherObservation::getHumidity)
                    .average()
                    .orElse(0);

            double totalPrecipitation = monthlyObservations.stream()
                    .mapToDouble(WeatherObservation::getPrecipitation)
                    .sum();

            System.out.printf("Month: %s, Average Temperature: %.1f, Average Humidity: %.1f, Total Precipitation: %.1f\n",
                    month, averageTemperature, averageHumidity, totalPrecipitation);
        }
    }

    private static void findMonthWithHighestWindSpeed(List<WeatherObservation> observations) {
        if (observations == null || observations.isEmpty()) {
            System.out.println("No weather observations available.");
            return;
        }

        Map<String, Double> averageWindSpeedByMonth = observations.stream()
                .collect(Collectors.groupingBy(
                        observation -> observation.getDate().getMonth().toString(),
                        Collectors.averagingDouble(WeatherObservation::getWindSpeed)
                ));

        String monthWithHighestWindSpeed = averageWindSpeedByMonth.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");

        double highestWindSpeed = averageWindSpeedByMonth.getOrDefault(monthWithHighestWindSpeed, 0.0);

        System.out.printf("Month with highest average wind speed: %s, Highest Wind Speed: %.1f\n",
                monthWithHighestWindSpeed, highestWindSpeed);
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

        System.out.printf("Average Temperature: %.1f\n", averageTemperature);
        System.out.println("Average Humidity: " + averageHumidity);
        System.out.printf("Average Wind Speed: %.1f\n", averageWindSpeed);
    }

    private static void displayResults(List<WeatherObservation> observations) {
        System.out.println("Weather Observations:");
        observations.forEach(System.out::println);
    }
}
