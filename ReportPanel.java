import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class ReportPanel extends JPanel {

    private JComboBox<String> periodComboBox;
    private JComboBox<String> monthComboBox;
    private JComboBox<String> yearComboBox;
    private JPanel chartPanel;
    private JScrollPane scrollPane;

    public ReportPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Expense Summary", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // Time period selection
        periodComboBox = new JComboBox<>(new String[]{"Monthly", "Yearly"});
        yearComboBox = new JComboBox<>(new String[]{"2023", "2024"}); // Add more years as needed
        monthComboBox = new JComboBox<>(new String[]{"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"});

        JPanel selectionPanel = new JPanel();
        selectionPanel.add(new JLabel("Select Period:"));
        selectionPanel.add(periodComboBox);
        selectionPanel.add(new JLabel("Select Year:"));
        selectionPanel.add(yearComboBox);
        selectionPanel.add(new JLabel("Select Month:"));
        selectionPanel.add(monthComboBox);

        add(selectionPanel, BorderLayout.SOUTH);

        // Charts panel with scroll
        chartPanel = new JPanel();
        chartPanel.setLayout(new GridLayout(3, 1)); // Layout for 3 charts
        scrollPane = new JScrollPane(chartPanel);
        add(scrollPane, BorderLayout.CENTER);

        periodComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateReport();
            }
        });

        yearComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateReport();
            }
        });

        monthComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateReport();
            }
        });

        updateReport();
    }

    private void updateReport() {
        chartPanel.removeAll();
        String selectedPeriod = (String) periodComboBox.getSelectedItem();
        String selectedYear = (String) yearComboBox.getSelectedItem();
        String selectedMonth = String.format("%02d", monthComboBox.getSelectedIndex() + 1); // Convert month name to number

        try {
            if ("Monthly".equals(selectedPeriod)) {
                // Monthly Report
                Map<String, Double> monthlyExpenses = ExpenseDataFetcher.getMonthlyExpenses(selectedYear, selectedMonth);
                chartPanel.add(createPieChart("Monthly Expenses for " + selectedMonth + " " + selectedYear, monthlyExpenses));
                chartPanel.add(createBarChart("Monthly Expenses for " + selectedMonth + " " + selectedYear, monthlyExpenses));

                // Add line chart for daily expenses
                Map<String, Double> dailyExpenses = ExpenseDataFetcher.getDailyExpenses(selectedYear, selectedMonth);
                chartPanel.add(createLineChart("Daily Expenses for " + selectedMonth + " " + selectedYear, dailyExpenses));
            } else {
                // Yearly Report
                Map<String, Double> yearlyExpenses = ExpenseDataFetcher.getYearlyExpenses(selectedYear);
                chartPanel.add(createPieChart("Yearly Expenses for " + selectedYear, yearlyExpenses));
                chartPanel.add(createBarChart("Yearly Expenses for " + selectedYear, yearlyExpenses));

                // Yearly monthly expenses bar chart
                Map<String, Double> monthlyYearlyExpenses = ExpenseDataFetcher.getYearlyMonthlyExpenses(selectedYear);
                chartPanel.add(createOrderedBarChart("Monthly Expenses for " + selectedYear, monthlyYearlyExpenses));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        revalidate();
        repaint();
    }

    private ChartPanel createPieChart(String title, Map<String, Double> data) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                true,
                true,
                false
        );

        return new ChartPanel(chart);
    }

    private ChartPanel createBarChart(String title, Map<String, Double> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), "Expenses", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Category",
                "Amount",
                dataset
        );

        return new ChartPanel(chart);
    }

    private ChartPanel createOrderedBarChart(String title, Map<String, Double> data) {
        // Convert month names to an ordered list
        List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (String month : months) {
            Double total = data.getOrDefault(month, 0.0);
            dataset.addValue(total, "Expenses", month);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Month",
                "Amount",
                dataset
        );

        return new ChartPanel(chart);
    }

    private ChartPanel createLineChart(String title, Map<String, Double> data) {
        XYSeries series = new XYSeries("Daily Expenses");
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            int day = Integer.parseInt(entry.getKey());
            series.add(day, entry.getValue());
        }

        XYDataset dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "Day",
                "Amount",
                dataset
        );

        return new ChartPanel(chart);
    }
}