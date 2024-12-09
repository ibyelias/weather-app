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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * The main GUI for the Weather App.
 * This class is responsible for displaying the weather data and handling user interactions.
 * It communicates with the {@link WeatherApp} class to fetch and display the weather data.
 *
 * @author Michael Broughton & Ibrahim Elias
 * @version 1.0
 */
public class WeatherAppGUI extends JFrame {
    private JSONObject weatherData;
    private JLabel cityNameLabel;
    private JLabel temperatureText;
    private JLabel windspeedText;
    private JLabel humidityText;
    private JLabel weatherConditionDesc;
    private JLabel weatherConditionImage;
    private JPanel dailyPanel;

    private boolean isDarkMode = false;
    private Color lightBackground = new Color(227, 248, 250);
    private Color lightText = Color.BLACK;
    private Color darkBackground = Color.DARK_GRAY;
    private Color darkText = Color.LIGHT_GRAY;
    private boolean isMetric = false;

    public WeatherAppGUI() {
        super("Weather App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(true);
        addGuiComponents();
        applyTheme();
        pack();
    }

    /**
     * Initializes the GUI components.
     */
    private void addGuiComponents() {
        // Create the menu bar
        JMenuBar menuBar = new JMenuBar();

        // Create the Settings menu
        JMenu settingsMenu = new JMenu("Settings");

        // Dark Mode toggle menu item
        JCheckBoxMenuItem darkModeItem = new JCheckBoxMenuItem("Dark Mode");
        darkModeItem.setState(isDarkMode); // Reflect current mode
        darkModeItem.addActionListener(e -> {
            toggleDarkMode();
            darkModeItem.setState(isDarkMode); // Update state
        });
        settingsMenu.add(darkModeItem);

        // Temperature Unit toggle menu item
        JCheckBoxMenuItem tempUnitItem = new JCheckBoxMenuItem("Use Metric (°C)");
        tempUnitItem.setState(isMetric); // Reflect current unit
        tempUnitItem.addActionListener(e -> {
            toggleTemperatureUnit();
            tempUnitItem.setState(isMetric); // Update state
        });
        settingsMenu.add(tempUnitItem);

        // Add the Settings menu to the menu bar
        menuBar.add(settingsMenu);
        setJMenuBar(menuBar);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());

        JTextField searchTextField = new JTextField(20);
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));
        topPanel.add(searchTextField, BorderLayout.CENTER);

        JButton searchButton = new JButton(loadImage("src/assets/searchv4.png"));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topPanel.add(searchButton, BorderLayout.EAST);

        // Center Panel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        cityNameLabel = new JLabel("");
        cityNameLabel.setFont(new Font("Dialog", Font.BOLD, 32));
        cityNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        centerPanel.add(cityNameLabel, gbc);

        weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        centerPanel.add(weatherConditionImage, gbc);

        temperatureText = new JLabel("Please Enter a City or Postal Code.");
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2;
        centerPanel.add(temperatureText, gbc);

        weatherConditionDesc = new JLabel(" ");
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 3;
        centerPanel.add(weatherConditionDesc, gbc);

        JPanel detailsPanel = new JPanel(new FlowLayout());
        detailsPanel.add(new JLabel(loadImage("src/assets/humidityv3.png")));
        humidityText = new JLabel("<html><b>Humidity</b> </html>");
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        detailsPanel.add(humidityText);

        detailsPanel.add(new JLabel(loadImage("src/assets/windspeedv3.png")));
        windspeedText = new JLabel("<html><b>Windspeed</b> </html>");
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        detailsPanel.add(windspeedText);

        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        centerPanel.add(detailsPanel, gbc);

        dailyPanel = new JPanel();
        dailyPanel.setLayout(new GridLayout(0, 7, 10, 10));
        JScrollPane dailyScrollPane = new JScrollPane(dailyPanel);
        dailyPanel.setPreferredSize(new Dimension(700, 175));
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 5;
        centerPanel.add(dailyScrollPane, gbc);

        searchButton.addActionListener(e -> searchWeather(searchTextField));
        searchTextField.addActionListener(e -> searchWeather(searchTextField));

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Searches for the weather data based on the provided search text.
     *
     * @param searchTextField the text field containing the city name or zip code
     */
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

        String name = (String) weatherData.get("name");
        String stateType = (String) weatherData.get("type");
        String state = (String) weatherData.get("state");
        String country = (String) weatherData.get("country");

        if(stateType.equals("PPLC")) {
            cityNameLabel.setText(name + ", " + country);
        }
        else if(stateType.equals("PPL") || stateType.equals("PPLA") || stateType.equals("PPLA2") || stateType.equals("PPLA3")){
            cityNameLabel.setText(name + ", " + state);
        }
        else {
            cityNameLabel.setText(name);
        }

        JSONObject hourlyData = (JSONObject) weatherData.get("hourly");
        if (hourlyData != null) {
            double temperature = (double) hourlyData.get("temperature");
            String weatherCondition = (String) hourlyData.get("weather_condition");
            long humidity = (long) hourlyData.get("humidity");
            double windspeed = (double) hourlyData.get("windspeed");

            isMetric = false;
            temperatureText.setText(temperature + "°F");
            humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");
            windspeedText.setText("<html><b>Windspeed</b> " + windspeed + "mph</html>");

            weatherConditionDesc.setText(weatherCondition);
            updateWeatherIcon(weatherCondition);
        }

        displayDailyWeather();
    }

    /**
     * Updates the weather icon based on the provided weather condition.
     *
     * @param weatherCondition the weather condition to update the icon for
     */
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

    /**
     * Toggles between Fahrenheit and Celsius temperature units.
     */
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
            windspeedText.setText(String.format("%.1f", windspeed) + windspeedUnit);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            temperatureText.setText("Error");
        }

        displayDailyWeather();
    }

    /**
     * Toggles between light and dark mode themes.
     */
    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    /**
     * Applies the current theme to all components in the GUI.
     */
    private void applyTheme() {
        Color backgroundColor = isDarkMode ? darkBackground : lightBackground;
        Color textColor = isDarkMode ? darkText : lightText;

        getContentPane().setBackground(backgroundColor);
        applyThemeToComponents(getContentPane(), backgroundColor, textColor);
    }

    /**
     * Applies the theme to the specified container and its components.
     *
     * @param container the container to apply the theme to
     * @param backgroundColor the background color to use
     * @param textColor the text color to use
     */
    private void applyThemeToComponents(Container container, Color backgroundColor, Color textColor) {
        for (Component comp : container.getComponents()) {
            comp.setBackground(backgroundColor);
            comp.setForeground(textColor);

            if (comp instanceof Container) {
                applyThemeToComponents((Container) comp, backgroundColor, textColor);
            }
        }
    }

    /**
     * Retrieves the weather icon for the specified weather condition.
     *
     * @param weatherCondition the weather condition to get the icon for
     * @param width the desired width of the icon
     * @param height the desired height of the icon
     * @return the weather icon
     */
    private ImageIcon getWeatherIcon(String weatherCondition, int width, int height) {
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
        return resizeIcon(loadImage(iconPath), width, height);
    }

    // Helper method to resize icons
    /**
     * Resizes an icon to the specified width and height.
     *
     * @param icon the icon to resize
     * @param width the desired width
     * @param height the desired height
     * @return the resized icon
     */
    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        if (icon == null) return null;
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }


    /**
     * Displays the daily weather forecast.
     */
    private void displayDailyWeather() {
        dailyPanel.removeAll();

        JSONArray dailyData = (JSONArray) weatherData.get("daily");
        if (dailyData == null) return;

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Adjust based on input format
        DateTimeFormatter monthDayFormatter = DateTimeFormatter.ofPattern("MMM d"); // For Dec 1

        for (Object dayObj : dailyData) {
            JSONObject dayData = (JSONObject) dayObj;
            String date = (String) dayData.get("date");
            String weatherCondition = (String) dayData.get("weather_code");
            double tempMax = (double) dayData.get("temperature_max");
            double tempMin = (double) dayData.get("temperature_min");

            // Parse and format the date
            LocalDate parsedDate = LocalDate.parse(date, inputFormatter);
            String formattedDate = monthDayFormatter.format(parsedDate);
            formattedDate += getOrdinalSuffix(parsedDate.getDayOfMonth()); // Add ordinal suffix
            String dayOfWeek = parsedDate.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH); // Get day name

            // Create a panel for each day's weather
            JPanel dayPanel = new JPanel();
            dayPanel.setLayout(new BoxLayout(dayPanel, BoxLayout.Y_AXIS));
            dayPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

            // Add date and day labels
            JLabel dateLabel = new JLabel(formattedDate);
            dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dayPanel.add(dateLabel);

            JLabel dayOfWeekLabel = new JLabel(dayOfWeek);
            dayOfWeekLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dayPanel.add(dayOfWeekLabel);

            // Add weather icon
            ImageIcon weatherIcon = getWeatherIcon(weatherCondition, 50, 50); // Resize to fit panel
            JLabel iconLabel = new JLabel(weatherIcon);
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dayPanel.add(iconLabel);

            // Add temperature labels
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
            pack();
        }

        dailyPanel.revalidate();
        dailyPanel.repaint();
        applyTheme();
    }

    // Helper method to get ordinal suffix for the day
    /**
     * Retrieves the ordinal suffix for the specified day.
     *
     * @param day the day to get the ordinal suffix for
     * @return the ordinal suffix
     */
    private String getOrdinalSuffix(int day) {
        if (day >= 11 && day <= 13) return "th"; // Handle special cases
        switch (day % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    /**
     * Loads an image from the specified resource path.
     *
     * @param resourcePath the path to the image resource
     * @return the loaded image
     */
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