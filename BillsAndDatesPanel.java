import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.sql.Date;
import java.util.Calendar;

class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
    private String datePattern = "yyyy-MM-dd"; // Set your desired date format
    private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

    @Override
    public Object stringToValue(String text) throws ParseException {
        return dateFormatter.parse(text);
    }

    @Override
    public String valueToString(Object value) throws ParseException {
        if (value != null) {
            // Check if value is of type GregorianCalendar
            if (value instanceof Calendar) {
                Calendar calendar = (Calendar) value;
                return dateFormatter.format(calendar.getTime());
            } else if (value instanceof java.util.Date) {
                return dateFormatter.format((java.util.Date) value);
            }
        }
        return "";
    }
}


public class BillsAndDatesPanel extends JPanel {

    private JTable billsTable;
    private DefaultTableModel tableModel;

    public BillsAndDatesPanel(Main mainFrame) {
        setLayout(new BorderLayout());

        // Create tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Add Bill", createAddBillPanel());
        tabbedPane.addTab("Bills Overview", createBillsOverviewPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createAddBillPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Spacing around components
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Set a larger font size
        Font labelFont = new Font("Arial", Font.PLAIN, 16);
        Font textFieldFont = new Font("Arial", Font.PLAIN, 16);
        Font buttonFont = new Font("Arial", Font.BOLD, 16);

        // Components for bill entry
        JTextField billNameField = new JTextField(20);
        billNameField.setFont(textFieldFont);
        JTextField amountField = new JTextField(20);
        amountField.setFont(textFieldFont);
        JDatePickerImpl datePicker = createDatePicker();
        datePicker.getJFormattedTextField().setFont(textFieldFont);

        String[] domains = {"Utilities", "Fees", "Loans", "Insurance", "Transportation", "Subscriptions", "Rent/Mortgage", "Internet/Cable"};
        JComboBox<String> domainComboBox = new JComboBox<>(domains);
        domainComboBox.setFont(textFieldFont);

        // Buttons for bill operations
        JButton addBillButton = new JButton("Add Bill");
        JButton removeBillButton = new JButton("Remove Bill");
        JButton markAsPaidButton = new JButton("Mark as Paid");

        // Set font for buttons
        addBillButton.setFont(buttonFont);
        removeBillButton.setFont(buttonFont);
        markAsPaidButton.setFont(buttonFont);

        // Add action listeners for buttons
        addBillButton.addActionListener(e -> addBill(billNameField.getText(), amountField.getText(), datePicker.getJFormattedTextField().getText(), (String) domainComboBox.getSelectedItem()));
        removeBillButton.addActionListener(e -> removeBill(billNameField.getText()));
        markAsPaidButton.addActionListener(e -> markAsPaid(billNameField.getText()));

        // Arrange components using GridBagConstraints
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Bill Name:"), gbc);

        gbc.gridx = 1;
        panel.add(billNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Due Date:"), gbc);

        gbc.gridx = 1;
        panel.add(datePicker, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Amount (Rs.):"), gbc);

        gbc.gridx = 1;
        panel.add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Domain:"), gbc);

        gbc.gridx = 1;
        panel.add(domainComboBox, gbc);

        // Add buttons
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2; // Span both columns for buttons
        JPanel buttonPanel = new JPanel(); // For button layout
        buttonPanel.add(addBillButton);
        buttonPanel.add(removeBillButton);
        buttonPanel.add(markAsPaidButton);
        panel.add(buttonPanel, gbc);

        // Add picture placeholder (you can set an actual image here)
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        JLabel pictureLabel = new JLabel(new ImageIcon("E:/SEMESTER SUBJECTS/3rd SEMESTER/ADVANCE OOPS/MINI PROJECT/budget_app/Photos/bills.jpg")); // Update the path to your image
        panel.add(pictureLabel, gbc);

        return panel;
    }

    private JPanel createBillsOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columnNames = {"ID", "Bill Name", "Due Date", "Amount (Rs.)", "Domain", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        billsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(billsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        loadBillsIntoTable(); // Load existing bills from database
        return panel;
    }

    private JDatePickerImpl createDatePicker() {
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");

        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        return datePicker;
    }

    private void addBill(String billName, String amount, String dueDate, String domain) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO billsanddatesPanel (bill_name, due_date, amount, domain, status) VALUES (?, ?, ?, ?, ?)")) {

            statement.setString(1, billName);
            statement.setDate(2, Date.valueOf(dueDate));
            statement.setBigDecimal(3, new BigDecimal(amount));
            statement.setString(4, domain);
            statement.setString(5, "Unpaid"); // Default status
            statement.executeUpdate();

            // Refresh the table
            loadBillsIntoTable();
            JOptionPane.showMessageDialog(this, "Bill added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding bill: " + e.getMessage());
        }
    }

    private void removeBill(String billName) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM billsanddatesPanel WHERE bill_name = ?")) {

            statement.setString(1, billName);
            statement.executeUpdate();
            loadBillsIntoTable(); // Refresh the table
            JOptionPane.showMessageDialog(this, "Bill removed successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error removing bill: " + e.getMessage());
        }
    }

    private void markAsPaid(String billName) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE billsanddatesPanel SET status = 'Paid' WHERE bill_name = ?")) {

            statement.setString(1, billName);
            statement.executeUpdate();
            loadBillsIntoTable(); // Refresh the table
            JOptionPane.showMessageDialog(this, "Bill marked as paid!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error marking bill as paid: " + e.getMessage());
        }
    }

    private void loadBillsIntoTable() {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM billsanddatesPanel");
             ResultSet resultSet = statement.executeQuery()) {

            tableModel.setRowCount(0); // Clear existing rows
            while (resultSet.next()) {
                Object[] row = new Object[]{
                        resultSet.getInt("id"),
                        resultSet.getString("bill_name"),
                        resultSet.getDate("due_date"),
                        resultSet.getBigDecimal("amount"),
                        resultSet.getString("domain"),
                        resultSet.getString("status")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/finwise"; // Update with your DB URL
            Properties props = new Properties();
            props.put("user", "root"); // Update with your DB username
            props.put("password", "My#music135"); // Update with your DB password
            return DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}