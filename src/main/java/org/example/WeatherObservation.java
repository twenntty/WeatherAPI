package org.example;

import java.time.LocalDate;

public class WeatherObservation {
    private LocalDate date;
    private String stationId;
    private String description;
    private double temperature;
    private double humidity;
    private double windSpeed;
    private double precipitation;

    public WeatherObservation(LocalDate date, String stationId, String description, double temperature, double humidity, double windSpeed, double precipitation) {
        this.date = date;
        this.stationId = stationId;
        this.description = description;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.precipitation = precipitation;
    }

    public String getStationId() {
        return stationId;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "WeatherObservation{" +
                "date=" + date +
                ", stationId='" + stationId + '\'' +
                ", description='" + description + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", windSpeed=" + windSpeed +
                ", precipitation=" + precipitation +
                '}';
    }

    public LocalDate getDate() {
        return date;
    }
}
