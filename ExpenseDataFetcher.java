import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ExpenseDataFetcher {

    public static Map<String, Double> getMonthlyExpenses(String year, String month) throws SQLException {
        Map<String, Double> expenses = new HashMap<>();
        String query = "SELECT category, SUM(amount) AS total " +
                "FROM expenditures WHERE YEAR(date) = ? AND MONTH(date) = ? GROUP BY category";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, year);
            statement.setString(2, month);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String category = resultSet.getString("category");
                double total = resultSet.getDouble("total");
                expenses.put(category, total);
            }
        }

        return expenses;
    }

    public static Map<String, Double> getYearlyExpenses(String year) throws SQLException {
        Map<String, Double> expenses = new HashMap<>();
        String query = "SELECT category, SUM(amount) AS total " +
                "FROM expenditures WHERE YEAR(date) = ? GROUP BY category";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, year);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String category = resultSet.getString("category");
                double total = resultSet.getDouble("total");
                expenses.put(category, total);
            }
        }

        return expenses;
    }

    public static Map<String, Double> getYearlyMonthlyExpenses(String year) throws SQLException {
        Map<String, Double> expenses = new HashMap<>();
        String query = "SELECT MONTH(date) AS month, SUM(amount) AS total " +
                "FROM expenditures WHERE YEAR(date) = ? GROUP BY MONTH(date)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, year);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String month = resultSet.getString("month");
                double total = resultSet.getDouble("total");
                // Convert month number to month name
                String monthName = new java.text.DateFormatSymbols().getMonths()[Integer.parseInt(month) - 1];
                expenses.put(monthName, total);
            }
        }

        return expenses;
    }

    public static Map<String, Double> getDailyExpenses(String year, String month) throws SQLException {
        Map<String, Double> expenses = new HashMap<>();
        String query = "SELECT DAY(date) AS day, SUM(amount) AS total " +
                "FROM expenditures WHERE YEAR(date) = ? AND MONTH(date) = ? " +
                "GROUP BY DAY(date)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, year);
            statement.setString(2, month);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String day = resultSet.getString("day");
                double total = resultSet.getDouble("total");
                expenses.put(day, total);
            }
        }

        return expenses;
    }
}