package commands;

import model.OrderBook;
import model.OrderManager;
import service.UserUtilities;

public class CancelOrderCommand implements Command{

    private final OrderManager orderManager;
    private final String username;
    private final String buyerOrSeller;
    private final String title;
    private boolean result;
    private String response;

   public CancelOrderCommand(OrderManager orderManager, String username, String buyerOrSeller, String title) {
        this.orderManager = orderManager;
        this.username = username;
        this.buyerOrSeller = buyerOrSeller;
        this.title = title;
    }

    @Override
    public void execute() {

        if (!buyerOrSeller.isEmpty()) {

            boolean checkIfBuyerOrSellerIsValid = orderManager.checkIfBuyerOrSellerIsValid(buyerOrSeller);

            if (checkIfBuyerOrSellerIsValid == true) {
                if (!title.isEmpty()) {

                    boolean cancelled = orderManager.cancelOrder(username, buyerOrSeller, title);

                    if (cancelled == true) {
                        response = UserUtilities.CANCELLED + "%%Order cancelled successfully";
                        result = true;
                    } else {
                        response = UserUtilities.NOT_FOUND + "%%No matching order found to cancel";
                        result = false;
                    }
                } else {
                    response = UserUtilities.TITLE_EMPTY + "%%Game title cannot be empty";
                    result = false;
                }

            } else {
                response = UserUtilities.BUYER_SELLER_NOT_VALID + "%%Buyer or seller must be 'B' for buyer or 'S' for seller";
                result = false;
            }


        } else {
            response = UserUtilities.BUYER_SELLER_EMPTY + "%%Buyer or seller cannot be empty";
            result = false;
        }
    }

    public boolean getResult() {
        return result;
    }

    public String getResponse() {
        return response;
    }

}
