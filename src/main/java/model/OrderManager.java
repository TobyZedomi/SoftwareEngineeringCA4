package model;

import commands.CancelOrderCommand;
import commands.SubmitOrderCommand;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class OrderManager implements IOrderManager {
    private final OrderBookManager orderBookManager = OrderBookManager.getInstance();

    public OrderManager() {

    }


    public MatchResult videoGameOrder(String username, String buyerOrSeller, String title, double price, int quantity, LocalDateTime dateOfOrder) {

        OrderBook book = orderBookManager.getOrderBook(title);
        Order order = new Order(username, buyerOrSeller, title, price, quantity, dateOfOrder);

        SubmitOrderCommand command = new SubmitOrderCommand(book, order);
        command.execute();

        return command.getResult();
    }


    public boolean cancelOrder(String username, String buyerOrSeller, String title) {

        OrderBook book = orderBookManager.getOrderBook(title);
        boolean cancelled = false;

        if (book != null) {
            CancelOrderCommand command = new CancelOrderCommand(book, username, buyerOrSeller);
            command.execute();
            cancelled = command.getResult();
        }


        return cancelled;
    }


    public boolean checkIfPriceIsValid(double price) {
        boolean valid = false;

        if (price > 0) {
            valid = true;
        }

        return valid;
    }


    public boolean checkIfBuyerOrSellerIsValid(String buyerOrSeller) {
        boolean match = false;

        if (buyerOrSeller.equalsIgnoreCase("S") || buyerOrSeller.equalsIgnoreCase("B")) {

            match = true;
        }

        return match;
    }


    public boolean checkIfTitleIsValid(String title) {

        boolean valid = false;

        if (title != null && !title.isEmpty() && orderBookManager.checkIfBookExists(title)) {
            valid = true;
        }

        return valid;
    }


    public ArrayList<Order> getAllOrders() {

        ArrayList<Order> allOrders = new ArrayList<>();
        String[] titles = orderBookManager.getAllTitles();

        for (int i = 0; i < titles.length; i++) {
            OrderBook book = orderBookManager.getOrderBook(titles[i]);

            for (int j = 0; j < book.getBids().size(); j++) {
                allOrders.add(book.getBids().get(j));
            }

            for (int k = 0; k < book.getOffers().size(); k++) {
                allOrders.add(book.getOffers().get(k));
            }
        }

        return allOrders;
    }
}
