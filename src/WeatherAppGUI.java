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

    private boolean isDarkMode = false; // Track the current mode
    private Color lightBackground = Color.WHITE;
    private Color lightText = Color.BLACK;
    private Color darkBackground = Color.DARK_GRAY;
    private Color darkText = Color.LIGHT_GRAY;
    private boolean isCelcius = false; // Celcius or Fahrenheit

    public WeatherAppGUI() {
        super("Weather App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 650);
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

        // Initialize the city name label and add it to the frame
        cityNameLabel = new JLabel("");
        cityNameLabel.setBounds(0, 70, 450, 30);
        cityNameLabel.setFont(new Font("Dialog", Font.BOLD, 24));
        cityNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(cityNameLabel);

        JButton searchButton = new JButton(loadImage("src/assets/search.png"));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);

        // Define the shared action listener
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

                String weatherCondition = (String) weatherData.get("weather_condition");
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

                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + " ");
                weatherConditionDesc.setText(weatherCondition);

                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");

                double windspeed = (double) weatherData.get("windspeed");
                windspeedText.setText("<html><b>Windspeed</b> " + windspeed + "mph</html>");

                String date = (String) weatherData.get("time");
                dateText.setText("<html><b>Date:</b> " + date + "</html>");
            }
        };
        //Adds search functionality to button and by also hitting enter
        searchButton.addActionListener(searchAction);
        searchTextField.addActionListener(searchAction);

        add(searchButton);

        // Add a toggle button for light/dark mode
        JButton modeToggleButton = new JButton("Dark Mode");
        modeToggleButton.setIcon(loadImage("src/Assets/moonv2.png"));
        modeToggleButton.setBounds(300, 580, 130, 30);
        modeToggleButton.addActionListener(e -> toggleDarkMode());
        add(modeToggleButton);

        applyTheme(); // Set the initial theme based on isDarkMode

        JButton temperatureButton = new JButton("°F");
        temperatureButton.setBounds(275, 350, 50, 50);
        temperatureButton.setFont(new Font("Dialog", Font.PLAIN, 12));

        ActionListener temperatureAction = new ActionListener() {
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

                isCelcius = !isCelcius; // Toggle the unit
                double temperature = (double) weatherData.get("temperature");

                if (!isCelcius) {
                    temperatureText.setText(temperature + " ");
                    temperatureButton.setText("°F");
                } else {
                    // Convert to Celsius
                    double temperatureInCelcius = (temperature - 32) * 5 / 9;
                    temperatureText.setText(String.format("%.1f", temperatureInCelcius));
                    temperatureButton.setText("°C");
                }
            }
        };

        temperatureButton.addActionListener(temperatureAction);
        add(temperatureButton);
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    private void applyTheme() {
        Color backgroundColor = isDarkMode ? darkBackground : lightBackground;
        Color textColor = isDarkMode ? darkText : lightText;

        // Update the background and text color for components
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

    private ImageIcon loadImage(String resourcePath) {
        try {
            BufferedImage image = ImageIO.read(new File(resourcePath));
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Could not find resource");
        return null;
    }
}
