package service;

import commands.*;
import model.*;
import network.TCPNetworkLayer;
import utils.DynamicArrayList;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.StringJoiner;

public class TCPVideoGameServer implements Runnable, OrderBookObserver {


    private final Socket clientDataSocket;
    public static DynamicArrayList<TCPVideoGameServer> clients = new DynamicArrayList<>();
    private final TCPNetworkLayer networkLayer;
    private final UserManager userManager;
    private final OrderManager orderManager;
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

        String bestBid;
        String bestOffer;

        if (orderBook.getBestBidPrice() == null) {
            bestBid = "NONE";
        } else {
            bestBid = String.valueOf(orderBook.getBestBidPrice());
        }

        if (orderBook.getBestOfferPrice() == null) {
            bestOffer = "NONE";
        } else {
            bestOffer = String.valueOf(orderBook.getBestOfferPrice());
        }

        String marketUpdate = UserUtilities.MARKET_UPDATE + "%%" + orderBook.getGameTitle() + "%%" + "Best Bid: " + bestBid + " | Best Offer: " + bestOffer;

        sendToClient(marketUpdate);

    }


    private String registerUser(String[] parts) {

        if (parts.length != 4) {
            return UserUtilities.INVALID + "%%Invalid registration request format";
        }

        RegisterCommand command = new RegisterCommand(userManager, parts[1], parts[2], parts[3]);
        command.execute();

        if (command.getResult() == true) {
            this.username = parts[1];
            this.loginStatus = true;
            addClient();
            OrderBookManager.getInstance().registerObserverWithAllBooks(this);
        }

        return command.getResponse();
    }


    private String loginUser(String[] parts) {

        if (parts.length != 3) {
            return UserUtilities.INVALID + "%%Invalid login request format";
        }

        LoginCommand command = new LoginCommand(userManager, parts[1], parts[2]);
        command.execute();

        if (command.getResult() == true) {
            this.username = parts[1];
            this.loginStatus = true;
            addClient();
            OrderBookManager.getInstance().registerObserverWithAllBooks(this);
        }

        return command.getResponse();
    }

    private String sendOrderForGame(String[] parts) {

        if (loginStatus == false) {
            return UserUtilities.NOT_LOGGED_IN + "%%You must be logged in to place an order";
        }

        if (parts.length < 5) {
            return UserUtilities.INVALID + "%%Invalid order request format";
        }


        try {

            String buyerOrSeller = parts[1];
            String title = parts[2];
            double price = Double.parseDouble(parts[3]);
            int quantity = Integer.parseInt(parts[4]);
            LocalDateTime dateOfOrder = LocalDateTime.now();


            SendOrderForGameCommand command = new SendOrderForGameCommand(orderManager, username, buyerOrSeller, title, price, quantity);
            command.execute();

            if (command.getMatchResult() != null) {
                MatchResult matchResult = command.getMatchResult();

                if (buyerOrSeller.equalsIgnoreCase("B")) {
                    String counterPartyMessage = UserUtilities.MATCH + "%%You Sold " + matchResult.getQuantity() + "x " + matchResult.getTitle() + " at a price of: " + matchResult.getPrice() + " to " + username;
                    sendMatch(matchResult.getCounterParty(), counterPartyMessage);
                    return UserUtilities.MATCH + "%%You BOUGHT " + matchResult.getQuantity() + "x " + matchResult.getTitle() + " at a price of " + matchResult.getPrice() + " from " + matchResult.getCounterParty();
                } else {

                    String counterPartyMessage = UserUtilities.MATCH + "%%You Bought " + matchResult.getQuantity() + "x " + matchResult.getTitle() + " at a price of: " + matchResult.getPrice() + " from " + username;
                    sendMatch(matchResult.getBidOwner(), counterPartyMessage);
                    return UserUtilities.MATCH + "%%You SOLD " + matchResult.getQuantity() + "x " + matchResult.getTitle() + " at a price of " + matchResult.getPrice() + " to " + matchResult.getBidOwner();
                }

            }

            return command.getResponse();

        } catch (NumberFormatException e) {
            return  UserUtilities.NON_NUMERIC_ID + "%%Price and quantity must be valid numbers";
        }

    }


    private String cancelOrder(String[] parts) {

        if (loginStatus == false) {
            return UserUtilities.NOT_LOGGED_IN + "%%You must be logged in to cancel an order";
        }

        if (parts.length != 3) {
            return UserUtilities.INVALID + "%%Invalid cancel request format";
        }

        CancelOrderCommand command = new CancelOrderCommand(orderManager, username, parts[1], parts[2]);
        command.execute();

        return command.getResponse();
    }


    private String viewAllOrders() {

        if (loginStatus == false) {
            return UserUtilities.NOT_LOGGED_IN + "%%You must be logged in to view orders";
        }

        ViewOrdersCommand command = new ViewOrdersCommand(orderManager);
        command.execute();

        return command.getResponse();
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
                if (client.username != null && client.username.equalsIgnoreCase(username)) {
                    client.sendToClient(message);
                    break;
                }
            }
            for (int i = 0; i < clients.size(); i++) {
                System.out.println("Client " + i + ": " + clients.get(i).username);
            }
        }
    }

}
