package commands;

import model.OrderBook;

public class CancelOrderCommand implements Command{

    private final OrderBook orderBook;
    private final String username;
    private final String buyerOrSeller;
    private boolean result;

    public CancelOrderCommand(OrderBook orderBook, String username, String buyerOrSeller) {
        this.orderBook = orderBook;
        this.username = username;
        this.buyerOrSeller = buyerOrSeller;
    }

    @Override
    public void execute() {
        result = orderBook.cancelOrder(username, buyerOrSeller);
    }

    public boolean getResult() {
        return result;
    }

}
