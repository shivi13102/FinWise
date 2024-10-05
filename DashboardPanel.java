import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardPanel extends JPanel {

    private DefaultTableModel tableModel; // To dynamically update the table
    private JLabel balanceLabel;
    private JLabel shoppingLabel;
    private JLabel foodAndDrinksLabel;
    private JLabel billsAndUtilitiesLabel;
    private JLabel othersLabel;
    private JTextField balanceTextField;  // Text field for entering balance
    private double bankBalance = 0.00; // New field to store the bank balance


    // Category totals
    private double balanceTotal = 0.00;
    private double shoppingTotal = 0.00;
    private double foodAndDrinksTotal = 0.00;
    private double billsAndUtilitiesTotal = 0.00;
    private double othersTotal = 0.00;

    public DashboardPanel() {
        setLayout(new BorderLayout());

        // Create dashboard panel
        JPanel dashboardPanel = createDashboardPanel();
        add(dashboardPanel, BorderLayout.NORTH);

        // Create recent transactions table panel
        JPanel transactionsPanel = createTransactionsPanel();
        add(transactionsPanel, BorderLayout.CENTER);

        // Create add expenditure panel
        JPanel addExpenditurePanel = createAddExpenditurePanel();
        add(addExpenditurePanel, BorderLayout.EAST);

        // Load previous expenses from the database and populate the table
        loadPreviousExpenses();

        // Schedule to clear the table at the end of each month
        scheduleMonthlyTableClear();

        // Load previous expenses for the current month
        String currentMonth = new SimpleDateFormat("MMMM").format(Calendar.getInstance().getTime());
        loadExpensesForMonth(currentMonth);
    }


    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setBackground(Color.WHITE);
        dashboardPanel.setLayout(new GridLayout(1, 5, 10, 10));

        // Dashboard cards
        String[] labels = {"Balance", "Shopping", "Food & Drinks", "Bills & Utilities", "Others"};
        Color[] colors = {new Color(40, 167, 69), new Color(23, 162, 184), new Color(255, 193, 7), new Color(220, 53, 69), new Color(52, 58, 64)};

        balanceLabel = createCard(dashboardPanel, labels[0], balanceTotal, colors[0]);
        shoppingLabel = createCard(dashboardPanel, labels[1], shoppingTotal, colors[1]);
        foodAndDrinksLabel = createCard(dashboardPanel, labels[2], foodAndDrinksTotal, colors[2]);
        billsAndUtilitiesLabel = createCard(dashboardPanel, labels[3], billsAndUtilitiesTotal, colors[3]);
        othersLabel = createCard(dashboardPanel, labels[4], othersTotal, colors[4]);

        return dashboardPanel;
    }

    private JLabel createCard(JPanel parent, String label, double amount, Color color) {
        JPanel card = new JPanel();
        card.setBackground(color);
        card.setLayout(new GridLayout(2, 1));
        card.setPreferredSize(new Dimension(200, 150));

        JLabel title = new JLabel(label, SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        card.add(title);

        JLabel amountLabel = new JLabel(String.format("Rs. %.2f", amount), SwingConstants.CENTER);
        amountLabel.setForeground(Color.WHITE);
        amountLabel.setFont(new Font("Arial", Font.BOLD, 32));
        card.add(amountLabel);

        parent.add(card);
        return amountLabel; // Return the label so it can be updated later
    }

    private JPanel createTransactionsPanel() {
        JPanel transactionsPanel = new JPanel();
        transactionsPanel.setBackground(Color.WHITE);
        transactionsPanel.setLayout(new BorderLayout());

        // Panel for the total balance and month selector
        JPanel balancePanel = new JPanel();
        balancePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Label for balance
        JLabel balanceLabel = new JLabel("Total Balance in the Bank Account:");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Input field for entering balance
        balanceTextField = new JTextField(10);
        balanceTextField.setFont(new Font("Arial", Font.PLAIN, 16));

        // Update balance button
        JButton updateBalanceButton = new JButton("Update Balance");

        // Label for month selection
        JLabel monthLabel = new JLabel("Select Month:");
        monthLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // JComboBox for selecting month
        JComboBox<String> monthComboBox = new JComboBox<>(new String[] {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        });
        monthComboBox.setFont(new Font("Arial", Font.PLAIN, 16));

        // Add components to the panel
        balancePanel.add(balanceLabel);
        balancePanel.add(balanceTextField);
        balancePanel.add(updateBalanceButton);
        balancePanel.add(monthLabel);
        balancePanel.add(monthComboBox);

        // Add the action listener for the update balance button
        updateBalanceButton.addActionListener(e -> {
            try {
                // Update bank balance with the input balance
                bankBalance = Double.parseDouble(balanceTextField.getText().replace(",", ".")); // Store the current balance

                // Update the total balance
                balanceTotal += bankBalance;

                // Optionally, update dashboard labels or any other UI component
                updateDashboardLabels();

                // Clear the input field after updating
                balanceTextField.setText("");

            } catch (NumberFormatException ex) {
                // Show an error dialog if the input is not a valid number
                JOptionPane.showMessageDialog(transactionsPanel, "Invalid balance format. Please enter a valid number.");
            }
        });

        // Add action listener for month selection
        monthComboBox.addActionListener(e -> {
            String selectedMonth = (String) monthComboBox.getSelectedItem();
            if (selectedMonth != null) {
                loadExpensesForMonth(selectedMonth);
            }
        });

        // Add this panel to the top of transactionsPanel
        transactionsPanel.add(balancePanel, BorderLayout.NORTH);

        // Table title
        JLabel title = new JLabel("Recent Transactions", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        transactionsPanel.add(title, BorderLayout.CENTER);

        // Column names for the transactions table
        String[] columnNames = {"Purpose", "Category", "Sum", "Date"};

        // Initially, the data is empty
        Object[][] emptyData = {};

        // Create table model with empty data
        tableModel = new DefaultTableModel(emptyData, columnNames);
        JTable table = new JTable(tableModel);

        // Increase font size for table headers
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));

        // Increase font size for table data
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.setRowHeight(30);  // Increase row height for better visibility

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        transactionsPanel.add(scrollPane, BorderLayout.CENTER);

        return transactionsPanel;
    }


    private JPanel createAddExpenditurePanel() {
        JPanel addExpenditurePanel = new JPanel();
        addExpenditurePanel.setBackground(Color.WHITE);
        addExpenditurePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.RED), "Add Expenditure"));
        addExpenditurePanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add padding around components
        gbc.fill = GridBagConstraints.HORIZONTAL; // Allow components to expand horizontally
        gbc.weightx = 1.0; // Allow components to expand horizontally

        // Purpose Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        addExpenditurePanel.add(new JLabel("Purpose"), gbc);

        gbc.gridx = 1;
        JTextField purposeField = new JTextField(20); // Increased number of columns
        addExpenditurePanel.add(purposeField, gbc);

        // Sum Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        addExpenditurePanel.add(new JLabel("Sum"), gbc);

        gbc.gridx = 1;
        JTextField sumField = new JTextField(20); // Increased number of columns
        addExpenditurePanel.add(sumField, gbc);

        // Date Field using JDatePicker
        gbc.gridx = 0;
        gbc.gridy = 2;
        addExpenditurePanel.add(new JLabel("Date"), gbc);

        gbc.gridx = 1;
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        addExpenditurePanel.add(datePicker, gbc);

        // Category ComboBox
        gbc.gridx = 0;
        gbc.gridy = 3;
        addExpenditurePanel.add(new JLabel("Category"), gbc);

        gbc.gridx = 1;
        JComboBox<String> categoryComboBox = new JComboBox<>(new String[] {"Shopping", "Food & Drinks", "Bills & Utilities", "Others"});
        addExpenditurePanel.add(categoryComboBox, gbc);

        // Submit Button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JButton submitButton = new JButton("Submit");
        addExpenditurePanel.add(submitButton, gbc);

        // Image Label with Resizing
        gbc.gridy = 5;
        gbc.gridwidth = 2; // Span across both columns

        // Load and resize image
        ImageIcon originalIcon = new ImageIcon("E:/SEMESTER SUBJECTS/3rd SEMESTER/ADVANCE OOPS/MINI PROJECT/budget_app/Photos/calculating_expense.jpg"); // Replace with your image path
        Image originalImage = originalIcon.getImage();
        Image resizedImage = originalImage.getScaledInstance(400, 400, Image.SCALE_SMOOTH); // Resize to 150x150
        ImageIcon resizedIcon = new ImageIcon(resizedImage);

        JLabel imageLabel = new JLabel(resizedIcon);
        addExpenditurePanel.add(imageLabel, gbc);

        submitButton.addActionListener(e -> {
            String purpose = purposeField.getText();
            String sumText = sumField.getText();
            String date = datePicker.getJFormattedTextField().getText(); // Get the date as a String
            String category = (String) categoryComboBox.getSelectedItem();

            try {
                double sum = Double.parseDouble(sumText.replace(",", "."));
                updateExpenditure(purpose, sum, date, category);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addExpenditurePanel, "Invalid sum format. Please enter a valid number.");
            }
        });

        return addExpenditurePanel;
    }

    private void updateExpenditure(String purpose, double sum, String date, String category) {
        // Update database
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finwise", "root", "My#music135");
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO expenditures (purpose, amount, date, category) VALUES (?, ?, ?, ?)")) {

            stmt.setString(1, purpose);
            stmt.setDouble(2, sum);
            stmt.setString(3, date);
            stmt.setString(4, category);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected); // Log the number of rows affected

            // Check if data is actually being inserted
            if (rowsAffected > 0) {
                System.out.println("Data inserted successfully.");
            } else {
                System.out.println("No data inserted.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Update the table and dashboard labels
        updateDashboard(category, sum);
        updateTable(purpose, category, sum, date);
    }



    private void updateDashboard(String category, double amount) {
        switch (category) {
            case "Shopping":
                shoppingTotal += amount;
                break;
            case "Food & Drinks":
                foodAndDrinksTotal += amount;
                break;
            case "Bills & Utilities":
                billsAndUtilitiesTotal += amount;
                break;
            case "Others":
                othersTotal += amount;
                break;
        }
        balanceTotal -= amount; // Subtract from balance total

        updateDashboardLabels();
    }

    private void updateDashboardLabels() {
        balanceLabel.setText(String.format("Rs. %.2f", balanceTotal));
        shoppingLabel.setText(String.format("Rs. %.2f", shoppingTotal));
        foodAndDrinksLabel.setText(String.format("Rs. %.2f", foodAndDrinksTotal));
        billsAndUtilitiesLabel.setText(String.format("Rs. %.2f", billsAndUtilitiesTotal));
        othersLabel.setText(String.format("Rs. %.2f", othersTotal));
    }


    private void updateTable(String purpose, String category, double sum, String date) {
        Object[] row = {purpose, category, sum, date};
        tableModel.addRow(row);
    }

    private void loadPreviousExpenses() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finwise", "root", "My#music135");
             PreparedStatement stmt = conn.prepareStatement("SELECT purpose, amount, date, category FROM expenditures")) {

            // Execute the query
            var resultSet = stmt.executeQuery();

            // Clear existing data from the table and reset totals
            tableModel.setRowCount(0);
            balanceTotal = 0.00;
            shoppingTotal = 0.00;
            foodAndDrinksTotal = 0.00;
            billsAndUtilitiesTotal = 0.00;
            othersTotal = 0.00;

            // Collect results into a list
            List<Expense> expenses = new ArrayList<>();
            while (resultSet.next()) {
                String purpose = resultSet.getString("purpose");
                double amount = resultSet.getDouble("amount");
                String date = resultSet.getString("date");
                String category = resultSet.getString("category");

                // Add expense to the list
                expenses.add(new Expense(purpose, amount, date, category));

                // Add to the correct category total
                switch (category) {
                    case "Shopping":
                        shoppingTotal += amount;
                        break;
                    case "Food & Drinks":
                        foodAndDrinksTotal += amount;
                        break;
                    case "Bills & Utilities":
                        billsAndUtilitiesTotal += amount;
                        break;
                    case "Others":
                        othersTotal += amount;
                        break;
                }
                // Add to total balance
                balanceTotal -= amount; // Assuming expenses reduce the balance
            }

            // Sort the list by date in ascending order using Comparator.comparing
            expenses.sort(Comparator.comparing(Expense::getDate));

            // Add sorted expenses to the table
            for (Expense expense : expenses) {
                Object[] row = {
                        expense.getPurpose(),
                        expense.getCategory(),
                        expense.getAmount(),
                        expense.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) // Convert LocalDate to String for display
                };
                tableModel.addRow(row);
            }

            // Update the dashboard labels with the new totals
            updateDashboardLabels();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading expenses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    // Assuming you have an Expense class like this:
    public static class Expense {
        private final String purpose;
        private final double amount;
        private final LocalDate date;
        private final String category;

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        public Expense(String purpose, double amount, String dateStr, String category) {
            this.purpose = purpose;
            this.amount = amount;
            this.date = LocalDate.parse(dateStr, formatter); // Parse the date string into LocalDate
            this.category = category;
        }

        public String getPurpose() {
            return purpose;
        }

        public double getAmount() {
            return amount;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getCategory() {
            return category;
        }
    }

    private void loadExpensesForMonth(String month) {
        String monthNumber = String.format("%02d", getMonthNumber(month));
        String query = "SELECT purpose, amount, date, category FROM expenditures WHERE DATE_FORMAT(date, '%m') = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finwise", "root", "My#music135");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, monthNumber);

            var resultSet = stmt.executeQuery();

            // Reset totals
            balanceTotal = 0.00;
            shoppingTotal = 0.00;
            foodAndDrinksTotal = 0.00;
            billsAndUtilitiesTotal = 0.00;
            othersTotal = 0.00;

            // Clear the table data
            tableModel.setRowCount(0);

            while (resultSet.next()) {
                String purpose = resultSet.getString("purpose");
                double amount = resultSet.getDouble("amount");
                String date = resultSet.getString("date");
                String category = resultSet.getString("category");

                // Add to the correct category total
                switch (category) {
                    case "Shopping":
                        shoppingTotal += amount;
                        break;
                    case "Food & Drinks":
                        foodAndDrinksTotal += amount;
                        break;
                    case "Bills & Utilities":
                        billsAndUtilitiesTotal += amount;
                        break;
                    case "Others":
                        othersTotal += amount;
                        break;
                }
                // Add to total balance
                balanceTotal -= amount;

                // Add row to the table
                Object[] row = {purpose, category, amount, date};
                tableModel.addRow(row);
            }

            // Update the dashboard labels
            updateDashboardLabels();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading expenses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private int getMonthNumber(String monthName) {
        return switch (monthName) {
            case "January" -> 1;
            case "February" -> 2;
            case "March" -> 3;
            case "April" -> 4;
            case "May" -> 5;
            case "June" -> 6;
            case "July" -> 7;
            case "August" -> 8;
            case "September" -> 9;
            case "October" -> 10;
            case "November" -> 11;
            case "December" -> 12;
            default -> throw new IllegalArgumentException("Invalid month name: " + monthName);
        };
    }



    // DateLabelFormatter class for JDatePicker
    private static class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormat.parse(text);
        }

        @Override
        public String valueToString(Object value) {
            if (value != null) {
                Calendar cal = (Calendar) value;
                return dateFormat.format(cal.getTime());
            }
            return "";
        }
    }

    private void clearTable() {
        tableModel.setRowCount(0); // This clears the table data
    }

    private void scheduleMonthlyTableClear() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                clearTable();
            }
        };

        // Schedule task to run at the start of each month
        // This is just an example; adjust the time as needed
        long delay = calculateInitialDelayForNextMonth();
        long period = 30L * 24 * 60 * 60 * 1000; // Approximately one month in milliseconds

        timer.scheduleAtFixedRate(task, delay, period);
    }

    private long calculateInitialDelayForNextMonth() {
        // Calculate the initial delay to schedule the task at the start of the next month
        // This example assumes the task should run on the first day of the month at midnight
        Calendar now = Calendar.getInstance();
        Calendar nextMonth = (Calendar) now.clone();
        nextMonth.add(Calendar.MONTH, 1);
        nextMonth.set(Calendar.DAY_OF_MONTH, 1);
        nextMonth.set(Calendar.HOUR_OF_DAY, 0);
        nextMonth.set(Calendar.MINUTE, 0);
        nextMonth.set(Calendar.SECOND, 0);
        nextMonth.set(Calendar.MILLISECOND, 0);

        return nextMonth.getTimeInMillis() - now.getTimeInMillis();
    }

}
