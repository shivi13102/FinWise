import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class PredictionPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTable predictionTable;
    private DefaultTableModel predictionTableModel;
    private JLabel monthYearLabel;
    private ChartPanel chartPanel;
    private ChartPanel predictionChartPanel;
    private ChartPanel lineChartPanel;

    public PredictionPanel() {
        setLayout(new BorderLayout());

        // Create a label to display the current month and year
        monthYearLabel = new JLabel();
        monthYearLabel.setHorizontalAlignment(SwingConstants.CENTER);
        monthYearLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Fetch and display the current month and year in the label
        updateMonthYearLabel();

        // Initialize the main expenses table and table model
        tableModel = new DefaultTableModel(new Object[]{"Category", "Average Expense"}, 0);
        table = new JTable(tableModel);

        // Set font size for the table content and adjust row height
        table.setFont(new Font("Arial", Font.PLAIN, 16)); // Set font size for table content
        table.setRowHeight(30); // Adjust row height to accommodate larger font

        // Create scroll pane for the table and remove borders
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(300, 150)); // Set height for the table
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove border

        // Initialize the chartPanel with an empty chart
        chartPanel = new ChartPanel(createBarChart(new DefaultCategoryDataset())); // Pass an empty dataset initially
        chartPanel.setPreferredSize(new Dimension(300, 300)); // Set height for the chart
        chartPanel.setBorder(BorderFactory.createEmptyBorder()); // Remove border

        // Initialize the prediction table and table model
        predictionTableModel = new DefaultTableModel(new Object[]{"Category", "Predicted Avg Expense"}, 0);
        predictionTable = new JTable(predictionTableModel);
        predictionTable.setFont(new Font("Arial", Font.PLAIN, 16));
        predictionTable.setRowHeight(30);

        // Create scroll pane for the prediction table and remove borders
        JScrollPane predictionTableScrollPane = new JScrollPane(predictionTable);
        predictionTableScrollPane.setPreferredSize(new Dimension(300, 150)); // Set height for the prediction table
        predictionTableScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove border

        // Initialize the prediction chart panel with an empty chart
        predictionChartPanel = new ChartPanel(createPredictionBarChart(new DefaultCategoryDataset())); // Pass an empty dataset initially
        predictionChartPanel.setPreferredSize(new Dimension(300, 300)); // Set height for the prediction chart
        predictionChartPanel.setBorder(BorderFactory.createEmptyBorder()); // Remove border

        // Create a panel for the prediction table and chart
        JPanel predictionPanel = new JPanel();
        predictionPanel.setLayout(new BoxLayout(predictionPanel, BoxLayout.Y_AXIS));
        JLabel predictionLabel = new JLabel(getNextMonthLabel());
        predictionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        predictionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        predictionPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Add space before the prediction label
        predictionPanel.add(predictionLabel);
        predictionPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between label and table
        predictionPanel.add(predictionTableScrollPane);
        predictionPanel.add(predictionChartPanel); // Add the prediction chart panel

        // Create a panel for the table and chart
        JPanel tableChartPanel = new JPanel();
        tableChartPanel.setLayout(new BoxLayout(tableChartPanel, BoxLayout.Y_AXIS)); // Stack components vertically

        // Add the table scroll pane and chart panel to the new panel
        tableChartPanel.add(tableScrollPane);
        tableChartPanel.add(chartPanel);

        // Create a scroll pane for the entire content
        JScrollPane mainScrollPane = new JScrollPane();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(monthYearLabel);
        mainPanel.add(tableChartPanel);
        mainPanel.add(predictionPanel);
        mainScrollPane.setViewportView(mainPanel);

        // Add the scroll pane to the PredictionPanel
        add(mainScrollPane, BorderLayout.CENTER);

        // Initialize the line chart panel with an empty chart
        lineChartPanel = new ChartPanel(createLineChart(new DefaultCategoryDataset())); // Pass an empty dataset initially
        lineChartPanel.setPreferredSize(new Dimension(300, 300)); // Set height for the line chart
        lineChartPanel.setBorder(BorderFactory.createEmptyBorder()); // Remove border

        predictionPanel.add(predictionChartPanel); // Add the prediction chart panel
        predictionPanel.add(lineChartPanel); // Add the line chart panel


        // Fetch data and populate the main expense table
        try {
            populateTable();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMonthYearLabel() {
        // Get the current month and year
        LocalDate currentDate = LocalDate.now();
        String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("MMMM, yyyy"));

        // Set the label text to display "Month, Year"
        monthYearLabel.setText(formattedDate);
    }

    private String getNextMonthLabel() {
        // Get the next month
        LocalDate nextMonthDate = LocalDate.now().plusMonths(1);

        // Create a DateTimeFormatter for the desired format
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM");
        DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");

        // Format the next month and current year
        String nextMonth = nextMonthDate.format(monthFormatter);
        String currentYear = nextMonthDate.format(yearFormatter);

        return nextMonth + ", " + currentYear + " Prediction";
    }

    private void populateTable() throws SQLException {
        // Get the current year and month
        LocalDate currentDate = LocalDate.now();
        String currentYear = String.valueOf(currentDate.getYear());
        String currentMonth = String.format("%02d", currentDate.getMonthValue());

        // Fetch total expenses and count for the current month
        Map<String, ExpenseDataFetcher.CategoryExpense> monthlyExpenses = ExpenseDataFetcher.getMonthlyExpensesWithCount(currentYear, currentMonth);

        // List of categories to display
        String[] categories = {"Food & Drinks", "Shopping", "Bills & Utilities", "Others"};

        // Clear the existing table data
        tableModel.setRowCount(0);

        // Populate the table with only the specified categories
        for (String category : categories) {
            // Fetch data for the category, defaulting to 0 if not found
            ExpenseDataFetcher.CategoryExpense expense = monthlyExpenses.getOrDefault(category, new ExpenseDataFetcher.CategoryExpense(0.0, 0));

            // Calculate the average expense
            double average = expense.getAverage();

            // Add data to the table (average for the current month)
            tableModel.addRow(new Object[]{category, String.format("%.2f", average)});
        }

        // After populating the table, update the bar chart and prediction table
        updateBarChart(monthlyExpenses);
        updatePredictionTable(monthlyExpenses);
    }

    private JFreeChart createBarChart(DefaultCategoryDataset dataset) {
        // Create the chart with the provided dataset
        JFreeChart chart = ChartFactory.createBarChart(
                "Current Month Average Expenses",   // Chart title
                "Category",                        // X-axis label
                "Average Expense",                 // Y-axis label
                dataset,                           // Dataset
                PlotOrientation.VERTICAL,          // Plot orientation
                false,                             // Include legend
                true,                              // Tooltips
                false                              // URLs
        );

        // Change the color of the bars to light blue
        chart.getCategoryPlot().getRenderer().setSeriesPaint(0, new Color(34, 82, 179)); // Light blue color

        return chart;
    }

    private void updateBarChart(Map<String, ExpenseDataFetcher.CategoryExpense> monthlyExpenses) {
        // Create a dataset for the chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // List of categories to display
        String[] categories = {"Food & Drinks", "Shopping", "Bills & Utilities", "Others"};

        // Populate the dataset with average expenses for each category
        for (String category : categories) {
            ExpenseDataFetcher.CategoryExpense expense = monthlyExpenses.getOrDefault(category, new ExpenseDataFetcher.CategoryExpense(0.0, 0));
            double average = expense.getAverage();
            dataset.addValue(average, "Average", category);
        }

        // Update the chart's dataset by creating a new chart with updated data
        JFreeChart barChart = createBarChart(dataset);

        // Refresh the chart panel with the new chart
        chartPanel.setChart(barChart);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    // Update the updatePredictionTable method to refresh the prediction chart
    private void updatePredictionTable(Map<String, ExpenseDataFetcher.CategoryExpense> monthlyExpenses) {
        // Clear the existing prediction table data
        predictionTableModel.setRowCount(0);

        // List of categories to display and their percentage increases
        String[] categories = {"Food & Drinks", "Shopping", "Bills & Utilities", "Others"};
        double[] increases = {1.1, 1.08, 1.05, 1.03}; // Increase percentages for each category

        // Populate the prediction table with the predicted average expenses for the next month
        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            ExpenseDataFetcher.CategoryExpense expense = monthlyExpenses.getOrDefault(category, new ExpenseDataFetcher.CategoryExpense(0.0, 0));
            double average = expense.getAverage();
            double predictedAverage = average * increases[i];

            // Add data to the prediction table
            predictionTableModel.addRow(new Object[]{category, String.format("%.2f", predictedAverage)});
        }

        // After populating the prediction table, update the prediction bar chart and line chart
        updatePredictionBarChart();
        updateLineChart(monthlyExpenses);
    }


    private JFreeChart createPredictionBarChart(DefaultCategoryDataset dataset) {
        // Create the prediction chart with the provided dataset
        JFreeChart chart = ChartFactory.createBarChart(
                "Next Month Prediction",            // Chart title
                "Category",                        // X-axis label
                "Predicted Avg Expense",           // Y-axis label
                dataset,                           // Dataset
                PlotOrientation.VERTICAL,          // Plot orientation
                false,                             // Include legend
                true,                              // Tooltips
                false                              // URLs
        );

        // Change the color of the bars to light green
        chart.getCategoryPlot().getRenderer().setSeriesPaint(0, new Color(34, 179, 82)); // Light green color

        return chart;
    }

    private void updatePredictionBarChart() {
        // Create a dataset for the prediction chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // List of categories to display
        String[] categories = {"Food & Drinks", "Shopping", "Bills & Utilities", "Others"};

        // Populate the dataset with predicted averages for each category
        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            double predictedAverage = Double.parseDouble(predictionTableModel.getValueAt(i, 1).toString());
            dataset.addValue(predictedAverage, "Predicted", category);
        }

        // Update the prediction chart's dataset by creating a new chart with updated data
        JFreeChart predictionChart = createPredictionBarChart(dataset);

        // Refresh the prediction chart panel with the new chart
        predictionChartPanel.setChart(predictionChart);
        predictionChartPanel.revalidate();
        predictionChartPanel.repaint();
    }

    private JFreeChart createLineChart(DefaultCategoryDataset dataset) {
        // Create the line chart with the provided dataset
        JFreeChart lineChart = ChartFactory.createLineChart(
                "Monthly Expense Comparison",    // Chart title
                "Category",                      // X-axis label
                "Expense",                       // Y-axis label
                dataset,                         // Dataset
                PlotOrientation.VERTICAL,        // Plot orientation
                true,                            // Include legend
                true,                            // Tooltips
                false                            // URLs
        );

        // Customize the line colors
        lineChart.getCategoryPlot().getRenderer().setSeriesPaint(0, new Color(34, 82, 179)); // Current month (light blue)
        lineChart.getCategoryPlot().getRenderer().setSeriesPaint(1, new Color(34, 179, 82)); // Next month (light green)

        return lineChart;
    }

    private void updateLineChart(Map<String, ExpenseDataFetcher.CategoryExpense> monthlyExpenses) {
        // Create a dataset for the line chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // List of categories to display
        String[] categories = {"Food & Drinks", "Shopping", "Bills & Utilities", "Others"};

        // Populate the dataset with average expenses for the current month
        for (String category : categories) {
            ExpenseDataFetcher.CategoryExpense expense = monthlyExpenses.getOrDefault(category, new ExpenseDataFetcher.CategoryExpense(0.0, 0));
            double average = expense.getAverage();
            dataset.addValue(average, "Current Month", category);
        }

        // Populate the dataset with predicted averages for the next month
        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            double predictedAverage = Double.parseDouble(predictionTableModel.getValueAt(i, 1).toString());
            dataset.addValue(predictedAverage, "Next Month", category);
        }

        // Update the line chart's dataset by creating a new chart with updated data
        JFreeChart lineChart = createLineChart(dataset);

        // Refresh the line chart panel with the new chart
        lineChartPanel.setChart(lineChart);
        lineChartPanel.revalidate();
        lineChartPanel.repaint();
    }

}
