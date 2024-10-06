import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class SavingsPanel extends JPanel {
    private JTextField inputField;  // Field to input the amount
    private JTextArea amountDisplay;  // Field to display all added amounts
    private Connection connection;
    private static final int MAX_ENTRIES = 16;  // Limit to the number of entries per month

    private JComboBox<String> monthComboBox;  // ComboBox to select month

    public SavingsPanel() {
        // Set up the database connection
        connectToDatabase();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);  // Set background color to white

        // Set up the font
        Font font = new Font("Arial", Font.PLAIN, 20);  // Create a font with size 20

        // Panel for input
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        inputPanel.setBackground(Color.WHITE);  // Set background color to white
        JLabel label = new JLabel("Enter amount to add:");
        label.setFont(font);  // Set font for label
        inputPanel.add(label);

        inputField = new JTextField(10);
        inputField.setFont(font);  // Set font for input field
        inputPanel.add(inputField);

        // Button to add amount
        JButton addButton = new JButton("Add to Jar");
        addButton.setFont(font);  // Set font for button
        inputPanel.add(addButton);

        // ComboBox for selecting the month
        monthComboBox = new JComboBox<>(new String[]{
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        });
        monthComboBox.setFont(font);  // Set font for combo box
        inputPanel.add(monthComboBox);

        add(inputPanel, BorderLayout.NORTH);

        // Panel for jar image and amount display
        JPanel jarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon jarImage = new ImageIcon("E://SEMESTER SUBJECTS//3rd SEMESTER//ADVANCE OOPS//MINI PROJECT//budget_app//Photos//jar.jpg");
                Image img = jarImage.getImage();
                int imgWidth = 800;
                int imgHeight = 950;
                int x = (getWidth() - imgWidth) / 2;
                int y = (getHeight() - imgHeight) / 2;
                g.drawImage(img, x, y, imgWidth, imgHeight, this);
            }
        };
        jarPanel.setLayout(null);
        jarPanel.setBackground(Color.WHITE);  // Set background color to white

        amountDisplay = new JTextArea();
        amountDisplay.setBounds(600, 230, 175, 400);
        amountDisplay.setEditable(false);
        amountDisplay.setLineWrap(true);
        amountDisplay.setWrapStyleWord(true);
        amountDisplay.setFont(font);  // Set font for amount display
        jarPanel.add(amountDisplay);

        add(jarPanel, BorderLayout.CENTER);

        // Load previous records when the panel is loaded
        loadEntriesForSelectedMonth(monthComboBox.getSelectedIndex());

        // Button action to add amount
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double amount = Double.parseDouble(inputField.getText());
                    if (getNumberOfEntriesForCurrentMonth() < MAX_ENTRIES) {
                        // Append the new amount to the JTextArea and save to the database
                        saveAmountToDatabase(amount);
                        inputField.setText("");  // Clear input after adding
                        loadEntriesForSelectedMonth(monthComboBox.getSelectedIndex()); // Refresh displayed entries
                    } else {
                        JOptionPane.showMessageDialog(null, "Maximum limit of " + MAX_ENTRIES + " entries reached for this month.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid amount.");
                }
            }
        });

        // Add action listener to the JComboBox for month selection
        monthComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadEntriesForSelectedMonth(monthComboBox.getSelectedIndex());
            }
        });
    }

    // Method to connect to the database
    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/finwise", "root", "My#music135");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Unable to connect to the database.");
            e.printStackTrace();
        }
    }

    // Method to save the entered amount to the database
    private void saveAmountToDatabase(double amount) {
        String insertQuery = "INSERT INTO savings (amount, added_on) VALUES (?, NOW())";  // Add timestamp
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setDouble(1, amount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error saving the amount to the database.");
            e.printStackTrace();
        }
    }

    // Method to load and display all previous savings entries for the selected month from the database
    private void loadEntriesForSelectedMonth(int monthIndex) {
        amountDisplay.setText("");  // Clear previous entries
        String selectQuery = "SELECT amount, added_on FROM savings WHERE MONTH(added_on) = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
            pstmt.setInt(1, monthIndex + 1);  // JDBC months are 1-based
            try (ResultSet rs = pstmt.executeQuery()) {
                // Display each record in the JTextArea
                while (rs.next()) {
                    double amount = rs.getDouble("amount");
                    Timestamp addedOn = rs.getTimestamp("added_on");
                    // Only display the amount
                    amountDisplay.append("Rs " + amount + "\n");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading entries for the selected month from the database.");
            e.printStackTrace();
        }
    }

    // Method to get the current number of entries for the selected month
    private int getNumberOfEntriesForCurrentMonth() {
        int monthIndex = monthComboBox.getSelectedIndex() + 1; // JDBC months are 1-based
        String countQuery = "SELECT COUNT(*) AS total FROM savings WHERE MONTH(added_on) = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(countQuery)) {
            pstmt.setInt(1, monthIndex);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error counting entries in the database.");
            e.printStackTrace();
        }
        return 0;  // Return 0 if there was an error
    }
}
