import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Ellipse2D;

public class Main extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel userNameLabel;
    private JLabel userIDLabel;

    public Main() {
        setTitle("FinWise");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.add(new DashboardPanel(), "Dashboard");
        contentPanel.add(createReportPanel(), "Report");
        contentPanel.add(createBillsAndDatesPanel(), "Bills & Dates");
        contentPanel.add(createPredictionPanel(), "Prediction"); // Add Prediction panel
        contentPanel.add(createSettingsPanel(), "Setting");

        add(contentPanel, BorderLayout.CENTER);

        // Set default panel to display
        cardLayout.show(contentPanel, "Dashboard");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(33, 37, 41));
        sidebar.setPreferredSize(new Dimension(250, 800));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Profile section
        ImageIcon profileIcon = new ImageIcon("E:/SEMESTER SUBJECTS/3rd SEMESTER/ADVANCE OOPS/MINI PROJECT/budget_app/Photos/user.png"); // Replace with your image path
        Image profileImage = profileIcon.getImage();

        // Resize and crop the image to a circle
        int diameter = 150;
        BufferedImage circleBuffer = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = circleBuffer.createGraphics();
        g2d.setClip(new Ellipse2D.Double(0, 0, diameter, diameter));
        g2d.drawImage(profileImage, 0, 0, diameter, diameter, null);
        g2d.dispose();

        ImageIcon circularIcon = new ImageIcon(circleBuffer);
        JLabel userProfile = new JLabel(circularIcon, SwingConstants.CENTER);
        userProfile.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(userProfile);

        // Add the name below the image
        userNameLabel = new JLabel("Name", SwingConstants.CENTER);
        userNameLabel.setForeground(Color.WHITE);
        userNameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        userNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(userNameLabel);

        userIDLabel = new JLabel("Username", SwingConstants.CENTER);
        userIDLabel.setForeground(new Color(144, 238, 144));
        userIDLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        userIDLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(userIDLabel);

        // Add a gap between the role label and menu items
        sidebar.add(Box.createVerticalStrut(20)); // 20 pixels gap

        // Sidebar menu items
        String[] menuItems = {"Dashboard", "Report", "Bills & Dates", "Prediction", "Setting"}; // Add Prediction here
        for (String item : menuItems) {
            JButton button = new JButton(item);
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(40, 45, 50));
            button.setFocusPainted(false);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setMaximumSize(new Dimension(200, 50));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.addActionListener(e -> {
                switch (item) {
                    case "Dashboard":
                        cardLayout.show(contentPanel, "Dashboard");
                        break;
                    case "Report":
                        cardLayout.show(contentPanel, "Report");
                        break;
                    case "Bills & Dates":
                        cardLayout.show(contentPanel, "Bills & Dates");
                        break;
                    case "Prediction":
                        cardLayout.show(contentPanel, "Prediction");
                        break;
                    case "Setting":
                        cardLayout.show(contentPanel, "Setting");
                        break;
                }
            });
            sidebar.add(button);
        }

        // Add a gap between menu items and the image
        sidebar.add(Box.createVerticalStrut(20)); // 20 pixels gap (adjust as needed)

        // Add the image below the menu
        ImageIcon imageIcon = new ImageIcon("E:/SEMESTER SUBJECTS/3rd SEMESTER/ADVANCE OOPS/MINI PROJECT/budget_app/Photos/Saving_money.jpg"); // Replace with your image path
        Image originalImage = imageIcon.getImage();
        Image resizedImage = originalImage.getScaledInstance(200, 320, Image.SCALE_SMOOTH); // Adjust size as needed
        JLabel imageLabel = new JLabel(new ImageIcon(resizedImage));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(imageLabel);

        return sidebar;
    }

    private JPanel createReportPanel() {
        return new ReportPanel();
    }

    private JPanel createBillsAndDatesPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.add(new JLabel("Bills & Dates Content"));
        return panel;
    }

    private JPanel createPredictionPanel() {
        return new PredictionPanel(); // Create and return PredictionPanel
    }

    private JPanel createSettingsPanel() {
        return new SettingsPanel(this);
    }

    // Methods to update the sidebar labels
    public void updateUserName(String name) {
        userNameLabel.setText(name);
    }

    public void updateUserID(String userID) {
        userIDLabel.setText(userID);
    }

    public CardLayout getCardLayout() {
        return cardLayout;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main mainFrame = new Main();
            mainFrame.setVisible(true);
        });
    }
}