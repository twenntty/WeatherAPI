package org.example;

import java.time.LocalDate;

public class WeatherObservation {
    private LocalDate date;
    private String stationId;
    private double temperature;
    private int humidity;
    private double precipitation;
    private double windSpeed;

    public WeatherObservation(LocalDate date, String stationId, double temperature, int humidity, double precipitation, double windSpeed) {
        this.date = date;
        this.stationId = stationId;
        this.temperature = temperature;
        this.humidity = humidity;
        this.precipitation = precipitation;
        this.windSpeed = windSpeed;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getStationId() {
        return stationId;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    @Override
    public String toString() {
        return "WeatherObservation (" +
                date +
                ", temperature = " + temperature + " C" +
                ", humidity = " + humidity +
                ", precipitation = " + precipitation +
                ", windSpeed = " + windSpeed +
                ')';
    }
}
