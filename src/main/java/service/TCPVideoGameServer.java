package service;

import com.google.gson.Gson;
import model.*;
import network.TCPNetworkLayer;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.StringJoiner;

public class TCPVideoGameServer implements Runnable, OrderBookObserver {


    private final Socket clientDataSocket;

    public static ArrayList<TCPVideoGameServer> clients = new ArrayList<>();

    private final TCPNetworkLayer networkLayer;
    private final UserManager userManager;
    private final OrderManager orderManager;
    private final Gson gson = new Gson();

    private String username;
    private boolean loginStatus;

    public TCPVideoGameServer(Socket clientDataSocket, UserManager userManager, OrderManager orderManager) throws IOException {
        this.clientDataSocket = clientDataSocket;
        this.networkLayer = new TCPNetworkLayer(clientDataSocket);
        this.userManager = userManager;
        this.orderManager = orderManager;
        this.username = null;
        this.loginStatus = false;
    }


    @Override
    public void run() {

        boolean validClientSession = true;

        try {
            while (validClientSession) {

                String request = networkLayer.receive();
                System.out.println("Request: " + request);

                String response = null;
                String[] parts = request.split("%%");
                String action = parts[0];

                switch (action) {

                    case UserUtilities.REGISTER:
                        response = registerUser(parts);
                        break;
                    case UserUtilities.LOGIN:
                        response = loginUser(parts);
                        break;
                    case UserUtilities.ORDER:
                        response = sendOrderForGame(parts);
                        break;
                    case UserUtilities.CANCEL:
                        response = cancelOrder(parts);
                        break;
                    case UserUtilities.VIEW:
                        response = viewAllOrders();
                        break;
                    case UserUtilities.END:
                        response = UserUtilities.END + "%%Logged out successfully";
                        loginStatus = false;
                        removeClient();
                        break;
                    case UserUtilities.EXIT:
                        response = UserUtilities.EXIT + "%%Goodbye";
                        break;
                    default:
                        response = UserUtilities.INVALID + "%%Invalid request";
                        break;
                }

                if (response == null) {

                    response = UserUtilities.INVALID + "%%Invalid request";
                }

                networkLayer.send(response);

            }

            networkLayer.disconnect();
            removeClient();
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
            try {
                networkLayer.disconnect();
            } catch (IOException ex) {
                System.out.println("Error disconnecting client: " + ex.getMessage());
            }
            removeClient();
        }
    }


    //update book

    @Override
    public void update(OrderBook orderBook) {
        String bestBid = orderBook.getBestBidPrice() == null ? "NONE" : String.valueOf(orderBook.getBestBidPrice());
        String bestOffer = orderBook.getBestOfferPrice() == null ? "NONE" : String.valueOf(orderBook.getBestOfferPrice());

        String marketUpdate = UserUtilities.MARKET_UPDATE + "%%" + orderBook.getGameTitle() + "%%" + "Best Bid: " + bestBid + " | Best Offer: " + bestOffer;

        synchronized (clients) {
            for (int i = 0; i < clients.size(); i++) {
                clients.get(i).sendToClient(marketUpdate);
            }
        }
    }


    private String registerUser(String[] parts) {

        String response = null;

        if (parts.length != 4) {
            return UserUtilities.INVALID + "%%Invalid registration request format";
        }

        String username = parts[1];
        String password = parts[2];
        String confirmPassword = parts[3];

        boolean success = userManager.registerUser(username, password);

        if (!username.isEmpty()) {
            if (!password.isEmpty()) {
                if (!confirmPassword.isEmpty()) {

                    boolean checkIfUserExist = userManager.checkIfUserExist(username);

                    if (checkIfUserExist == false) {

                        boolean checkIfPasswordsMatch = userManager.checkIfPasswordsAreTheSame(password, confirmPassword);

                        if (checkIfPasswordsMatch == true) {
                            boolean checkPasswordFormat = userManager.checkIfPasswordsMatchRegex(password, confirmPassword);
                            if (checkPasswordFormat == true) {

                                userManager.registerUser(username, password);
                                this.username = username;
                                this.loginStatus = true;
                                addClient();

                                response = UserUtilities.REGISTER_SUCCESSFUL + "%%Registration successful. Welcome " + username;

                            } else {
                                response = UserUtilities.INVALID + "%%Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one digit, and one special character";
                            }
                        } else {
                            response = UserUtilities.PASSWORDS_DONT_MATCH + "%%Passwords do not match";
                        }

                    } else {
                        response = UserUtilities.USER_ALREADY_EXIST + "%%Username already exists";
                    }

                } else {
                    response = UserUtilities.INVALID + "%%Confirm password cannot be empty";
                }

            } else {
                response = UserUtilities.INVALID + "%%Password cannot be empty";
            }
        } else {
            response = UserUtilities.INVALID + "%%Username cannot be empty";
        }

        return response;
    }


    private String loginUser(String[] parts) {

        String response = null;

        if (parts.length != 3) {
            return UserUtilities.INVALID + "%%Invalid login request format";
        }

        String username = parts[1];
        String password = parts[2];

        if (!username.isEmpty()) {
            if (!password.isEmpty()) {

                boolean loginSuccess = userManager.loginUser(username, password);

                if (loginSuccess == true) {
                    this.username = username;
                    this.loginStatus = true;
                    addClient();
                    response = UserUtilities.LOGIN_SUCCESSFUL + "%%Login successful. Welcome back " + username;
                } else {
                    response = UserUtilities.LOGIN_FAILED + "%%Invalid username or password";
                }

            } else {
                response = UserUtilities.INVALID + "%%Password cannot be empty";
            }
        } else {
            response = UserUtilities.INVALID + "%%Username cannot be empty";
        }


        return response;
    }

    private String sendOrderForGame(String[] parts) {

        String response = null;

        if (loginStatus == false) {
            return UserUtilities.NOT_LOGGED_IN + "%%You must be logged in to place an order";
        }

        if (parts.length < 5) {
            return UserUtilities.INVALID + "%%Invalid order request format";
        }


        try {

            String buyerOrSeller = parts[1];
            String title = parts[2];
            String price = parts[3];
            int quantity = Integer.parseInt(parts[4]);
            LocalDateTime dateOfOrder = LocalDateTime.now();

            if (!buyerOrSeller.isEmpty()) {

                boolean checkIfBuyerOrSellerIsValid = orderManager.checkIfBuyerOrSellerIsValid(buyerOrSeller);

                if (checkIfBuyerOrSellerIsValid == true) {

                    boolean checkIfTitleIsValid = orderManager.checkIfTitleIsValid(title);

                    if (checkIfTitleIsValid == true) {

                        if (quantity >= 1) {

                            MatchResult matchResult = orderManager.videoGameOrder(username, buyerOrSeller, title, Double.parseDouble(price), Integer.parseInt(parts[4]), dateOfOrder);

                            if (matchResult != null) {
                                String counterPartyMessage = UserUtilities.MATCH + "%%You SOLD " + matchResult.getQuantity() + "x " + matchResult.getTitle() + " @ " + matchResult.getPrice() + " to " + username;
                                sendMatch(matchResult.getCounterParty(), counterPartyMessage);

                                response = UserUtilities.MATCH + "%%You BOUGHT " + matchResult.getQuantity() + "x " + matchResult.getTitle() + " @ " + matchResult.getPrice() + " from " + matchResult.getCounterParty();
                                System.out.println("Trade matched: " + username + " with " + matchResult.getCounterParty());
                            } else {
                                ArrayList<Order> allOrders = orderManager.getAllOrders();
                                response = serializeOrders(allOrders);
                            }
                        } else {
                            response = UserUtilities.QUANTITY_NOT_VALID + "%%Quantity must be at least 1";
                        }

                    } else {
                        response = UserUtilities.TITLE_EMPTY + "%%Invalid game title. Please enter a valid game title";
                    }

                } else {
                    return UserUtilities.BUYER_SELLER_NOT_VALID + "%%Buyer or seller must be 'B' for buyer or 'S' for seller";
                }

            } else {
                return UserUtilities.BUYER_SELLER_EMPTY + "%%Buyer or seller cannot be empty";
            }

        } catch (NumberFormatException e) {
            response = UserUtilities.NON_NUMERIC_ID + "%%Price and quantity must be valid numbers";
        }

        return response;
    }


    private String cancelOrder(String[] parts) {

        String response = null;

        if (loginStatus == false) {
            return UserUtilities.NOT_LOGGED_IN + "%%You must be logged in to cancel an order";
        }

        if (parts.length != 3) {
            return UserUtilities.INVALID + "%%Invalid cancel request format";
        }

        String buyerOrSeller = parts[1];
        String title = parts[2];

        if (!buyerOrSeller.isEmpty()) {

            boolean checkIfBuyerOrSellerIsValid = orderManager.checkIfBuyerOrSellerIsValid(buyerOrSeller);

            if (checkIfBuyerOrSellerIsValid == true) {
                if (!title.isEmpty()) {

                    boolean cancelled = orderManager.cancelOrder(username, buyerOrSeller, title);

                    if (cancelled == true) {
                        response = UserUtilities.CANCELLED + "%%Order cancelled successfully";
                    } else {
                        response = UserUtilities.NOT_FOUND + "%%No matching order found to cancel";
                    }
                } else {
                    response = UserUtilities.TITLE_EMPTY + "%%Game title cannot be empty";
                }

            } else {
                response = UserUtilities.BUYER_SELLER_NOT_VALID + "%%Buyer or seller must be 'B' for buyer or 'S' for seller";
            }


        } else {
            response = UserUtilities.BUYER_SELLER_EMPTY + "%%Buyer or seller cannot be empty";
        }


        return response;
    }


    private String viewAllOrders() {

        String response = null;

        if (loginStatus == false) {
            return UserUtilities.NOT_LOGGED_IN + "%%You must be logged in to view orders";
        }


        ArrayList<Order> allOrders = orderManager.getAllOrders();

        if (!allOrders.isEmpty()) {
            response = serializeOrders(allOrders);

        } else {
            response = UserUtilities.YOU_HAVE_NO_ORDERS + "%%No orders found";
        }

        return response;
    }


    private String serializeOrders(ArrayList<Order> orders) {

        StringJoiner joiner = new StringJoiner("##");

        for (int i = 0; i < orders.size(); i++) {

            Order order = orders.get(i);
            joiner.add("BuyerOrSeller: " + order.getBuyerOrSeller() + ", Title: " + order.getTitle() + ", Price: " + order.getPrice() + ", Quantity: " + order.getQuantity() + ", Username: " + order.getUsername());
        }

        return UserUtilities.ORDERS_RETRIEVED_SUCCESSFULLY + "%%" + joiner.toString();
    }


    // clients connected

    private synchronized void addClient() {
        if (!clients.contains(this)) {
            clients.add(this);
        }
    }

    private synchronized void removeClient() {
        clients.remove(this);
    }

    private synchronized void sendToClient(String message) {

        networkLayer.send(message);
    }


    private void sendMatch(String username, String message) {
        synchronized (clients) {

            for (int i = 0; i < clients.size(); i++) {
                TCPVideoGameServer client = clients.get(i);
                if (client.username != null && client.username.equals(username)) {
                    client.sendToClient(message);
                    break;
                }
            }
        }
    }

}
