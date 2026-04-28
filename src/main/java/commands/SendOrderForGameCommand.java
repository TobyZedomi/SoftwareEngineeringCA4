package commands;

import model.MatchResult;
import model.Order;
import model.OrderBook;
import model.OrderManager;
import service.UserUtilities;

import java.time.LocalDateTime;

public class SendOrderForGameCommand implements Command {

    private final OrderManager orderManager;
    private final String username;
    private String buyerOrSeller;
    private String title;
    private double price;
    private int quantity;
    private final LocalDateTime dateOfOrder;
    private MatchResult matchResult;
    private String response;

    private boolean result;

    public SendOrderForGameCommand(OrderManager orderManager, String username, String buyerOrSeller, String title, double price, int quantity) {
        this.orderManager = orderManager;
        this.username = username;
        this.buyerOrSeller = buyerOrSeller;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
        this.dateOfOrder = LocalDateTime.now();
    }
    @Override
    public void execute() {

        if (!buyerOrSeller.isEmpty()) {

            boolean checkIfBuyerOrSellerIsValid = orderManager.checkIfBuyerOrSellerIsValid(buyerOrSeller);

            if (checkIfBuyerOrSellerIsValid == true) {

                boolean checkIfTitleIsValid = orderManager.checkIfTitleIsValid(title);

                if (checkIfTitleIsValid == true) {

                    if (price >= 0) {

                        if (price == 0) {

                            boolean cancelled = orderManager.cancelOrder(username, buyerOrSeller, title);

                            if (cancelled == true) {
                                response = UserUtilities.CANCELLED + "%%Order cancelled successfully";
                                result = true;
                            } else {
                                response = UserUtilities.NOT_FOUND + "%%No matching order found to cancel";
                                result = false;
                            }
                        } else if (quantity >= 1) {

                            matchResult = orderManager.videoGameOrder(username, buyerOrSeller, title, price, quantity, dateOfOrder);

                            if (matchResult != null) {
                                response = UserUtilities.MATCH + "%%match";
                                result = true;
                            } else {
                                response = UserUtilities.ORDERS_RETRIEVED_SUCCESSFULLY + "%%Order placed successfully. No match found yet.";
                                result = true;
                            }
                        } else {
                            response = UserUtilities.QUANTITY_NOT_VALID + "%%Quantity must be at least 1";
                            result = false;
                        }
                    }else {
                        response = UserUtilities.PRICE_NOT_VALID + "%%Price cannot be a negative number";
                        result = false;
                    }
                } else {
                    response = UserUtilities.TITLE_EMPTY + "%%Invalid game title. Please enter a valid game title";
                    result = false;
                }

            } else {
                response = UserUtilities.BUYER_SELLER_NOT_VALID + "%%Buyer or seller must be 'B' for buyer or 'S' for seller";
                result = false;
            }

        } else {
            response =  UserUtilities.BUYER_SELLER_EMPTY + "%%Buyer or seller cannot be empty";
            result = false;
        }

    }

    public MatchResult getMatchResult() {
        return matchResult;
    }

    public String getResponse() {
        return response;
    }
}
