package org.example;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class WeatherDataGUI extends JFrame {

    private JTable dataTable;

    public WeatherDataGUI(List<WeatherObservation> weatherObservations) {
        initializeGUI();
        displayDataInTable(weatherObservations);
    }

    private void initializeGUI() {
        setTitle("Weather Data Analysis");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    private void displayDataInTable(List<WeatherObservation> weatherObservations) {
        String[] columnNames = {"Date", "Station", "Description", "Temperature", "Humidity", "Wind Speed", "Precipitation"};

        Object[][] data = new Object[weatherObservations.size()][columnNames.length];

        for (int i = 0; i < weatherObservations.size(); i++) {
            WeatherObservation observation = weatherObservations.get(i);
            data[i][0] = observation.getDate();
            data[i][1] = observation.getStationId();
            data[i][2] = observation.getDescription();
            data[i][3] = observation.getTemperature();
            data[i][4] = observation.getHumidity();
            data[i][5] = observation.getWindSpeed();
            data[i][6] = observation.getPrecipitation();
        }

        dataTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(dataTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WeatherDataProcessor processor = new WeatherDataProcessor();
            List<WeatherObservation> weatherObservations = processor.fetchDataFromApi();

            WeatherDataGUI gui = new WeatherDataGUI(weatherObservations);
            gui.setVisible(true);
        });
    }
}
