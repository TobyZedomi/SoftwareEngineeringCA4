package model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public interface IOrderManager {

    MatchResult videoGameOrder(String username, String buyerOrSeller, String title, double price, int quantity, LocalDateTime dateOfOrder);

    boolean cancelOrder (String username, String buyerOrSeller, String title);

    boolean checkIfPriceIsValid(double price);

    boolean checkIfBuyerOrSellerIsValid(String buyerOrSeller);

    boolean checkIfTitleIsValid(String title);
    public ArrayList<Order> getAllOrders();
}
