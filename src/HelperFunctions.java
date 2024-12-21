import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class HelperFunctions {
    // Verify that a customerID exists
    public static String customerVerification(Scanner scanner, Connection conn) {
        String customerId = null;
        boolean customerVerified = false;

        // Step 1: Validate that the customerID exists, otherwise prompt again
        while (!customerVerified) {
            System.out.print("Enter your Customer ID (10001): ");
            customerId = scanner.nextLine().trim();

            if (customerId.isEmpty()) {
                System.out.println("Customer ID cannot be empty. Please try again.");
                continue;
            }

            // Check if customerId exists in the database
            String checkCustomerIDQuery = "SELECT * FROM customer WHERE customer_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkCustomerIDQuery)) {
                pstmt.setString(1, customerId);
                ResultSet rs = pstmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("Customer ID not found. Please check and try again.");
                    continue;
                }

                rs.close();
            } catch (SQLException e) {
                System.out.println("An error occurred while retrieving the customer ID.");
                continue;
            }

            // Step 2: Once customerId is validated, prompt user for password
            boolean passwordCorrect = false;

            while (!passwordCorrect) {
                System.out.print("Enter your customer password (password1): ");
                String customerPassword = scanner.nextLine().trim();

                if (customerPassword.isEmpty()) {
                    System.out.println("Password cannot be empty. Please try again.");
                    continue;
                }

                String checkPasswordQuery = "SELECT * FROM customer WHERE customer_id = ? AND password = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(checkPasswordQuery)) {
                    pstmt.setString(1, customerId);
                    pstmt.setString(2, customerPassword);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        // Password is correct
                        passwordCorrect = true;
                        customerVerified = true;
                    } else {
                        System.out.println("Incorrect password, please try again.");
                    }

                    rs.close();
                } catch (SQLException e) {
                    System.out.println("An error occurred while verifying the password.");
                }
            }
        }

        // Return the verified customerId
        return customerId;
    }

    // Verify accountID exists and belongs to customerId
    public static String accountVerification(Scanner scanner, Connection conn, String customerId) {
        String accountId = null;
        boolean accountVerified = false;

        while (!accountVerified) {
            System.out.print("Enter the Account ID you want to check (AC000001): ");
            accountId = scanner.nextLine().trim();

            if (accountId.isEmpty()) {
                System.out.println("Account ID cannot be empty. Please try again.");
                continue;
            }

            // Check if accountId belongs to the customer
            String checkAccountIDQuery = "SELECT * FROM account WHERE account_id = ? AND customer_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkAccountIDQuery)) {
                pstmt.setString(1, accountId);
                pstmt.setString(2, customerId);
                ResultSet rs = pstmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("Account ID not found or does not belong to you. Please check and try again.");
                    continue;
                }

                accountVerified = true;
                rs.close();
            } catch (SQLException e) {
                System.out.println("An error occurred while verifying the account ID.");
            }
        }

        return accountId;
    }

     // Validate details for a debit card
     public static boolean validateDebitCard(Connection conn, String accountId, String cardNumber, String secCode) {
        String cardValidationQuery = "SELECT 1 FROM debit_card WHERE card_number = ? AND sec_code = ? AND account_id = ?";
        try (PreparedStatement cardStmt = conn.prepareStatement(cardValidationQuery)) {
            cardStmt.setString(1, cardNumber);
            cardStmt.setString(2, secCode);
            cardStmt.setString(3, accountId);

            ResultSet cardRs = cardStmt.executeQuery();
            return cardRs.next(); // Return true if the card details are valid
        } catch (SQLException e) {
            System.out.println("An error occurred while validating the debit card.");
            
            return false;
        }
    }

    // Validate details for a credit card
    public static boolean validateCreditCard(Connection conn, String customerId, String cardNumber, String secCode) {
        String cardValidationQuery = 
            "SELECT 1 " +
            "FROM credit_card " +
            "WHERE card_number = ? AND security_code = ? AND customer_id = ?";
        try (PreparedStatement cardStmt = conn.prepareStatement(cardValidationQuery)) {
            cardStmt.setString(1, cardNumber);   
            cardStmt.setString(2, secCode);     
            cardStmt.setString(3, customerId);  

            ResultSet cardRs = cardStmt.executeQuery();
            return cardRs.next(); // Return true if the card details are valid
        } catch (SQLException e) {
            System.out.println("An error occurred while validating the credit card.");
            
            return false;
        }
    }

    // Check if a checking account has enough money for a transaction
    public static boolean hasSufficientBalance(Connection conn, String accountId, double paymentAmount) {
        String balanceQuery = "SELECT balance FROM checking WHERE account_id = ?";
        double accountBalance = 0;

        try (PreparedStatement balanceStmt = conn.prepareStatement(balanceQuery)) {
            balanceStmt.setString(1, accountId);
            ResultSet balanceRs = balanceStmt.executeQuery();

            if (balanceRs.next()) {
                accountBalance = balanceRs.getDouble("balance");
            } else {
                System.out.println("Account not found or invalid.");
                return false;
            }

            balanceRs.close();
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving the account balance.");
            
            return false;
        }

        // Return true if the balance is sufficient, false otherwise
        return paymentAmount <= accountBalance;
    }

    // Payment done with debit card
    public static void debitCardPayment(Connection conn, String accountId, String cardNumber, double paymentAmount) {
        try {
            // Step 1: Check if there is enough balance in the checking account
            if (!hasSufficientBalance(conn, accountId, paymentAmount)) {
                System.out.println("Insufficient funds in the checking account. Payment cannot proceed.");
                return;
            }

            // Step 2: Deduct the payment amount from the checking account
            String deductBalanceQuery = "UPDATE checking SET balance = balance - ? WHERE account_id = ?";
            try (PreparedStatement deductStmt = conn.prepareStatement(deductBalanceQuery)) {
                deductStmt.setDouble(1, paymentAmount);
                deductStmt.setString(2, accountId);
                deductStmt.executeUpdate();
                System.out.printf("Amount $%.2f successfully deducted from account %s.%n", paymentAmount, accountId);
            }

            // Step 3: Insert the transaction into the debit_card_transaction table
            String insertDebitTransactionQuery = "INSERT INTO debit_card_transaction (card_number, amount, trans_date) " +
                                                "VALUES (?, ?, SYSDATE)";
            try (PreparedStatement debitStmt = conn.prepareStatement(insertDebitTransactionQuery)) {
                debitStmt.setString(1, cardNumber);
                debitStmt.setDouble(2, paymentAmount);
                debitStmt.executeUpdate();
                System.out.printf("Debit transaction of $%.2f recorded successfully for card number %s.%n", paymentAmount, cardNumber);
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while processing the debit transaction.");
            
        }
    }

    // Make payment with credit card
    public static void creditCardPayment(Connection conn, String cardNumber, double paymentAmount) {
        try {
            // Step 1: Check if the credit card exists and validate it
            String validateCardQuery = 
                "SELECT running_balance, credit_limit " +
                "FROM credit_card " +
                "WHERE card_number = ?";
            double runningBalance = 0;
            double creditLimit = 0;

            try (PreparedStatement validateStmt = conn.prepareStatement(validateCardQuery)) {
                validateStmt.setString(1, cardNumber);
                ResultSet rs = validateStmt.executeQuery();
                if (rs.next()) {
                    runningBalance = rs.getDouble("running_balance");
                    creditLimit = rs.getDouble("credit_limit");
                } else {
                    System.out.println("Credit card not found. Payment canceled.");
                    return;
                }
            }

            // Step 2: Check if the payment exceeds the available credit limit
            if (runningBalance + paymentAmount > creditLimit) {
                System.out.println("Payment exceeds the credit limit. Payment canceled.");
                return;
            }

            // Step 3: Update the credit card running balance
            String updateBalanceQuery = "UPDATE credit_card SET running_balance = running_balance + ? WHERE card_number = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateBalanceQuery)) {
                updateStmt.setDouble(1, paymentAmount);
                updateStmt.setString(2, cardNumber);
                updateStmt.executeUpdate();
                System.out.printf("Credit card running balance updated successfully for card number %s.%n", cardNumber);
            }

            // Step 4: Record the transaction in the credit_card_transaction table
            String insertTransactionQuery = 
                "INSERT INTO credit_card_transaction (card_number, amount, trans_date) VALUES (?, ?, SYSDATE)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertTransactionQuery)) {
                insertStmt.setString(1, cardNumber);
                insertStmt.setDouble(2, paymentAmount);
                insertStmt.executeUpdate();
                System.out.printf("Credit transaction of $%.2f recorded successfully for card number %s.%n", paymentAmount, cardNumber);
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while processing the credit card payment.");
            
        }
    }
}
