import java.sql.Connection;
import java.sql.PreparedStatement;  
import java.sql.ResultSet;          
import java.sql.SQLException;
import java.util.Scanner;

public class BankMenu {
    // Bank Management Menu
    public static void bankManagementMenu(Scanner scanner, Connection conn) {
        boolean back = false;
        String customerId = HelperFunctions.customerVerification(scanner, conn);
        while (!back) {
            System.out.println("\n===== Bank Management Menu =====");
            System.out.println("1. View checking account balance");
            System.out.println("2. View saving account information");
            System.out.println("3. View investment assets");
            System.out.println("4. Get a new debit card");
            System.out.println("5. View all debit card information");
            System.out.println("6. Open a new main account");
            System.out.println("7. Open a checking account");
            System.out.println("8. Open a saving account");
            System.out.println("9. Transfer money");
            System.out.println("10. Deposit money");
            System.out.println("11. Withdraw money");
            System.out.println("12. View all transactions (Debit & Money Transfers)");
            System.out.println("13. View all accounts");
            System.out.println("14. Back to Main Menu");
            System.out.print("Input an option (1 - 14): ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    viewCheckingBalance(scanner, conn, customerId);
                    break;
                case "2":
                    viewSavingAccountInfo(scanner, conn, customerId);
                    break;
                case "3":
                    viewInvestmentAssets(scanner, conn, customerId);
                    break;
                case "4":
                    requestNewDebitCard(scanner, conn, customerId);
                    break;
                case "5":
                    viewAllDebitCards(scanner, conn, customerId);
                    break;
                case "6":
                    openNewAccount(scanner, conn, customerId);
                    break;
                case "7":
                    openNewCheckingAccount(scanner, conn, customerId);
                    break;
                case "8":
                    openNewSavingAccount(scanner, conn, customerId);
                    break;
                case "9":
                    transferMoney(scanner, conn, customerId);
                    break;
                case "10":
                    depositMoney(scanner, conn, customerId);
                    break;
                case "11":
                    withdrawMoney(scanner, conn, customerId);
                    break;
                case "12":
                    viewAccountTransactions(scanner, conn, customerId);
                    break;
                case "13":
                    viewAllAccounts(conn, customerId);
                    break;
                case "14":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 14.");
            }
        }
    }

    // Return the checking balance based on accountID
    public static void viewCheckingBalance(Scanner scanner, Connection conn, String customerId) {
        // Verify accountId
        String accountId = HelperFunctions.accountVerification(scanner, conn, customerId);

        String accountCheckQuery = "SELECT balance FROM checking WHERE account_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(accountCheckQuery)) {
            pstmt.setString(1, accountId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                System.out.printf("Your current balance for your checking account %s is: $%.2f%n", accountId, balance);
            } else {
                // No checking account associated with the accountId
                System.out.printf("No checking account found for account ID %s.%n", accountId);
            }

            rs.close();
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving the balance.");
            
        }
    }

    // View saving account information for an accountId
    public static void viewSavingAccountInfo(Scanner scanner, Connection conn, String customerId) {
        // Verify account
        String accountId = HelperFunctions.accountVerification(scanner, conn, customerId);

        // Query to retrieve balance, min_balance, and interest_rate
        String accountCheckQuery = "SELECT balance, min_balance, interest_rate FROM saving WHERE account_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(accountCheckQuery)) {
            pstmt.setString(1, accountId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                double minBalance = rs.getDouble("min_balance");
                double interestRate = rs.getDouble("interest_rate");

                System.out.printf("Savings Account Information for %s:%n", accountId);
                System.out.printf("  Current Balance: $%.2f%n", balance);
                System.out.printf("  Minimum Balance: $%.2f%n", minBalance);
                System.out.printf("  Interest Rate: %.2f%%%n", interestRate);
            } else {
                // No saving account associated with the accountId
                System.out.printf("No saving account found for account ID %s.%n", accountId);
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving the savings account information.");
            
        }
    }

    // View all investment assests for an accountId
    public static void viewInvestmentAssets(Scanner scanner, Connection conn, String customerId) {
        // Verify accountId
        String accountId = HelperFunctions.accountVerification(scanner, conn, customerId);

        // Query to retrieve company, shares, and share_price for the investments
        String investmentQuery = "SELECT company, shares, share_price FROM investment WHERE account_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(investmentQuery)) {
            pstmt.setString(1, accountId);
            ResultSet rs = pstmt.executeQuery();

            double totalPortfolioValue = 0.0;

            System.out.printf("Investment Portfolio for Account ID: %s%n", accountId);
            System.out.println("---------------------------------------------------");
            System.out.printf("%-20s %-10s %-10s %-10s%n", "Company", "Shares", "Share Price", "Total Value");

            while (rs.next()) {
                String company = rs.getString("company");
                int shares = rs.getInt("shares");
                double sharePrice = rs.getDouble("share_price");
                double totalValue = shares * sharePrice;

                totalPortfolioValue += totalValue;

                System.out.printf("%-20s %-10d $%-9.2f $%-9.2f%n", company, shares, sharePrice, totalValue);
            }

            System.out.println("---------------------------------------------------");
            System.out.printf("Total Portfolio Value: $%.2f%n", totalPortfolioValue);

            rs.close();
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving the investment assets.");
            
        }
    }

    // Opens a new account associated with customerId
    public static void openNewAccount(Scanner scanner, Connection conn, String customerId) {
        // SQL query to insert a new account (trigger handles account_id and checking account creation)
        String insertAccountQuery = "INSERT INTO account (customer_id) VALUES (?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertAccountQuery)) {
            // Set the customer_id parameter
            pstmt.setString(1, customerId);

            // Execute the insert statement
            pstmt.executeUpdate();
            System.out.println("New account created successfully for customer ID: " + customerId);

            // Retrieve and display the generated account_id
            String getLatestAccountQuery = "SELECT MAX(account_id) AS account_id FROM account WHERE customer_id = ?";
            try (PreparedStatement pstmt2 = conn.prepareStatement(getLatestAccountQuery)) {
                pstmt2.setString(1, customerId);
                ResultSet rs = pstmt2.executeQuery();
                if (rs.next()) {
                    String newAccountId = rs.getString("account_id");
                    System.out.println("Your new account ID is: " + newAccountId);
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while creating a new account.");
            
        }
    }

    // Creates a new debit card for an accountID connected to their checking account
    public static void requestNewDebitCard(Scanner scanner, Connection conn, String customerId) {
        // Verify the accountId
        String accountId = HelperFunctions.accountVerification(scanner, conn, customerId);

        // Query to insert a new debit card
        String insertDebitCardQuery = "INSERT INTO debit_card (account_id) VALUES (?)";

        try (PreparedStatement insertStmt = conn.prepareStatement(insertDebitCardQuery)) {
            insertStmt.setString(1, accountId);
            insertStmt.executeUpdate();
            System.out.printf("New debit card successfully created for account ID %s.%n", accountId);
        } catch (SQLException e) {
            System.out.println("An error occurred while requesting a new debit card.");
            
        }
    }

    // Opens a checking account for an accountID
    public static void openNewCheckingAccount(Scanner scanner, Connection conn, String customerId) {
        // Verify the accountId
        String accountId = HelperFunctions.accountVerification(scanner, conn, customerId);

        // Query to check if a checking account already exists for the accountId
        String checkExistingQuery = "SELECT 1 FROM checking WHERE account_id = ?";

        // Query to insert a new checking account
        String insertCheckingQuery = "INSERT INTO checking (account_id, balance) VALUES (?, 0)";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkExistingQuery)) {
            checkStmt.setString(1, accountId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // A checking account already exists for this accountId
                System.out.printf("A checking account already exists for account ID %s.%n", accountId);
            } else {
                // No checking account exists; proceed to create a new one
                try (PreparedStatement insertStmt = conn.prepareStatement(insertCheckingQuery)) {
                    insertStmt.setString(1, accountId);
                    insertStmt.executeUpdate();
                    System.out.printf("New checking account successfully created for account ID %s.%n", accountId);
                }
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("An error occurred while creating the checking account.");
            
        }
    }

    // Open Saving Account for an accountId
    public static void openNewSavingAccount(Scanner scanner, Connection conn, String customerId) {
        // Verify the accountId
        String accountId = HelperFunctions.accountVerification(scanner, conn, customerId);

        // Query to check if a saving account already exists
        String checkExistingQuery = "SELECT 1 FROM saving WHERE account_id = ?";

        // Query to insert a new saving account
        String insertSavingAccountQuery = "INSERT INTO saving (account_id, balance, interest_rate, min_balance) VALUES (?, ?, ?, ?)";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkExistingQuery)) {
            checkStmt.setString(1, accountId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // A saving account already exists for this accountId
                System.out.printf("A saving account already exists for account ID %s.%n", accountId);
                return;
            }
            rs.close();

            // Generate random interest rate and minimum balance
            double interestRate = Math.round(Math.random() * (5 - 1) + 1); // Random between 1% and 5%
            double minBalance = Math.round(Math.random() * (1000 - 100) + 100); // Random between $100 and $1000

            // Display generated conditions to the user
            System.out.printf("Saving Account Offer for Account ID: %s%n", accountId);
            System.out.printf("Interest Rate: %.2f%%%n", interestRate);
            System.out.printf("Minimum Balance: $%.2f%n", minBalance);
            System.out.print("Do you accept these conditions? (yes/no): ");
            String userResponse = scanner.nextLine().trim().toLowerCase();

            // Proceed only if the user accepts the conditions
            if (!userResponse.equals("yes")) {
                System.out.println("Saving account application canceled.");
                return;
            }

            // Insert the new saving account
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSavingAccountQuery)) {
                insertStmt.setString(1, accountId); // Set account ID
                insertStmt.setDouble(2, 0.0); // Set initial balance to 0
                insertStmt.setDouble(3, interestRate); // Set interest rate
                insertStmt.setDouble(4, minBalance); // Set minimum balance
                insertStmt.executeUpdate();
                System.out.printf("New saving account successfully created for account ID %s.%n", accountId);
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while creating the saving account.");
            
        }
    }

    // Display information for all debit cards connected to an account ID
    public static void viewAllDebitCards(Scanner scanner, Connection conn, String customerId) {
        try {
            // Step 1: Ask user for the account ID
            System.out.print("Enter the Account ID to view associated debit cards: ");
            String accountId = scanner.nextLine();

            // Step 2: Validate that the account belongs to the customer
            String validateAccountQuery = "SELECT account_id FROM account WHERE account_id = ? AND customer_id = ?";
            try (PreparedStatement validateStmt = conn.prepareStatement(validateAccountQuery)) {
                validateStmt.setString(1, accountId);
                validateStmt.setString(2, customerId);
                ResultSet validateRs = validateStmt.executeQuery();

                if (!validateRs.next()) {
                    System.out.println("Invalid or non-existent account ID for this customer.");
                    return;
                }
            }

            // Step 3: Retrieve and display debit card information
            String debitCardQuery = "SELECT card_number, sec_code, expiration_date FROM debit_card WHERE account_id = ?";
            try (PreparedStatement debitCardStmt = conn.prepareStatement(debitCardQuery)) {
                debitCardStmt.setString(1, accountId);
                ResultSet debitCardRs = debitCardStmt.executeQuery();

                if (!debitCardRs.isBeforeFirst()) {
                    System.out.println("No debit cards found for the specified account ID.");
                    return;
                }

                System.out.printf("%-20s %-10s %-15s%n", "Card Number", "Security Code", "Expiration Date");
                while (debitCardRs.next()) {
                    System.out.printf("%-20s %-10s %-15s%n",
                            debitCardRs.getString("card_number"),
                            debitCardRs.getString("sec_code"),
                            debitCardRs.getDate("expiration_date"));
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving debit card information.");
        }
    }

    // Allow user to transfer money between acounts
    public static void transferMoney(Scanner scanner, Connection conn, String customerId) {
        try {
            // Step 1: Ask the user which account they want to transfer from
            System.out.println("Enter the Account ID you want to use to transfer from (AC000001):");
            String sourceAccountId = scanner.nextLine();

            // Validate that the source account exists and belongs to the user
            String validateSourceAccountQuery = "SELECT account_id FROM account WHERE account_id = ? AND customer_id = ?";
            try (PreparedStatement sourceAccountStmt = conn.prepareStatement(validateSourceAccountQuery)) {
                sourceAccountStmt.setString(1, sourceAccountId);
                sourceAccountStmt.setString(2, customerId);
                ResultSet sourceAccountRs = sourceAccountStmt.executeQuery();

                if (!sourceAccountRs.next()) {
                    System.out.println("Invalid or non-existent source Account ID. Transfer canceled.");
                    return;
                }
            }

            // Step 2: Validate the existence of `checking` or `saving` for the source account
            System.out.println("Is the source account a Checking or Saving account? (Enter C/S):");
            String sourceAccountType = scanner.nextLine().trim().toUpperCase();

            if (!sourceAccountType.equals("C") && !sourceAccountType.equals("S")) {
                System.out.println("Invalid source account type. Transfer canceled.");
                return;
            }

            String sourceBalanceQuery = "SELECT balance" + (sourceAccountType.equals("S") ? ", min_balance" : "") + " FROM " + (sourceAccountType.equals("C") ? "checking" : "saving") + " WHERE account_id = ?";
            boolean sourceExists = false;
            double sourceBalance = 0;
            double minBalance = 0;

            try (PreparedStatement sourceBalanceStmt = conn.prepareStatement(sourceBalanceQuery)) {
                sourceBalanceStmt.setString(1, sourceAccountId);
                ResultSet sourceBalanceRs = sourceBalanceStmt.executeQuery();

                if (sourceBalanceRs.next()) {
                    sourceExists = true;
                    sourceBalance = sourceBalanceRs.getDouble("balance");
                    if (sourceAccountType.equals("S")) {
                        minBalance = sourceBalanceRs.getDouble("min_balance");
                    }
                }
            }

            if (!sourceExists) {
                System.out.printf("The selected source account (%s) does not have an associated %s account. Transfer canceled.%n",
                        sourceAccountId, sourceAccountType.equals("C") ? "Checking" : "Saving");
                return;
            }

            // Step 3: Ask the user which account they want to transfer to
            System.out.println("Enter the Account ID you want to transfer to (AC000002):");
            String targetAccountId = scanner.nextLine();

            // Validate that the target account exists
            String validateTargetAccountQuery = "SELECT account_id FROM account WHERE account_id = ?";
            try (PreparedStatement targetAccountStmt = conn.prepareStatement(validateTargetAccountQuery)) {
                targetAccountStmt.setString(1, targetAccountId);
                ResultSet targetAccountRs = targetAccountStmt.executeQuery();

                if (!targetAccountRs.next()) {
                    System.out.println("Invalid or non-existent target Account ID or account does not belong to you. Transfer canceled.");
                    return;
                }
            }

            // Validate the existence of `checking` or `saving` for the target account
            System.out.println("Is the target account a Checking or Saving account? (Enter C/S):");
            String targetAccountType = scanner.nextLine().trim().toUpperCase();

            if (!targetAccountType.equals("C") && !targetAccountType.equals("S")) {
                System.out.println("Invalid target account type. Transfer canceled.");
                return;
            }

            String targetBalanceQuery = "SELECT balance FROM " + (targetAccountType.equals("C") ? "checking" : "saving") + " WHERE account_id = ?";
            boolean targetExists = false;

            try (PreparedStatement targetBalanceStmt = conn.prepareStatement(targetBalanceQuery)) {
                targetBalanceStmt.setString(1, targetAccountId);
                ResultSet targetBalanceRs = targetBalanceStmt.executeQuery();

                if (targetBalanceRs.next()) {
                    targetExists = true;
                }
            }

            if (!targetExists) {
                System.out.printf("The selected target account (%s) does not have an associated %s account. Transfer canceled.%n",
                        targetAccountId, targetAccountType.equals("C") ? "Checking" : "Saving");
                return;
            }

            // Ensure they are not transferring between the same account type and ID
            if (sourceAccountId.equals(targetAccountId) && sourceAccountType.equals(targetAccountType)) {
                System.out.println("Cannot transfer between the same type of accounts for the same Account ID. Transfer canceled.");
                return;
            }

            // Step 4: Ask the user the amount to transfer
            System.out.println("Enter the amount you want to transfer:");
            double transferAmount = Double.parseDouble(scanner.nextLine());

            if (transferAmount <= 0) {
                System.out.println("Transfer amount must be greater than 0. Transfer canceled.");
                return;
            }

            // Validate sufficient funds in the source account
            if (transferAmount > sourceBalance) {
                System.out.println("Insufficient funds in the source account. Transfer canceled.");
                return;
            }

            // Check if savings account transfer violates minimum balance
            if (sourceAccountType.equals("S") && (sourceBalance - transferAmount < minBalance)) {
                System.out.println("This transfer will take your savings below the minimum balance. A $5 fee will be applied.");
                System.out.print("Do you want to proceed? (yes/no): ");
                String confirmFee = scanner.nextLine().trim().toLowerCase();
                if (!confirmFee.equals("yes")) {
                    System.out.println("Transfer canceled.");
                    return;
                }
                transferAmount += 5; // Add the $5 fee
            }

            // Step 5: Process the transfer
            conn.setAutoCommit(false); // Begin transaction
            try {
                // Deduct from source account
                String deductFromSourceQuery = "UPDATE " + (sourceAccountType.equals("C") ? "checking" : "saving") + " SET balance = balance - ? WHERE account_id = ?";
                try (PreparedStatement deductStmt = conn.prepareStatement(deductFromSourceQuery)) {
                    deductStmt.setDouble(1, transferAmount);
                    deductStmt.setString(2, sourceAccountId);
                    deductStmt.executeUpdate();
                }

                // Add to target account
                String addToTargetQuery = "UPDATE " + (targetAccountType.equals("C") ? "checking" : "saving") + " SET balance = balance + ? WHERE account_id = ?";
                try (PreparedStatement addStmt = conn.prepareStatement(addToTargetQuery)) {
                    addStmt.setDouble(1, transferAmount);
                    addStmt.setString(2, targetAccountId);
                    addStmt.executeUpdate();
                }

                // Record the transaction in `transfer_transaction`
                String recordTransferQuery = "INSERT INTO transfer_transaction (recipient, sender, amount, trans_date) VALUES (?, ?, ?, SYSDATE)";
                try (PreparedStatement recordStmt = conn.prepareStatement(recordTransferQuery)) {
                    recordStmt.setString(1, targetAccountId);
                    recordStmt.setString(2, sourceAccountId);
                    recordStmt.setDouble(3, transferAmount);
                    recordStmt.executeUpdate();
                }

                conn.commit(); // Commit transaction
                System.out.printf("Successfully transferred $%.2f from %s (%s) to %s (%s).%n",
                        transferAmount, sourceAccountId, sourceAccountType.equals("C") ? "Checking" : "Saving",
                        targetAccountId, targetAccountType.equals("C") ? "Checking" : "Saving");
            } catch (SQLException e) {
                conn.rollback(); // Rollback transaction on failure
                throw e;
            } finally {
                conn.setAutoCommit(true); // Restore default behavior
            }
        } catch (SQLException | NumberFormatException e) {
            System.out.println("An error occurred while processing the transfer.");
        }
    }

    // Ask user which accountId they want to deposit to
    public static void depositMoney(Scanner scanner, Connection conn, String customerId) {
        try {
            // Step 1: Ask user which account they want to deposit to
            System.out.println("Enter the Account ID you want to deposit to (AC000001):");
            String accountId = scanner.nextLine();

            // Validate that the account exists and belongs to the user
            String validateAccountQuery = "SELECT account_id FROM account WHERE account_id = ? AND customer_id = ?";
            try (PreparedStatement validateStmt = conn.prepareStatement(validateAccountQuery)) {
                validateStmt.setString(1, accountId);
                validateStmt.setString(2, customerId);
                ResultSet rs = validateStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("Invalid or non-existent Account ID. Deposit canceled.");
                    return;
                }
            }

            // Step 2: Ask user if they want to deposit to checking or saving
            System.out.println("Do you want to deposit to Checking or Saving? (Enter C/S):");
            String accountType = scanner.nextLine().trim().toUpperCase();

            if (!accountType.equals("C") && !accountType.equals("S")) {
                System.out.println("Invalid account type selected. Deposit canceled.");
                return;
            }

            // Validate that the selected checking or saving account exists
            String validateSubAccountQuery = "SELECT 1 FROM " + (accountType.equals("C") ? "checking" : "saving") + " WHERE account_id = ?";
            try (PreparedStatement validateSubStmt = conn.prepareStatement(validateSubAccountQuery)) {
                validateSubStmt.setString(1, accountId);
                ResultSet rs = validateSubStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println((accountType.equals("C") ? "Checking" : "Saving") + " account does not exist for this Account ID. Deposit canceled.");
                    return;
                }
            }

            // Step 3: Ask user how much they are depositing
            System.out.println("Enter the amount you want to deposit:");
            double depositAmount = Double.parseDouble(scanner.nextLine());

            if (depositAmount <= 0) {
                System.out.println("Deposit amount must be greater than 0. Deposit canceled.");
                return;
            }

            // Step 4: Add the amount to the selected account
            String updateBalanceQuery = "UPDATE " + (accountType.equals("C") ? "checking" : "saving") + " SET balance = balance + ? WHERE account_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateBalanceQuery)) {
                updateStmt.setDouble(1, depositAmount);
                updateStmt.setString(2, accountId);
                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.printf("Successfully deposited $%.2f into your %s account (Account ID: %s).%n",
                            depositAmount, accountType.equals("C") ? "Checking" : "Saving", accountId);
                } else {
                    System.out.println("An error occurred. Deposit could not be completed.");
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while processing the deposit.");
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid deposit amount entered. Please try again.");
        }
    }

    // Ask user which accountId they want to withdraw from
    public static void withdrawMoney(Scanner scanner, Connection conn, String customerId) {
        try {
            // Step 1: Ask user which account they want to withdraw from
            System.out.println("Enter the Account ID you want to withdraw from (AC000001):");
            String accountId = scanner.nextLine();

            // Validate that the account exists and belongs to the user
            String validateAccountQuery = "SELECT account_id FROM account WHERE account_id = ? AND customer_id = ?";
            try (PreparedStatement validateStmt = conn.prepareStatement(validateAccountQuery)) {
                validateStmt.setString(1, accountId);
                validateStmt.setString(2, customerId);
                ResultSet rs = validateStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("Invalid or non-existent Account ID. Withdrawal canceled.");
                    return;
                }
            }

            // Step 2: Ask user if they want to withdraw from checking or saving
            System.out.println("Do you want to withdraw from Checking or Saving? (Enter C/S):");
            String accountType = scanner.nextLine().trim().toUpperCase();

            if (!accountType.equals("C") && !accountType.equals("S")) {
                System.out.println("Invalid account type selected. Withdrawal canceled.");
                return;
            }

            // Validate that the selected checking or saving account exists
            String validateSubAccountQuery = accountType.equals("C")
                    ? "SELECT balance FROM checking WHERE account_id = ?"
                    : "SELECT balance, min_balance FROM saving WHERE account_id = ?";
            double balance = 0;
            double minBalance = 0;

            try (PreparedStatement validateSubStmt = conn.prepareStatement(validateSubAccountQuery)) {
                validateSubStmt.setString(1, accountId);
                ResultSet rs = validateSubStmt.executeQuery();

                if (rs.next()) {
                    balance = rs.getDouble("balance");
                    if (accountType.equals("S")) {
                        minBalance = rs.getDouble("min_balance");
                    }
                } else {
                    System.out.println((accountType.equals("C") ? "Checking" : "Saving") + " account does not exist for this Account ID. Withdrawal canceled.");
                    return;
                }
            }

            // Step 3: Ask user how much they're withdrawing
            System.out.println("Enter the amount you want to withdraw:");
            double withdrawalAmount = Double.parseDouble(scanner.nextLine());

            if (withdrawalAmount <= 0) {
                System.out.println("Withdrawal amount must be greater than 0. Withdrawal canceled.");
                return;
            }

            if (withdrawalAmount > balance) {
                System.out.println("Insufficient funds in the account. Withdrawal canceled.");
                return;
            }

            // Step 4: Handle saving account specific rules
            if (accountType.equals("S") && (balance - withdrawalAmount < minBalance)) {
                System.out.println("This withdrawal will take your savings balance below the minimum balance. A $5 fee will be charged.");
                System.out.print("Do you want to proceed? (yes/no): ");
                String confirmFee = scanner.nextLine().trim().toLowerCase();
                if (!confirmFee.equals("yes")) {
                    System.out.println("Withdrawal canceled.");
                    return;
                }
                withdrawalAmount += 5; // Add $5 fee
            }

            // Step 5: Subtract the amount from the account
            String updateBalanceQuery = "UPDATE " + (accountType.equals("C") ? "checking" : "saving") + " SET balance = balance - ? WHERE account_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateBalanceQuery)) {
                updateStmt.setDouble(1, withdrawalAmount);
                updateStmt.setString(2, accountId);
                updateStmt.executeUpdate();

                System.out.printf("Successfully withdrew $%.2f from your %s account (Account ID: %s).%n",
                        withdrawalAmount - 5, accountType.equals("C") ? "Checking" : "Saving", accountId);
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while processing the withdrawal.");
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid withdrawal amount entered. Please try again.");
        }
    }

    // View all debit transaction 
    // View all transfer_transaction where sender has the accountId
    public static void viewAccountTransactions(Scanner scanner, Connection conn, String customerId) {
        try {
            // Step 1: Ask the user which account they want to view transactions for
            System.out.println("Enter the Account ID you want to view transactions for (AC000001):");
            String accountId = scanner.nextLine();

            // Validate the account exists and belongs to the customer
            String validateAccountQuery = "SELECT account_id FROM account WHERE account_id = ? AND customer_id = ?";
            try (PreparedStatement validateStmt = conn.prepareStatement(validateAccountQuery)) {
                validateStmt.setString(1, accountId);
                validateStmt.setString(2, customerId);
                ResultSet rs = validateStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("Invalid or non-existent Account ID. Returning to menu.");
                    return;
                }
            }

            // Step 2: Fetch and display debit card transactions linked to the account
            System.out.printf("Debit Transactions for Account ID: %s%n", accountId);
            System.out.println("---------------------------------");
            String debitTransactionQuery = 
                    "SELECT dct.debit_trans_id, dct.card_number, dct.amount, dct.trans_date " +
                    "FROM debit_card_transaction dct " +
                    "JOIN debit_card dc ON dct.card_number = dc.card_number " +
                    "WHERE dc.account_id = ? " +
                    "ORDER BY dct.trans_date DESC";

            try (PreparedStatement debitStmt = conn.prepareStatement(debitTransactionQuery)) {
                debitStmt.setString(1, accountId);
                ResultSet debitRs = debitStmt.executeQuery();

                if (!debitRs.isBeforeFirst()) {
                    System.out.println("No debit transactions found for this account.");
                } else {
                    System.out.printf("%-15s %-20s %-10s %-15s%n", "Transaction ID", "Card Number", "Amount", "Date");
                    while (debitRs.next()) {
                        System.out.printf("%-15s %-20s %-10.2f %-15s%n",
                                debitRs.getString("debit_trans_id"),
                                debitRs.getString("card_number"),
                                debitRs.getDouble("amount"),
                                debitRs.getDate("trans_date"));
                    }
                }
            }

            // Step 3: Fetch and display transfer transactions where the account is the sender
            System.out.printf("Transfer Transactions for Account ID: %s%n", accountId);
            System.out.println("---------------------------------");
            String transferTransactionQuery =
                    "SELECT tt.money_trans_id, tt.recipient, tt.amount, tt.trans_date " +
                    "FROM transfer_transaction tt " +
                    "WHERE tt.sender = ? " +
                    "ORDER BY tt.trans_date DESC";

            try (PreparedStatement transferStmt = conn.prepareStatement(transferTransactionQuery)) {
                transferStmt.setString(1, accountId);
                ResultSet transferRs = transferStmt.executeQuery();

                if (!transferRs.isBeforeFirst()) {
                    System.out.println("No transfer transactions found for this account.");
                } else {
                    System.out.printf("%-15s %-10s %-10s %-15s%n", "Transaction ID", "Recipient", "Amount", "Date");
                    while (transferRs.next()) {
                        System.out.printf("%-15s %-10s %-10.2f %-15s%n",
                                transferRs.getString("money_trans_id"),
                                transferRs.getString("recipient"),
                                transferRs.getDouble("amount"),
                                transferRs.getDate("trans_date"));
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving transactions.");
            
        }
    }

    // List all accountIds for a customer
    public static void viewAllAccounts(Connection conn, String customerId) {
        try {
            // Query to fetch all account IDs for the given customer ID
            String fetchAccountsQuery = 
                    "SELECT account_id " +
                    "FROM account " +
                    "WHERE customer_id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(fetchAccountsQuery)) {
                pstmt.setString(1, customerId);
                ResultSet rs = pstmt.executeQuery();

                // Check if the customer has any accounts
                if (!rs.isBeforeFirst()) {
                    System.out.printf("No accounts found for Customer ID: %s%n", customerId);
                    return;
                }

                // Display the accounts
                System.out.printf("Accounts for Customer ID: %s%n", customerId);
                System.out.println("---------------------------------");
                System.out.printf("%-10s%n", "Account ID");
                System.out.println("---------------------------------");
                while (rs.next()) {
                    System.out.printf("%-10s%n", rs.getString("account_id"));
                }
                System.out.println("---------------------------------");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving accounts.");
            
        }
    }
}
