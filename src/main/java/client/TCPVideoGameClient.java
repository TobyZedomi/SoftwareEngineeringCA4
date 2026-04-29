package client;

import network.TCPNetworkLayer;

import java.io.IOException;
import java.util.Scanner;


public class TCPVideoGameClient {

    private static volatile boolean loggedIn = false;
    private static volatile boolean responseReceived = false;

    public static void main(String[] args) {

        try {

            TCPNetworkLayer networkLayer = new TCPNetworkLayer(AuthUtils.HOSTNAME, AuthUtils.PORT);
            networkLayer.connect();

            Scanner keyboard = new Scanner(System.in);

            Thread listenerThread = new Thread(() -> {

                try {
                    while (true) {
                        String response = networkLayer.receive();
                        handleAllServerMessages(response);
                    }
                } catch (Exception e) {
                    System.out.println("Connection to server lost.");
                    System.exit(0);
                }
            });

            listenerThread.setDaemon(true);
            listenerThread.start();

            boolean running = true;

            while (running) {

                if (loggedIn == false) {
                    displayAuthMenu();
                    int choice = getNumber(keyboard);

                    switch (choice) {

                        case 0:
                            networkLayer.send(AuthUtils.EXIT);
                            running = false;
                            break;
                        case 1:
                            responseReceived = false;
                            networkLayer.send(generateLoginMessage(keyboard));
                            while (responseReceived == false) {
                            }
                            break;
                        case 2:
                            responseReceived = false;
                            networkLayer.send(generateRegisterMessage(keyboard));
                            while (responseReceived == false) {
                            }
                            break;
                        default:
                            System.out.println("Please use an option from the menu");
                            break;
                    }
                } else {

                    displayMenu();
                    int choice = getNumber(keyboard);

                    switch (choice) {
                        case 0:
                            networkLayer.send(AuthUtils.EXIT);
                            running = false;
                            break;
                        case 1:
                            responseReceived = false;
                            networkLayer.send(generateBidMessage(keyboard));
                            while (responseReceived == false) {
                            }
                            break;
                        case 2:
                            responseReceived = false;

                            networkLayer.send(generateOfferMessage(keyboard));
                            while (responseReceived == false) {
                            }
                            break;
                        case 3:
                            responseReceived = false;

                            networkLayer.send(generateCancelMessage(keyboard));
                            while (responseReceived == false) {
                            }
                            break;
                        case 4:
                            responseReceived = false;

                            networkLayer.send(AuthUtils.VIEW);
                            while (responseReceived == false) {
                            }
                            break;
                        case 5:
                            responseReceived = false;

                            networkLayer.send(AuthUtils.END);
                            while (responseReceived == false) {
                            }
                            loggedIn = false;
                            break;
                        default:
                            System.out.println("Please use an option from the menu");
                            break;
                    }
                }
            }

            networkLayer.disconnect();
            System.out.println("Thank you for using the video game order system");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void displayAuthMenu() {
        System.out.println("Please select an option:");
        System.out.println("0. Exit");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("Enter choice: ");
    }

    public static void displayMenu() {
        System.out.println("Please select an option:");
        System.out.println("0. Exit");
        System.out.println("1. Place a bid");
        System.out.println("2. Place an offer");
        System.out.println("3. Cancel an order");
        System.out.println("4. View orders");
        System.out.println("5. Logout");
        System.out.println("Enter choice: ");
    }

    public static int getNumber(Scanner keyboard) {
        boolean numberEntered = false;
        int number = 0;
        while (!numberEntered) {
            try {
                number = Integer.parseInt(keyboard.nextLine());
                numberEntered = true;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number");
            }
        }
        return number;
    }

    public static void handleAllServerMessages(String message) {

        String[] parts = message.split("%%");
        String type = parts[0];
        String content = message;

        if (parts.length > 1) {
            content = parts[1];
        }

        switch (type) {

            case AuthUtils.LOGIN_SUCCESSFUL:
                loggedIn = true;
                System.out.println("Login successful");
                responseReceived = true;
                break;
            case AuthUtils.REGISTER_SUCCESSFUL:
                loggedIn = true;
                System.out.println("Registration successful");
                responseReceived = true;
                break;
            case AuthUtils.MARKET_UPDATE:
                System.out.println("Market update: " + parts[1] + " at " + parts[2]);
                break;
            case AuthUtils.MATCH:
                System.out.println(content);
                responseReceived = true;
                break;
            case AuthUtils.ORDERS_RETRIEVED_SUCCESSFULLY:
                String[] orders = parts[1].split("##");

                for (int i = 0; i < orders.length; i++) {
                    System.out.println(" " + orders[i]);
                }
                responseReceived = true;
                break;
            default:
                if (!content.isEmpty()) {
                    System.out.println(content);
                } else {
                    System.out.println(message);
                }
                responseReceived = true;
                break;

        }

    }

    public static String generateLoginMessage(Scanner keyboard) {
        System.out.println("Please enter your username:");
        String username = keyboard.nextLine();
        System.out.println("Please enter your password:");
        String password = keyboard.nextLine();
        return AuthUtils.LOGIN + "%%" + username + "%%" + password;
    }

    public static String generateRegisterMessage(Scanner keyboard) {
        System.out.println("Please enter your username:");
        String username = keyboard.nextLine();
        System.out.println("Please enter your password:");
        String password = keyboard.nextLine();
        System.out.println("Please confirm your password:");
        String confirmPassword = keyboard.nextLine();
        return AuthUtils.REGISTER + "%%" + username + "%%" + password + "%%" + confirmPassword;
    }

    public static String generateBidMessage(Scanner keyboard) {
        System.out.println("Please enter the title of the game you want to buy (The Only Games Available Are GTA5, NBA2K or Fortnite):");
        String title = keyboard.nextLine();
        System.out.println("Please enter the price you are willing to pay (PUT Price as 0 to cancel a previous Order):");
        String price = keyboard.nextLine();
        System.out.println("Please enter the quantity you want to buy:");
        String quantity = keyboard.nextLine();
        return AuthUtils.ORDER + "%%B%%" + title + "%%" + price + "%%" + quantity;
    }


    public static String generateOfferMessage(Scanner keyboard) {
        System.out.println("Please enter the title of the game you want to sell (The Only Games Available Are GTA5, NBA2K or Fortnite):");
        String title = keyboard.nextLine();
        System.out.println("Please enter the price you want to sell for (PUT Price as 0 to cancel a previous Order):");
        String price = keyboard.nextLine();
        System.out.println("Please enter the quantity you want to sell:");
        String quantity = keyboard.nextLine();
        return AuthUtils.ORDER + "%%S%%" + title + "%%" + price + "%%" + quantity;
    }

    public static String generateCancelMessage(Scanner keyboard) {
        System.out.println("Please enter the order type (B for bid, S for offer) of the order you want to cancel:");
        String orderType = keyboard.nextLine();
        System.out.println("Enter game title (GTA5, NBA2K, Fortnite):");
        String title = keyboard.nextLine();
        return AuthUtils.CANCEL + "%%" + orderType + "%%" + title;
    }
}
