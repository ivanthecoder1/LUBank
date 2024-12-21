import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class LoanMenu {
    // Loans Management Menu
    public static void loanManagementMenu(Scanner scanner, Connection conn) {
        boolean back = false;
        String customerId = HelperFunctions.customerVerification(scanner, conn);
        while (!back) {
            System.out.println("\n===== Loan Management Menu =====");
            System.out.println("1. View all loans");
            System.out.println("2. View sum of all loan");
            System.out.println("3. Pay a loan");
            System.out.println("4. Take out a new loan");
            System.out.println("5. View past transactions");
            System.out.println("6. Back to Main Menu");
            System.out.print("Input an option (1-6): ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    viewAllLoans(conn, customerId);
                    break;
                case "2":
                    viewTotalLoans(conn, customerId);
                    break;
                case "3":
                    payLoan(scanner, conn, customerId);
                    break;
                case "4":
                    takeOutNewLoan(scanner, conn, customerId);
                    break;
                case "5":
                    viewPastLoanTransactions(scanner, conn, customerId);
                    break;
                case "6":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 6.");
            }
        }
    }

    // View total sum of all loans for a customer
    public static void viewTotalLoans(Connection conn, String customerId) {
        String totalLoansQuery = "SELECT SUM(amount) AS total_loans FROM loan WHERE customer_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(totalLoansQuery)) {
            pstmt.setString(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double totalLoans = rs.getDouble("total_loans");
                System.out.printf("Your total loan amount is: $%.2f%n", totalLoans);
            } else {
                System.out.println("You have no loans.");
            }

            rs.close();
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving the total loan amount.");
            
        }
    }
    
    // View information of loans for a customerID
    public static void viewAllLoans(Connection conn, String customerId) {
        // SQL query to retrieve loan information for a customer
        String query = "SELECT loan_id, amount, interest_rate, monthly_payment " +
                    "FROM loan WHERE customer_id = ? AND amount > 0";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            // Check if there are any loans for the customer
            if (!rs.isBeforeFirst()) {
                System.out.printf("No loans found for customer ID: %s%n", customerId);
                return;
            }

            // Display loan information header
            System.out.printf("Loan Information for Customer ID: %s%n", customerId);
            System.out.printf("%-10s %-15s %-15s %-15s%n", "Loan ID", "Amount", "Interest Rate", "Monthly Payment");

            // Iterate through the result set and print loan details
            while (rs.next()) {
                String loanId = rs.getString("loan_id");
                double amount = rs.getDouble("amount");
                double interestRate = rs.getDouble("interest_rate");
                double monthlyPayment = rs.getDouble("monthly_payment");

                System.out.printf("%-10s %-15.2f %-15.2f %-15.2f%n", loanId, amount, interestRate, monthlyPayment);
            }

            rs.close();
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving loan information.");
            
        }
    }

    // Record a transaction for loan payments
    public static void recordLoanTransaction(Connection conn, String loanId, double paymentAmount) {
        String insertLoanTransactionQuery = "INSERT INTO loan_transaction (loan_id, amount, trans_date) VALUES (?, ?, SYSDATE)";
        try (PreparedStatement loanTransStmt = conn.prepareStatement(insertLoanTransactionQuery)) {
            loanTransStmt.setString(1, loanId); 
            loanTransStmt.setDouble(2, paymentAmount); 
            loanTransStmt.executeUpdate();
            System.out.printf("Loan transaction recorded successfully for Loan ID: %s.%n", loanId);
        } catch (SQLException e) {
            System.out.println("An error occurred while recording the loan transaction.");
            
        }
    }

    // Take out a loan for a customerId
    public static void takeOutNewLoan(Scanner scanner, Connection conn, String customerId) {
        double loanAmount = 0;
        boolean validAmount = false;

        // Step 1: Prompt the user for a loan amount
        while (!validAmount) {
            System.out.print("Enter the loan amount you want to take out (between $500 and $100,000): ");
            String input = scanner.nextLine();

            try {
                loanAmount = Double.parseDouble(input);

                if (loanAmount <= 0) {
                    System.out.println("Loan amount must be greater than 0. Please try again.");
                } else if (loanAmount < 500) {
                    System.out.println("Loan amount cannot be less than $500. Please try again.");
                } else if (loanAmount > 100000) {
                    System.out.println("Loan amount cannot exceed $100,000. Please try again.");
                } else {
                    validAmount = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric value.");
            }
        }

        // Step 2: Generate a random interest rate (e.g., 3% to 15%)
        double interestRate = Math.round((Math.random() * (15 - 3) + 3) * 100.0) / 100.0; // Random interest rate between 3% and 15%

        // Display the loan details and ask for confirmation
        System.out.printf("Loan Details:%n");
        System.out.printf("Amount: $%.2f%n", loanAmount);
        System.out.printf("Interest Rate: %.2f%%%n", interestRate);
        System.out.print("Do you accept these terms? (yes/no): ");
        String userResponse = scanner.nextLine().trim().toLowerCase();

        if (!userResponse.equals("yes")) {
            System.out.println("Loan application canceled.");
            return;
        }

        // Step 3: Insert the loan into the database
        String insertLoanQuery = "INSERT INTO loan (customer_id, amount, interest_rate, monthly_payment) " +
                                "VALUES (?, ?, ?, ?)";

        // Calculate monthly payment over a 5-year period
        int loanTermMonths = 60; // 5 years
        double monthlyInterestRate = interestRate / 100 / 12;
        double monthlyPayment = loanAmount * monthlyInterestRate /
                                (1 - Math.pow(1 + monthlyInterestRate, -loanTermMonths));

        try (PreparedStatement pstmt = conn.prepareStatement(insertLoanQuery)) {
            pstmt.setString(1, customerId);   
            pstmt.setDouble(2, loanAmount);   
            pstmt.setDouble(3, interestRate); 
            pstmt.setDouble(4, monthlyPayment); 
            pstmt.executeUpdate();

            System.out.printf("Loan successfully created for Customer ID: %s%n", customerId);
            System.out.printf("Amount: $%.2f%n", loanAmount);
            System.out.printf("Interest Rate: %.2f%%%n", interestRate);
            System.out.printf("Monthly Payment: $%.2f%n", monthlyPayment);
        } catch (SQLException e) {
            System.out.println("An error occurred while creating the loan.");
            
        }
    }

    // Pay off a loan for a customerId
    public static void payLoan(Scanner scanner, Connection conn, String customerId) {
        try {
            // Step 1: Display available loans with an amount > 0
            System.out.println("Loans available for Customer ID: " + customerId);
            String loanQuery = "SELECT loan_id, amount, monthly_payment " +
                            "FROM loan WHERE customer_id = ? AND amount > 0";
            String loanId = null;
            double loanAmount = 0;
            boolean validLoan = false;

            try (PreparedStatement loanStmt = conn.prepareStatement(loanQuery)) {
                loanStmt.setString(1, customerId);
                ResultSet loanRs = loanStmt.executeQuery();

                if (!loanRs.isBeforeFirst()) {
                    System.out.println("No outstanding loans available to pay off.");
                    return;
                }

                System.out.printf("%-10s %-15s %-15s%n", "Loan ID", "Loan Amount", "Monthly Payment");
                while (loanRs.next()) {
                    System.out.printf("%-10s %-15.2f %-15.2f%n",
                            loanRs.getString("loan_id"),
                            loanRs.getDouble("amount"),
                            loanRs.getDouble("monthly_payment"));
                }

                System.out.print("Enter the Loan ID you want to pay off (LN000001): ");
                loanId = scanner.nextLine();

                loanRs = loanStmt.executeQuery(); // Re-execute to validate loan ID
                while (loanRs.next()) {
                    if (loanRs.getString("loan_id").equals(loanId)) {
                        validLoan = true;
                        loanAmount = loanRs.getDouble("amount");
                        break;
                    }
                }

                if (!validLoan) {
                    System.out.println("Invalid Loan ID or Loan does not belong to you or loan is already paid off. Returning to menu.");
                    return;
                }
            }

            // Step 2: Validate debit card details
            System.out.print("Enter the Account ID to use for payment (AC000001): ");
            String accountId = scanner.nextLine();
            System.out.print("Enter the Debit Card Number (4742239148670084): ");
            String cardNumber = scanner.nextLine();
            System.out.print("Enter the Security Code (123): ");
            String secCode = scanner.nextLine();

            if (!HelperFunctions.validateDebitCard(conn, accountId, cardNumber, secCode)) {
                System.out.println("Invalid debit card details. Payment cannot proceed.");
                return;
            }

            // Step 3: Validate sufficient funds and payment amount
            System.out.printf("Loan Amount: $%.2f%n", loanAmount);
            System.out.print("Enter the amount you want to pay: ");
            double paymentAmount = Double.parseDouble(scanner.nextLine());

            if (paymentAmount <= 0 || paymentAmount > loanAmount) {
                System.out.println("Invalid payment amount. Payment canceled.");
                return;
            }

            if (!HelperFunctions.hasSufficientBalance(conn, accountId, paymentAmount)) {
                System.out.println("Insufficient funds in the associated checking account. Payment cannot proceed.");
                return;
            }

            // Step 4: Use `debitCardPayment` to deduct funds and record the transaction
            HelperFunctions.debitCardPayment(conn, accountId, cardNumber, paymentAmount);

            // Step 5: Process the loan payment
            conn.setAutoCommit(false); // Begin transaction
            try {
                if (paymentAmount == loanAmount) {
                    // Full payment: Mark the loan as "paid"
                    String updateLoanQuery = "UPDATE loan SET amount = 0, monthly_payment = 0 WHERE loan_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateLoanQuery)) {
                        updateStmt.setString(1, loanId);
                        updateStmt.executeUpdate();
                        System.out.printf("Loan %s has been fully paid off.%n", loanId);
                    }
                } else {
                    // Partial payment: Update the loan balance
                    String updateLoanQuery = "UPDATE loan SET amount = amount - ? WHERE loan_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateLoanQuery)) {
                        updateStmt.setDouble(1, paymentAmount);
                        updateStmt.setString(2, loanId);
                        updateStmt.executeUpdate();
                        System.out.printf("Payment of $%.2f applied to Loan %s.%n", paymentAmount, loanId);
                    }
                }

                // Record the loan transaction
                recordLoanTransaction(conn, loanId, paymentAmount);

                conn.commit(); // Commit transaction
            } catch (SQLException e) {
                conn.rollback(); // Rollback on failure
                throw e;
            } finally {
                conn.setAutoCommit(true); // Restore default behavior
            }

        } catch (SQLException | NumberFormatException e) {
            System.out.println("An error occurred while processing the payment.");
            
        }
    }

    // Display all loan transactions for a customer
    public static void viewPastLoanTransactions(Scanner scanner, Connection conn, String customerId) {
        String loanTransactionQuery = "SELECT lt.loan_trans_id, lt.loan_id, lt.amount, lt.trans_date " +
                                    "FROM loan_transaction lt " +
                                    "JOIN loan l ON lt.loan_id = l.loan_id " +
                                    "WHERE l.customer_id = ? " +
                                    "ORDER BY lt.trans_date DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(loanTransactionQuery)) {
            pstmt.setString(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            // Check if there are any loan transactions
            if (!rs.isBeforeFirst()) {
                System.out.println("No past loan transactions found for Customer ID: " + customerId);
                return;
            }

            // Display loan transactions
            System.out.printf("%-15s %-10s %-15s %-15s%n",
                            "Transaction ID", "Loan ID", "Amount Paid", "Transaction Date");
            while (rs.next()) {
                System.out.printf("%-15s %-10s %-15.2f %-15s%n",
                                rs.getString("loan_trans_id"),
                                rs.getString("loan_id"),
                                rs.getDouble("amount"),
                                rs.getDate("trans_date"));
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving loan transactions.");
            
        }
    }
}
