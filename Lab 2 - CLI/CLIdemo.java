import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.SavingsAccount;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.io.File;

import org.jline.reader.*;
import org.jline.reader.impl.completer.*;
import org.jline.utils.*;
import org.fusesource.jansi.*;

public class CLIdemo {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private String[] commandsList;

    public void init() {
        commandsList = new String[]{"help", "customers", "customer", "exit"};
    }

    public void run() {
        AnsiConsole.systemInstall(); // needed to support ansi on Windows cmd
        printWelcomeMessage();
        LineReaderBuilder readerBuilder = LineReaderBuilder.builder();
        List<Completer> completors = new LinkedList<Completer>();

        completors.add(new StringsCompleter(commandsList));
        readerBuilder.completer(new ArgumentCompleter(completors));

        LineReader reader = readerBuilder.build();

        String line;
        PrintWriter out = new PrintWriter(System.out);

        while ((line = readLine(reader, "")) != null) {
            if ("help".equals(line)) {
                printHelp();
            } else if ("customers".equals(line)) {
                AttributedStringBuilder a = new AttributedStringBuilder()
                        .append("\nThis is all of your ")
                        .append("customers", AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                        .append(":");

                System.out.println(a.toAnsi());
                if (Bank.getNumberOfCustomers() > 0) {
                    System.out.println("\nLast name\tFirst Name\tBalance");
                    System.out.println("---------------------------------------");
                    for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                        System.out.println(Bank.getCustomer(i).getLastName() + "\t\t" + Bank.getCustomer(i).getFirstName() + "\t\t$" + Bank.getCustomer(i).getAccount(0).getBalance());
                    }
                } else {
                    System.out.println(ANSI_RED+"Your bank has no customers!"+ANSI_RESET);
                }

            } else if ("report".equals(line)) { // Нова команда
                printCustomerReport();
            } else if (line.indexOf("customer") != -1) {
                try {
                    int custNo = 0;
                    if (line.length() > 8) {
                        String strNum = line.split(" ")[1];
                        if (strNum != null) {
                            custNo = Integer.parseInt(strNum);
                        }
                    }                    
                    Customer cust = Bank.getCustomer(custNo);
                    String accType = cust.getAccount(0) instanceof CheckingAccount ? "Checkinh" : "Savings";
                    
                    AttributedStringBuilder a = new AttributedStringBuilder()
                            .append("\nThis is detailed information about customer #")
                            .append(Integer.toString(custNo), AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                            .append("!");

                    System.out.println(a.toAnsi());
                    
                    System.out.println("\nLast name\tFirst Name\tAccount Type\tBalance");
                    System.out.println("-------------------------------------------------------");
                    System.out.println(cust.getLastName() + "\t\t" + cust.getFirstName() + "\t\t" + accType + "\t$" + cust.getAccount(0).getBalance());
                } catch (Exception e) {
                    System.out
                        .println(ANSI_RED + "ERROR! Wrong customer number!" + ANSI_RESET);
                }
            } else if ("exit".equals(line)) {
                System.out.println("Exiting application");
                return;
            } else {
                System.out
                        .println(ANSI_RED + "Invalid command, For assistance press TAB or type \"help\" then hit ENTER." + ANSI_RESET);
            }
        }

        AnsiConsole.systemUninstall();
    }

    private void printWelcomeMessage() {
        System.out
                .println("\nWelcome to " + ANSI_GREEN + " MyBank Console Client App" + ANSI_RESET + "! \nFor assistance press TAB or type \"help\" then hit ENTER.");

    }

    private void printHelp() {
        System.out.println("help\t\t\t- Show help");
        System.out.println("customer\t\t- Show list of customers");
        System.out.println("customer 'index'\t- Show customer details");
        System.out.println("report\t\t\t- Generate customer report");
        System.out.println("exit\t\t\t- Exit the app");
    }

    private String readLine(LineReader reader, String promtMessage) {
        try {
            String line = reader.readLine(promtMessage + ANSI_YELLOW + "\nbank> " + ANSI_RESET);
            return line.trim();
        } catch (UserInterruptException e) {
            // e.g. ^C
            return null;
        } catch (EndOfFileException e) {
            // e.g. ^D
            return null;
        }
    }

    private void loadCustomersFromFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println(ANSI_RED + "File not found: " + fileName + ANSI_RESET);
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            int numberOfCustomers = Integer.parseInt(br.readLine().trim()); // Перше число — кількість клієнтів
            System.out.println("Number of customers: " + numberOfCustomers);

            for (int i = 0; i < numberOfCustomers; i++) {
                String line = br.readLine();
                if (line == null || line.trim().isEmpty()) {
                    System.out.println(ANSI_RED + "Error: Missing customer data at line " + (i + 2) + ANSI_RESET);
                    continue;
                }

                String[] customerInfo = line.split("\t"); // Читаємо інформацію про клієнта
                if (customerInfo.length < 3) {
                    System.out.println(ANSI_RED + "Error: Invalid customer format at line " + (i + 2) + ANSI_RESET);
                    continue;
                }

                String firstName = customerInfo[0].trim();
                String lastName = customerInfo[1].trim();
                int numberOfAccounts = Integer.parseInt(customerInfo[2].trim());

                System.out.println("Adding customer: " + firstName + " " + lastName + " with " + numberOfAccounts + " accounts.");
                Bank.addCustomer(firstName, lastName);

                // Читаємо інформацію про рахунки клієнта
                for (int j = 0; j < numberOfAccounts; j++) {
                    line = br.readLine();
                    if (line == null || line.trim().isEmpty()) {
                        System.out.println(ANSI_RED + "Error: Missing account data for customer " + firstName + " " + lastName + ANSI_RESET);
                        continue;
                    }

                    String[] accountInfo = line.split("\t");
                    if (accountInfo.length < 3) {
                        System.out.println(ANSI_RED + "Error: Invalid account format for customer " + firstName + " " + lastName + ANSI_RESET);
                        continue;
                    }

                    String accountType = accountInfo[0].trim();
                    double balance = Double.parseDouble(accountInfo[1].trim());

                    if ("S".equalsIgnoreCase(accountType)) { // Savings Account
                        double interestRate = Double.parseDouble(accountInfo[2].trim());
                        System.out.println("Adding SavingsAccount with balance: " + balance + " and interest rate: " + interestRate);
                        Bank.getCustomer(i).addAccount(new SavingsAccount(balance, interestRate));
                    } else if ("C".equalsIgnoreCase(accountType)) { // Checking Account
                        double overdraftLimit = Double.parseDouble(accountInfo[2].trim());
                        System.out.println("Adding CheckingAccount with balance: " + balance + " and overdraft limit: " + overdraftLimit);
                        Bank.getCustomer(i).addAccount(new CheckingAccount(balance, overdraftLimit));
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println(ANSI_RED + "Error reading file: " + e.getMessage() + ANSI_RESET);
        }
    }

    private void printCustomerReport() {
        System.out.println("\nCUSTOMER REPORT");
        System.out.println("-------------------------------");

        for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
            Customer customer = Bank.getCustomer(i);
            System.out.println("Customer: " + customer.getLastName() + ", " + customer.getFirstName());

            for (int j = 0; j < customer.getNumberOfAccounts(); j++) {
                String accountType = customer.getAccount(j) instanceof CheckingAccount ? "Checking Account" : "Savings Account";
                System.out.println("    " + accountType + ": balance = $" + customer.getAccount(j).getBalance());
            }
        }
    }

    public static void main(String[] args) {
        CLIdemo shell = new CLIdemo();
        shell.init();

        // Замість статичного додавання клієнтів
        shell.loadCustomersFromFile("F:/jline-34-rronik3/data/test.dat");

        shell.run();
    }
}
