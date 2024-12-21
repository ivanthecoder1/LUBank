import java.sql.Connection;
import java.sql.DriverManager;       
import java.sql.SQLException;
import java.util.Scanner;

public class LUBank {
    public static void main(String[] args) {
        Scanner myScanner = new Scanner(System.in);

        String dbURL = "jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241";
        Connection conn = null;

        while (conn == null) {
            // Get Oracle credentials
            System.out.print("Enter Oracle user id: ");
            String oracleUser = myScanner.nextLine();

            System.out.print("Enter Oracle password: ");
            String oraclePass = myScanner.nextLine();

            // Attempt to establish a connection
            try {
                conn = DriverManager.getConnection(dbURL, oracleUser, oraclePass);
                System.out.println("Welcome to LUBank, " + oracleUser);
                // Call the menu method to interact with the user
            } catch (SQLException sqle) {
                System.out.println("Invalid credentials, please try again.");
            }
        }

        // Call the menu method to interact with the user
        mainMenu(myScanner, conn);

        // Close the connection
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("LUBank application closed. Goodbye!");
            }
        } catch (SQLException e) {
            System.out.println("Error with clsoing LUBank application.");
        }

        // Close the scanner and exit
        myScanner.close();
        System.exit(0);
    }

    // Print out menu options
    public static void mainMenu(Scanner scanner, Connection conn) {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n===== LUBank - Main Menu =====");
            System.out.println("1. Bank Management");
            System.out.println("2. Loan Management");
            System.out.println("3. Credit Card Management");
            System.out.println("4. Vendor Management");
            System.out.println("5. Exit");
            System.out.print("Input an option (1-5): ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    BankMenu.bankManagementMenu(scanner, conn);
                    break;
                case "2":
                    LoanMenu.loanManagementMenu(scanner, conn);
                    break;
                case "3":
                    CreditMenu.creditCardManagementMenu(scanner, conn);
                    break;
                case "4":
                    VendorMenu.vendorMenu(scanner, conn);
                    break;
                case "5":
                    exit = true;
                    System.out.println("Exiting LUBank application...");
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 5.");
            }
        }
    }
}
