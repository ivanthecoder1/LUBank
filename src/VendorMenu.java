import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class VendorMenu {
    // Vendor Menu
    public static void vendorMenu(Scanner scanner, Connection conn) {
        boolean back = false;
        String customerId = HelperFunctions.customerVerification(scanner, conn);
        while (!back) {
            System.out.println("\n===== Vendor Management Menu =====");
            System.out.println("1. View all vendors");
            System.out.println("2. Purchase from a vendor");
            System.out.println("3. Back to Main Menu");
            System.out.print("Input an option (1-3): ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    viewAllVendors(conn, customerId);
                    break;
                case "2":
                    purchaseFromVendor(scanner, conn, customerId);
                    break;
                case "3":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 3.");
            }
        }
    }

    // View all vendors
    public static void viewAllVendors(Connection conn, String customerId) {
        // SQL query to retrieve all vendors and their items
        String query = "SELECT vendor_id, vendor_name, item, item_price FROM vendor ORDER BY vendor_name";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();

            // Check if there are any vendors
            if (!rs.isBeforeFirst()) {
                System.out.println("No vendors found.");
                return;
            }

            // Display header
            System.out.printf("%-10s %-20s %-20s %-10s%n", "Vendor ID", "Vendor Name", "Item", "Price");

            // Iterate through the result set and display each vendor
            while (rs.next()) {
                String vendorId = rs.getString("vendor_id");
                String vendorName = rs.getString("vendor_name");
                String item = rs.getString("item");
                double itemPrice = rs.getDouble("item_price");

                System.out.printf("%-10s %-20s %-20s $%-10.2f%n", vendorId, vendorName, item, itemPrice);
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving the vendors.");
            
        }
    }

    // Display all vendors, and ask user to select a vendor_name to purchase from
    public static void purchaseFromVendor(Scanner scanner, Connection conn, String customerId) {
        try {
            String vendorName = null;
            String itemName = null;
            int quantity = 0;
            double totalPrice = 0;

            // Step 1: Display all vendors and prompt for a valid vendor name
            while (true) {
                System.out.println("Available Vendors:");
                String vendorQuery = "SELECT DISTINCT vendor_name FROM vendor ORDER BY vendor_name";

                try (PreparedStatement stmt = conn.prepareStatement(vendorQuery)) {
                    ResultSet rs = stmt.executeQuery();

                    if (!rs.isBeforeFirst()) {
                        System.out.println("No vendors available.");
                        return;
                    }

                    while (rs.next()) {
                        System.out.println(rs.getString("vendor_name"));
                    }
                }

                System.out.print("Which vendor do you want to visit (Type 'exit' to leave): ");
                vendorName = scanner.nextLine();
                if (vendorName.equalsIgnoreCase("exit")) {
                    System.out.println("Leaving from the vendors.");
                    return;
                }

                // Validate vendor exists
                String validateVendorQuery = "SELECT 1 FROM vendor WHERE vendor_name = ?";
                try (PreparedStatement validateStmt = conn.prepareStatement(validateVendorQuery)) {
                    validateStmt.setString(1, vendorName);
                    ResultSet validateRs = validateStmt.executeQuery();
                    if (validateRs.next()) {
                        break; // Valid vendor, proceed
                    } else {
                        System.out.println("Invalid vendor name. Please try again.");
                    }
                }
            }

            // Step 2: Display items and prompt for a valid item name
            while (true) {
                System.out.println("Available items from " + vendorName + ":");
                String itemQuery = "SELECT item, item_price FROM vendor WHERE vendor_name = ?";
                try (PreparedStatement itemStmt = conn.prepareStatement(itemQuery)) {
                    itemStmt.setString(1, vendorName);
                    ResultSet itemRs = itemStmt.executeQuery();

                    System.out.printf("%-20s %-10s%n", "Item", "Price");
                    while (itemRs.next()) {
                        System.out.printf("%-20s $%-10.2f%n", itemRs.getString("item"), itemRs.getDouble("item_price"));
                    }
                }

                System.out.print("What item do you want to buy (Type 'exit' to leave): ");
                itemName = scanner.nextLine();
                if (itemName.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting purchase process.");
                    return;
                }

                // Get the price of the selected item
                String itemPriceQuery = "SELECT item_price FROM vendor WHERE vendor_name = ? AND item = ?";
                try (PreparedStatement priceStmt = conn.prepareStatement(itemPriceQuery)) {
                    priceStmt.setString(1, vendorName);
                    priceStmt.setString(2, itemName);
                    ResultSet priceRs = priceStmt.executeQuery();
                    if (priceRs.next()) {
                        totalPrice = priceRs.getDouble("item_price");
                        break; // Valid item, proceed
                    } else {
                        System.out.println("Invalid item name. Please try again.");
                    }
                }
            }

            // Step 3: Prompt for a valid quantity
            while (true) {
                System.out.print("How many items do you want (Enter a number, or type 'exit' to leave): ");
                String quantityInput = scanner.nextLine();
                if (quantityInput.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting purchase process.");
                    return;
                }

                try {
                    quantity = Integer.parseInt(quantityInput);
                    if (quantity >= 1) {
                        totalPrice *= quantity;
                        break; // Valid quantity, proceed
                    } else {
                        System.out.println("Quantity must be at least 1. Please try again.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid number.");
                }
            }

            // Step 4: Prompt for a valid payment method
            while (true) {
                System.out.printf("The total price is: $%.2f%n", totalPrice);
                System.out.print("Do you want to pay with (1) Credit Card or (2) Debit Card? (Enter 1, 2, or type 'exit' to leave): ");
                String paymentInput = scanner.nextLine();
                if (paymentInput.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting purchase process.");
                    return;
                }

                try {
                    int paymentMethod = Integer.parseInt(paymentInput);
                    if (paymentMethod == 1) {
                        // Pay with credit card
                        System.out.print("Enter your Credit Card Number: ");
                        String cardNumber = scanner.nextLine();
                        System.out.print("Enter the Security Code: ");
                        String secCode = scanner.nextLine();

                        if (HelperFunctions.validateCreditCard(conn, customerId, cardNumber, secCode)) {
                            HelperFunctions.creditCardPayment(conn, cardNumber, totalPrice);
                            System.out.println("Payment successful! Thank you for your purchase.");
                            break;
                        } else {
                            System.out.println("Invalid credit card details. Please try again.");
                        }

                    } else if (paymentMethod == 2) {
                        // Pay with debit card
                        System.out.print("Enter the Account ID linked to the debit card: ");
                        String accountId = scanner.nextLine();
                        System.out.print("Enter your Debit Card Number: ");
                        String cardNumber = scanner.nextLine();
                        System.out.print("Enter the Security Code: ");
                        String secCode = scanner.nextLine();

                        if (HelperFunctions.validateDebitCard(conn, accountId, cardNumber, secCode)) {
                            HelperFunctions.debitCardPayment(conn, accountId, cardNumber, totalPrice);
                            System.out.println("Payment successful! Thank you for your purchase.");
                            break;
                        } else {
                            System.out.println("Invalid debit card details. Please try again.");
                        }

                    } else {
                        System.out.println("Invalid payment method. Please enter 1 or 2.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter 1 or 2.");
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred during the purchase process.");
        }
    }
}
