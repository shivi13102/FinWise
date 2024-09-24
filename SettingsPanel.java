import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SettingsPanel extends JPanel {

    private Main mainFrame;

    public SettingsPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        // Load and resize the image
        ImageIcon originalIcon = new ImageIcon("E:/SEMESTER SUBJECTS/3rd SEMESTER/ADVANCE OOPS/MINI PROJECT/budget_app/Photos/Settings.jpg"); // Replace with your image path
        Image originalImage = originalIcon.getImage();
        Image resizedImage = originalImage.getScaledInstance(1300, 640, Image.SCALE_SMOOTH); // Adjust size as needed
        ImageIcon resizedIcon = new ImageIcon(resizedImage);

        // Create a panel for the image
        JPanel imagePanel = new JPanel();
        imagePanel.add(new JLabel(resizedIcon));
        add(imagePanel, BorderLayout.NORTH);

        // Create a panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 8));

        // Create buttons
        JButton signUpButton = new JButton("Sign Up");
        JButton logInButton = new JButton("Log In");
        JButton logOutButton = new JButton("Log Out");

        // Add buttons to the button panel
        buttonPanel.add(signUpButton);
        buttonPanel.add(logInButton);
        buttonPanel.add(logOutButton);

        // Add button panel to the settings panel
        add(buttonPanel, BorderLayout.CENTER);

        // Add action listeners for buttons
        signUpButton.addActionListener(e -> handleSignUp());
        logInButton.addActionListener(e -> handleLogIn());
        logOutButton.addActionListener(e -> handleLogOut());
    }

    private void handleSignUp() {
        // Collect user details
        String name = JOptionPane.showInputDialog(SettingsPanel.this, "Enter your name:");
        String username = JOptionPane.showInputDialog(SettingsPanel.this, "Enter your username:");

        // Create a password field to get masked password input
        JPasswordField passwordField = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(SettingsPanel.this, passwordField, "Enter your password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());

            if (name != null && username != null && password != null && !password.isEmpty()) {
                try {
                    // Check if user already exists
                    Connection conn = connect();
                    String checkQuery = "SELECT * FROM user WHERE username = ?";
                    PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                    checkStmt.setString(1, username);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        JOptionPane.showMessageDialog(SettingsPanel.this, "User already has an account. Please login.");
                    } else {
                        // Insert new user
                        String insertQuery = "INSERT INTO user (full_name, username, password) VALUES (?, ?, ?)";
                        PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                        insertStmt.setString(1, name);
                        insertStmt.setString(2, username);
                        insertStmt.setString(3, password); // Consider hashing the password for security
                        insertStmt.executeUpdate();
                        JOptionPane.showMessageDialog(SettingsPanel.this, "Sign Up successful!");
                    }

                    conn.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(SettingsPanel.this, "An error occurred. Please try again.");
                }
            } else {
                JOptionPane.showMessageDialog(SettingsPanel.this, "All fields are required.");
            }
        }
    }

    private void handleLogIn() {
        // Collect login details
        String username = JOptionPane.showInputDialog(SettingsPanel.this, "Enter your username:");

        // Create a password field to get masked password input
        JPasswordField passwordField = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(SettingsPanel.this, passwordField, "Enter your password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());

            if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
                try {
                    // Check user credentials
                    Connection conn = connect();
                    String loginQuery = "SELECT * FROM user WHERE username = ? AND password = ?";
                    PreparedStatement loginStmt = conn.prepareStatement(loginQuery);
                    loginStmt.setString(1, username);
                    loginStmt.setString(2, password);
                    ResultSet rs = loginStmt.executeQuery();

                    if (rs.next()) {
                        JOptionPane.showMessageDialog(SettingsPanel.this, "You are logged in!");
                        // Update the sidebar with user details
                        String name = rs.getString("full_name");
                        String userID = rs.getString("username"); // or other relevant field
                        mainFrame.updateUserName(name);
                        mainFrame.updateUserID(userID);
                        // Redirect to the dashboard
                        mainFrame.getCardLayout().show(mainFrame.getContentPanel(), "Dashboard");
                    } else {
                        JOptionPane.showMessageDialog(SettingsPanel.this, "You do not have an account. Please sign up.");
                    }

                    conn.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(SettingsPanel.this, "An error occurred. Please try again.");
                }
            } else {
                JOptionPane.showMessageDialog(SettingsPanel.this, "Both fields are required.");
            }
        }
    }

    private void handleLogOut() {
        // Switch to the initial panel
        mainFrame.getCardLayout().show(mainFrame.getContentPanel(), "Dashboard"); // Change this to the desired initial panel if needed
        mainFrame.updateUserName("Name"); // Reset to default values if needed
        mainFrame.updateUserID("Username");
    }

    private Connection connect() throws Exception {
        // Database connection details
        String url = "jdbc:mysql://localhost:3306/finwise";
        String user = "root"; // Replace with your database username
        String password = "My#music135"; // Replace with your database password
        return DriverManager.getConnection(url, user, password);
    }
}