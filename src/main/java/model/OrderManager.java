package model;

import commands.CancelOrderCommand;
import commands.SendOrderForGameCommand;
import utils.DynamicArrayList;

import java.time.LocalDateTime;

public class OrderManager implements IOrderManager {
    private final OrderBookManager orderBookManager = OrderBookManager.getInstance();

    public OrderManager() {

    }


    public MatchResult videoGameOrder(String username, String buyerOrSeller, String title, double price, int quantity, LocalDateTime dateOfOrder) {

        OrderBook book = orderBookManager.getOrderBook(title);
        Order order = new Order(username, buyerOrSeller, title, price, quantity, dateOfOrder);

       MatchResult matchResult = book.submitOrder(order);

        return matchResult;
    }


    public boolean cancelOrder(String username, String buyerOrSeller, String title) {

        OrderBook book = orderBookManager.getOrderBook(title);
        boolean cancelled = false;

        if (book != null) {
            cancelled = book.cancelOrder(username, buyerOrSeller);
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


    public DynamicArrayList<Order> getAllOrders() {

        DynamicArrayList<Order> allOrders = new DynamicArrayList<>();
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
