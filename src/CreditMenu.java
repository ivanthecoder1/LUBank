import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.Date;

public class CreditMenu {
    // Credit Card Management Menu
    public static void creditCardManagementMenu(Scanner scanner, Connection conn) {
        boolean back = false;
        String customerId = HelperFunctions.customerVerification(scanner, conn);
        while (!back) {
            System.out.println("\n===== Credit Card Management Menu =====");
            System.out.println("1. View credit card information");
            System.out.println("2. Open a new credit card");
            System.out.println("3. Pay off a running balance for a credit card");
            System.out.println("4. View past transactions");
            System.out.println("5. Back to Main Menu");
            System.out.print("Input an option (1-5): ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    viewCreditCardInfo(scanner, conn, customerId);
                    break;
                case "2":
                    openNewCreditCard(scanner, conn, customerId);
                    break;
                case "3":
                    payOffCreditCard(scanner, conn, customerId);
                    break;
                case "4":
                    viewPastCreditTransactions(scanner, conn, customerId);
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 5.");
            }
        }
    }

    // Opens a new credit card for a customer
    public static void openNewCreditCard(Scanner scanner, Connection conn, String customerId) {
        // Randomly generate credit card details for min_balance, credit limit, and interest rate
        double minBalance = Math.round(Math.random() * (500 - 100) + 100); // Random between 100 and 500
        double creditLimit = Math.round(Math.random() * (5000 - 1000) + 1000); // Random between 1000 and 5000
        double interestRate = Math.round(Math.random() * (25 - 15) + 15); // Random between 15% and 25%

        // Display generated conditions to the user
        System.out.printf("Credit Card Offer for Customer ID: %s%n", customerId);
        System.out.printf("Minimum Balance: $%.2f%n", minBalance);
        System.out.printf("Credit Limit: $%.2f%n", creditLimit);
        System.out.printf("Interest Rate: %.2f%%%n", interestRate);
        System.out.print("Do you accept these conditions? (yes/no): ");
        String userResponse = scanner.nextLine().trim().toLowerCase();

        // Proceed only if the user accepts the conditions
        if (!userResponse.equals("yes")) {
            System.out.println("Credit card application canceled.");
            return;
        }

        // Query to insert the new credit card
        String insertCreditCardQuery = "INSERT INTO credit_card (expiration_date, credit_limit, running_balance, " +
                                        "balance_due, interest_rate, security_code, customer_id) " +
                                        "VALUES (NULL, ?, 0, 0, ?, NULL, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertCreditCardQuery)) {
            pstmt.setDouble(1, creditLimit); // Set credit limit
            pstmt.setDouble(2, interestRate); // Set interest rate
            pstmt.setString(3, customerId); // Set customer ID
            pstmt.executeUpdate();

            System.out.println("New credit card successfully created for customer ID: " + customerId);
        } catch (SQLException e) {
            System.out.println("An error occurred while creating the credit card.");
            
        }
    }

    // View all credit card information associated with a customer_id
    public static void viewCreditCardInfo(Scanner scanner, Connection conn, String customerId) {
        // SQL query to retrieve all credit card information for the customer
        String query = "SELECT card_number, expiration_date, credit_limit, running_balance, " +
                    "balance_due, interest_rate, security_code " +
                    "FROM credit_card WHERE customer_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            // Check if the customer has any credit cards
            if (!rs.isBeforeFirst()) {
                System.out.printf("No credit cards found for customer ID: %s%n", customerId);
                return;
            }

            // Display all credit cards
            System.out.printf("Credit Card Information for Customer ID: %s%n", customerId);
            System.out.printf("%-20s %-15s %-15s %-15s %-15s %-15s %-10s%n",
                    "Card Number", "Expiration Date", "Credit Limit", "Running Balance", "Balance Due", "Interest Rate", "Sec Code");

            while (rs.next()) {
                String cardNumber = rs.getString("card_number");
                Date expirationDate = rs.getDate("expiration_date");
                double creditLimit = rs.getDouble("credit_limit");
                double runningBalance = rs.getDouble("running_balance");
                double balanceDue = rs.getDouble("balance_due");
                double interestRate = rs.getDouble("interest_rate");
                String securityCode = rs.getString("security_code");

                // Print credit card information
                System.out.printf("%-20s %-15s %-15.2f %-15.2f %-15.2f %-15.2f %-10s%n",
                        cardNumber, expirationDate, creditLimit, runningBalance, balanceDue, interestRate, securityCode);
            }

            rs.close();
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving credit card information.");
            
        }
    }

    // Pay off a credit card
    public static void payOffCreditCard(Scanner scanner, Connection conn, String customerId) {
        try {
            // Step 1: Display credit cards with a running_balance > 0
            System.out.println("Credit cards with outstanding balances for Customer ID: " + customerId);
            String creditCardQuery = "SELECT card_number, running_balance, credit_limit, balance_due " +
                                "FROM credit_card WHERE customer_id = ? AND running_balance > 0";
            String cardNumber = null;
            double runningBalance = 0;
            boolean validCard = false;

            try (PreparedStatement cardStmt = conn.prepareStatement(creditCardQuery)) {
                cardStmt.setString(1, customerId);
                ResultSet cardRs = cardStmt.executeQuery();

                if (!cardRs.isBeforeFirst()) {
                    System.out.println("No credit cards with outstanding balances found.");
                    return;
                }

                System.out.printf("%-20s %-15s %-15s %-15s%n", "Card Number", "Running Balance", "Credit Limit", "Balance Due");
                while (cardRs.next()) {
                    System.out.printf("%-20s %-15.2f %-15.2f %-15.2f%n",
                                    cardRs.getString("card_number"),
                                    cardRs.getDouble("running_balance"),
                                    cardRs.getDouble("credit_limit"),
                                    cardRs.getDouble("balance_due"));
                }

                // Prompt for the credit card to pay off
                System.out.print("Enter the Credit Card Number you want to pay off (5142239148670084): ");
                cardNumber = scanner.nextLine();

                // Re-execute to validate credit card number
                cardRs = cardStmt.executeQuery();
                while (cardRs.next()) {
                    if (cardRs.getString("card_number").equals(cardNumber)) {
                        validCard = true;
                        runningBalance = cardRs.getDouble("running_balance");
                        break;
                    }
                }

                if (!validCard) {
                    System.out.println("Invalid Credit Card Number or Card does not belong to you or card has no running balance. Returning to menu.");
                    return;
                }
            }

            // Step 2: Validate debit card details
            System.out.print("Enter the Account ID linked to the debit card you will be using to pay (AC000001): ");
            String accountId = scanner.nextLine();
            System.out.print("Enter the Debit Card Number (4742239148670084): ");
            String debitCardNumber = scanner.nextLine();
            System.out.print("Enter the Security Code (123): ");
            String secCode = scanner.nextLine();

            if (!HelperFunctions.validateDebitCard(conn, accountId, debitCardNumber, secCode)) {
                System.out.println("Invalid debit card details. Payment cannot proceed.");
                return;
            }

            // Step 3: Validate sufficient funds and payment amount
            System.out.printf("Credit Card Balance: $%.2f%n", runningBalance);
            System.out.print("Enter the amount you want to pay: ");
            double paymentAmount = Double.parseDouble(scanner.nextLine());

            if (paymentAmount <= 0 || paymentAmount > runningBalance) {
                System.out.println("Invalid payment amount. Payment canceled.");
                return;
            }

            if (!HelperFunctions.hasSufficientBalance(conn, accountId, paymentAmount)) {
                System.out.println("Insufficient funds in the associated checking account. Payment cannot proceed.");
                return;
            }

            // Step 4: Deduct funds and record the payment
            HelperFunctions.debitCardPayment(conn, accountId, debitCardNumber, paymentAmount);

            // Step 5: Update the credit card balance
            conn.setAutoCommit(false); // Begin transaction
            try {
                String updateCreditCardQuery = "UPDATE credit_card SET running_balance = running_balance - ? WHERE card_number = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateCreditCardQuery)) {
                    updateStmt.setDouble(1, paymentAmount);
                    updateStmt.setString(2, cardNumber);
                    updateStmt.executeUpdate();
                    System.out.printf("Payment of $%.2f applied to Credit Card %s.%n", paymentAmount, cardNumber);
                }
                conn.commit(); // Commit transaction
            } catch (SQLException e) {
                conn.rollback(); // Rollback on failure
                throw e;
            } finally {
                conn.setAutoCommit(true); // Restore default behavior
            }

        } catch (SQLException | NumberFormatException e) {
            System.out.println("An error occurred while processing the credit card payment.");
            
        }
    }

    // Display all credit transactions
    public static void viewPastCreditTransactions(Scanner scanner, Connection conn, String customerId) {
        String creditTransactionQuery = "SELECT ct.credit_trans_id, ct.card_number, ct.amount, ct.trans_date " +
                                        "FROM credit_card_transaction ct " +
                                        "JOIN credit_card cc ON ct.card_number = cc.card_number " +
                                        "WHERE cc.customer_id = ? " +
                                        "ORDER BY ct.trans_date DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(creditTransactionQuery)) {
            pstmt.setString(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            // Check if there are any credit transactions
            if (!rs.isBeforeFirst()) {
                System.out.println("No past credit transactions found for Customer ID: " + customerId);
                return;
            }

            // Display credit transactions
            System.out.printf("%-15s %-20s %-15s %-15s%n",
                            "Transaction ID", "Card Number", "Amount Paid", "Transaction Date");
            while (rs.next()) {
                System.out.printf("%-15s %-20s %-15.2f %-15s%n",
                                rs.getString("credit_trans_id"),
                                rs.getString("card_number"),
                                rs.getDouble("amount"),
                                rs.getDate("trans_date"));
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving credit transactions.");
            
        }
    }
}
