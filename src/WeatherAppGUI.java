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
    private JPanel dailyPanel;

    private boolean isDarkMode = false;
    private Color lightBackground = Color.WHITE;
    private Color lightText = Color.BLACK;
    private Color darkBackground = Color.DARK_GRAY;
    private Color darkText = Color.LIGHT_GRAY;
    private boolean isCelcius = false;

    public WeatherAppGUI() {
        super("Weather App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 750); // Adjusted height to fit daily data
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);
        addGuiComponents();
    }

    private void addGuiComponents() {
        JTextField searchTextField = new JTextField();
        searchTextField.setBounds(15, 15, 351, 45);
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));
        add(searchTextField);

        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        JLabel temperatureText = new JLabel("10");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        JLabel weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0, 405, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        JLabel humidityImage = new JLabel(loadImage("src/assets/humidityv3.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeedv3.png"));
        windspeedImage.setBounds(220, 500, 74, 66);
        add(windspeedImage);

        JLabel windspeedText = new JLabel("<html><b>Windspeed</b> 15mph</html>");
        windspeedText.setBounds(310, 500, 85, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

        JLabel dateText = new JLabel("<html><b>Date:</b> </html>");
        dateText.setBounds(0, 70, 450, 80);
        dateText.setFont(new Font("Dialog", Font.PLAIN, 20));
        dateText.setHorizontalAlignment(SwingConstants.CENTER);
        add(dateText);

        cityNameLabel = new JLabel("");
        cityNameLabel.setBounds(0, 70, 450, 30);
        cityNameLabel.setFont(new Font("Dialog", Font.BOLD, 32));
        cityNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(cityNameLabel);

        JButton searchButton = new JButton(loadImage("src/assets/search.png"));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);


        ActionListener searchAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = searchTextField.getText().trim();
                if (userInput.isEmpty()) {
                    return;
                }

                weatherData = WeatherApp.getWeatherData(userInput);
                if (weatherData == null) {
                    cityNameLabel.setText("City not found");
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
                cityNameLabel.setText(userInput + ", " + state + country);

                if(stateType.equals("PPLC")) {
                    cityNameLabel.setText(userInput + ", " + country);
                }
                else if(stateType.equals("PPL") || stateType.equals("PPLA") || stateType.equals("PPLA2") || stateType.equals("PPLA3")){
                    cityNameLabel.setText(userInput + ", " + state);
                }
                else {
                    cityNameLabel.setText(userInput);
                }


                cityNameLabel.setText(userInput);

                JSONObject hourlyData = (JSONObject) weatherData.get("hourly");
                if (hourlyData != null) {
                    double temperature = (double) hourlyData.get("temperature");
                    String weatherCondition = (String) hourlyData.get("weather_condition");
                    long humidity = (long) hourlyData.get("humidity");
                    double windspeed = (double) hourlyData.get("windspeed");

                    temperatureText.setText(temperature + " ");
                    weatherConditionDesc.setText(weatherCondition);
                    humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");
                    windspeedText.setText("<html><b>Windspeed</b> " + windspeed + "mph</html>");

                    if (weatherCondition != null) {

                        switch (weatherCondition) {
                            case "Clear":
                                weatherConditionImage.setIcon(loadImage("src/Assets/clear.png"));
                                break;
                            case "Cloudy":
                                weatherConditionImage.setIcon(loadImage("src/Assets/cloudy.png"));
                                break;
                            case "Rain":
                                weatherConditionImage.setIcon(loadImage("src/Assets/rain.png"));
                                break;
                            case "Snow":
                                weatherConditionImage.setIcon(loadImage("src/Assets/snow.png"));
                                break;
                        }
                    }
                }

                displayDailyWeather();
            }
        };
        searchButton.addActionListener(searchAction);
        searchTextField.addActionListener(searchAction);

        add(searchButton);

        JButton modeToggleButton = new JButton("Dark Mode");
        modeToggleButton.setIcon(loadImage("src/Assets/moonv2.png"));
        modeToggleButton.setBounds(300, 580, 130, 30);
        modeToggleButton.addActionListener(e -> toggleDarkMode());
        add(modeToggleButton);



        dailyPanel = new JPanel();
        dailyPanel.setLayout(new GridLayout(0, 1));
        dailyPanel.setBounds(15, 600, 420, 120);

        JScrollPane dailyScrollPane = new JScrollPane(dailyPanel);
        dailyScrollPane.setBounds(15, 600, 420, 120);
        add(dailyScrollPane);

        applyTheme();
    }

    private void toggleTemperatureUnit() {
        // Logic to toggle between Fahrenheit and Celsius
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    private void applyTheme() {
        Color backgroundColor = isDarkMode ? darkBackground : lightBackground;
        Color textColor = isDarkMode ? darkText : lightText;

        getContentPane().setBackground(backgroundColor);

        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(textColor);
            } else if (comp instanceof JTextField) {
                comp.setBackground(backgroundColor);
                ((JTextField) comp).setForeground(textColor);
            } else if (comp instanceof JButton) {
                comp.setBackground(backgroundColor);
                ((JButton) comp).setForeground(textColor);
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
            String weatherCondition = WeatherApp.convertWeatherCode((long) dayData.get("weather_code"));

            JLabel dailyLabel = new JLabel(date + ": " + weatherCondition + ", Max: " + tempMax + "°F, Min: " + tempMin + "°F");
            dailyPanel.add(dailyLabel);
        }

        dailyPanel.revalidate();
        dailyPanel.repaint();
    }

    private ImageIcon loadImage(String resourcePath) {
        try {
            BufferedImage image = ImageIO.read(new File(resourcePath));
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
