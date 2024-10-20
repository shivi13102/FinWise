# FinWise

## Overview
FinWise is a personal budget tracking application built using Java Swing, designed to help users manage their finances by tracking expenses, generating reports, and setting savings goals. The application features a user-friendly interface with various panels for managing finances effectively.

## Features
- **Dashboard**: Provides an overview of user expenses and financial health.
- **Report**: Generates detailed reports on spending patterns, including monthly and yearly summaries.
- **Bills & Dates**: Notifies users of upcoming bill due dates to help avoid late fees.
- **Prediction**: Uses historical data to forecast future expenses and spending trends.
- **Savings**: Allows users to set and track savings goals.
- **Settings**: Manages user authentication and profile settings.

## Technologies Used
- Java
- Swing (for GUI)
- BufferedImage (for image processing)
- CardLayout (for panel switching)
- jDatePicker (for date selection)
- JFreeChart (for data visualization)

## Installation
1. Ensure you have Java Development Kit (JDK) installed on your machine.
2. Clone the repository or download the project files.
3. Open the project in your favorite Java IDE (e.g., IntelliJ IDEA).
4. Include the necessary libraries:
   - **jDatePicker**: Download from [jDatePicker GitHub](https://github.com/jdatepicker/jdatepicker) and add the JAR files to your project build path.
   - **JFreeChart**: Download from [JFreeChart](http://www.jfree.org/jfreechart/) and add the JAR files to your project build path.
5. Run the `Main` class to launch the application.

## Code Structure
The main components of the project are organized into various classes:

- **Main.java**: The entry point of the application that initializes the JFrame and sets up the CardLayout for panel navigation.
- **DashboardPanel.java**: Manages the dashboard interface.
- **ReportPanel.java**: Generates and displays financial reports.
- **BillsAndDatesPanel.java**: Manages bill tracking and reminders.
- **PredictionPanel.java**: Forecasts future expenses based on historical data.
- **SavingsPanel.java**: Allows users to set and monitor savings goals.
- **SettingsPanel.java**: Handles user authentication and profile management.

### Main Class Description
The `Main` class extends `JFrame` and serves as the core of the FinWise application. It includes:
- A sidebar for navigation between different panels (Dashboard, Report, Bills & Dates, Prediction, Savings, Settings).
- A CardLayout for dynamically displaying the selected panel.
- User profile management with dynamically updated user name and ID labels.
- Functionality for switching between different panels through button actions.

### Future Enhancement
- Integration of a database for persistent storage of user data and expenses.
- Implementation of advanced analytics and visualization for a better understanding of spending habits.
- Support for multiple user accounts and user-specific expense tracking.
