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
        contentPanel.add(new BillsAndDatesPanel(this), "Bills & Dates");
        contentPanel.add(createPredictionPanel(), "Prediction");
        contentPanel.add(createSavingsPanel(), "Savings"); // Add Savings Panel
        contentPanel.add(createSettingsPanel(), "Setting");

        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "Setting");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(33, 37, 41));
        sidebar.setPreferredSize(new Dimension(250, 800));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        ImageIcon profileIcon = new ImageIcon("E:/SEMESTER SUBJECTS/3rd SEMESTER/ADVANCE OOPS/MINI PROJECT/budget_app/Photos/user.png");
        Image profileImage = profileIcon.getImage();

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

        sidebar.add(Box.createVerticalStrut(20));

        String[] menuItems = {"Dashboard", "Report", "Bills & Dates", "Prediction", "Savings", "Setting"};
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
                    case "Savings":
                        cardLayout.show(contentPanel, "Savings"); // Switch to Savings panel
                        break;
                    case "Setting":
                        cardLayout.show(contentPanel, "Setting");
                        break;
                }
            });
            sidebar.add(button);
        }

        sidebar.add(Box.createVerticalStrut(20));

        ImageIcon imageIcon = new ImageIcon("E:/SEMESTER SUBJECTS/3rd SEMESTER/ADVANCE OOPS/MINI PROJECT/budget_app/Photos/Saving_money.jpg");
        Image originalImage = imageIcon.getImage();
        Image resizedImage = originalImage.getScaledInstance(200, 320, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(resizedImage));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(imageLabel);

        return sidebar;
    }

    private JPanel createReportPanel() {
        return new ReportPanel();
    }

    private JPanel createPredictionPanel() {
        return new PredictionPanel();
    }

    private JPanel createSavingsPanel() {
        return new SavingsPanel(); // Connect the Savings panel
    }

    private JPanel createSettingsPanel() {
        return new SettingsPanel(this);
    }

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
