import javax.swing.*;

public class WeatherAppGUI extends JFrame {
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
        add(searchTextField);
    }
}
