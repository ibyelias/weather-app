import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGUI extends JFrame {
    private JSONObject weatherData;
    private JLabel cityNameLabel;
    private JLabel temperatureText;
    private JButton temperatureButton;
    private JLabel windspeedText;
    private JLabel humidityText;
    private JLabel weatherConditionDesc;
    private JLabel weatherConditionImage;
    private JPanel dailyPanel;

    private boolean isDarkMode = false;
    private Color lightBackground = Color.WHITE;
    private Color lightText = Color.BLACK;
    private Color darkBackground = Color.DARK_GRAY;
    private Color darkText = Color.LIGHT_GRAY;
    private boolean isMetric = false;

    public WeatherAppGUI() {
        super("Weather App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(true);
        addGuiComponents();
        applyTheme();
    }

    private void addGuiComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel(new GridBagLayout());  // Use GridBagLayout for centering components

        JTextField searchTextField = new JTextField(20);
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));
        topPanel.add(searchTextField, BorderLayout.CENTER);

        JButton searchButton = new JButton(loadImage("src/assets/search.png"));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topPanel.add(searchButton, BorderLayout.EAST);

        // Constraints for GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  // Add padding around components
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // City name label
        cityNameLabel = new JLabel("");
        cityNameLabel.setFont(new Font("Dialog", Font.BOLD, 32));
        cityNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        centerPanel.add(cityNameLabel, gbc);

        // Weather icon
        weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        centerPanel.add(weatherConditionImage, gbc);

        // Temperature text
        temperatureText = new JLabel("10");
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2;
        centerPanel.add(temperatureText, gbc);

        // Weather description
        weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 3;
        centerPanel.add(weatherConditionDesc, gbc);

        // Details panel for humidity and windspeed
        JPanel detailsPanel = new JPanel(new FlowLayout());
        detailsPanel.add(new JLabel(loadImage("src/assets/humidityv3.png")));
        humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        detailsPanel.add(humidityText);

        detailsPanel.add(new JLabel(loadImage("src/assets/windspeedv3.png")));
        windspeedText = new JLabel("<html><b>Windspeed</b> 15mph</html>");
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        detailsPanel.add(windspeedText);

        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        centerPanel.add(detailsPanel, gbc);

        // Dark mode toggle button
        JButton modeToggleButton = new JButton("Dark Mode");
        modeToggleButton.setIcon(loadImage("src/assets/moonv2.png"));
        modeToggleButton.addActionListener(e -> toggleDarkMode());
        modeToggleButton.setPreferredSize((new Dimension(5,50)));
        gbc.gridy = 5;
        centerPanel.add(modeToggleButton, gbc);

        // Temperature unit toggle button
        temperatureButton = new JButton("°F");
        temperatureButton.setFont(new Font("Dialog", Font.PLAIN, 12));
        temperatureButton.addActionListener(e -> toggleTemperatureUnit());
        gbc.gridy = 6;
        centerPanel.add(temperatureButton, gbc);

        // Daily weather panel with scroll pane
        dailyPanel = new JPanel();
        dailyPanel.setLayout(new GridLayout(0, 7, 10, 10)); // 7 columns for each day of the week

        JScrollPane dailyScrollPane = new JScrollPane(dailyPanel);
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 5; // Make the daily forecast area expand
        centerPanel.add(dailyScrollPane, gbc);

        searchButton.addActionListener(e -> searchWeather(searchTextField));
        searchTextField.addActionListener(e -> searchWeather(searchTextField));

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        applyTheme();
    }

    private void searchWeather(JTextField searchTextField) {
        String userInput = searchTextField.getText().trim();
        if (userInput.isEmpty()) {
            return;
        }

        weatherData = WeatherApp.getWeatherData(userInput);
        if (weatherData == null) {
            cityNameLabel.setText("City not found");
            return;
        }

        String stateType = (String) weatherData.get("type");
        String state = (String) weatherData.get("state");
        String country = (String) weatherData.get("country");
        String postcode = (String) weatherData.get("postcode");

        if(stateType.equals("PPLC")) {
            cityNameLabel.setText(userInput + ", " + country);
        }
        else if(stateType.equals("PPL") || stateType.equals("PPLA") || stateType.equals("PPLA2") || stateType.equals("PPLA3")){
            cityNameLabel.setText(userInput + ", " + state);
        }
        else {
            cityNameLabel.setText(userInput);
        }

        JSONObject hourlyData = (JSONObject) weatherData.get("hourly");
        if (hourlyData != null) {
            double temperature = (double) hourlyData.get("temperature");
            String weatherCondition = (String) hourlyData.get("weather_condition");
            long humidity = (long) hourlyData.get("humidity");
            double windspeed = (double) hourlyData.get("windspeed");

            isMetric = false;
            temperatureText.setText(temperature + "°F");
            temperatureButton.setText("°F");
            humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");
            windspeedText.setText("<html><b>Windspeed</b> " + windspeed + "mph</html>");

            weatherConditionDesc.setText(weatherCondition);
            updateWeatherIcon(weatherCondition);
        }

        displayDailyWeather();
    }

    private void updateWeatherIcon(String weatherCondition) {
        String iconPath;
        switch (weatherCondition.toLowerCase()) {
            case "clear":
                iconPath = "src/assets/clear.png";
                break;
            case "cloudy":
                iconPath = "src/assets/cloudy.png";
                break;
            case "rain":
                iconPath = "src/assets/rain.png";
                break;
            case "snow":
                iconPath = "src/assets/snow.png";
                break;
            default:
                iconPath = "src/assets/clear.png";
                break;
        }
        weatherConditionImage.setIcon(loadImage(iconPath));
    }

    private void toggleTemperatureUnit() {
        isMetric = !isMetric;
        String currentText = temperatureText.getText();
        String currentWindspeed = windspeedText.getText();
        String unit = isMetric ? "°C" : "°F";
        String windspeedUnit = isMetric ? "km/h" : "mph";

        try {
            // Remove the degree symbol and unit from the text to get the numeric part
            double temp = Double.parseDouble(currentText.replaceAll("[^0-9.-]", ""));
            double windspeed = Double.parseDouble(currentWindspeed.replaceAll("[^0-9.-]", ""));

            // Convert temperature based on the current unit
            temp = isMetric ? (temp - 32) * 5 / 9 : (temp * 9 / 5) + 32;
            windspeed = isMetric ? windspeed * 1.60934 : windspeed / 1.60934 ;

            // Update the temperature label with the converted value and the new unit
            temperatureText.setText(String.format("%.1f", temp) + unit);
            temperatureButton.setText(unit);
            windspeedText.setText(String.format("%.1f", windspeed) + windspeedUnit);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            temperatureText.setText("Error");
        }

        displayDailyWeather();
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    private void applyTheme() {
        Color backgroundColor = isDarkMode ? darkBackground : lightBackground;
        Color textColor = isDarkMode ? darkText : lightText;

        getContentPane().setBackground(backgroundColor);
        applyThemeToComponents(getContentPane(), backgroundColor, textColor);
    }

    private void applyThemeToComponents(Container container, Color backgroundColor, Color textColor) {
        for (Component comp : container.getComponents()) {
            comp.setBackground(backgroundColor);
            comp.setForeground(textColor);

            if (comp instanceof Container) {
                applyThemeToComponents((Container) comp, backgroundColor, textColor);
            }
        }
    }

    private void displayDailyWeather() {
        dailyPanel.removeAll();

        JSONArray dailyData = (JSONArray) weatherData.get("daily");
        if (dailyData == null) return;

        for (Object dayObj : dailyData) {
            JSONObject dayData = (JSONObject) dayObj;
            String date = (String) dayData.get("date");
            double tempMax = (double) dayData.get("temperature_max");
            double tempMin = (double) dayData.get("temperature_min");

            JPanel dayPanel = new JPanel();
            dayPanel.setLayout(new BoxLayout(dayPanel, BoxLayout.Y_AXIS));
            dayPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

            JLabel dateLabel = new JLabel(date);
            dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dayPanel.add(dateLabel);

            JLabel maxTempLabel;
            JLabel minTempLabel;

            if (!isMetric) {
                maxTempLabel = new JLabel("Max: " + tempMax + "°F");
                minTempLabel = new JLabel("Min: " + tempMin + "°F");
            } else {
                maxTempLabel = new JLabel("Max: " + String.format("%.1f", ((tempMax - 32) * 5 / 9)) + "°C");
                minTempLabel = new JLabel("Min: " + String.format("%.1f", ((tempMin - 32) * 5 / 9)) + "°C");
            }

            maxTempLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dayPanel.add(maxTempLabel);

            minTempLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dayPanel.add(minTempLabel);

            dailyPanel.add(dayPanel);
        }

        dailyPanel.revalidate();
        dailyPanel.repaint();
        applyTheme();
    }

    private ImageIcon loadImage(String resourcePath) {
        try {
            BufferedImage image = ImageIO.read(new File(resourcePath));
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WeatherAppGUI().setVisible(true));
    }
}