package commands;

import model.MatchResult;
import model.Order;
import model.OrderBook;

public class SubmitOrderCommand implements Command {

    private final OrderBook orderBook;
    private final Order order;

    private MatchResult result;

    public SubmitOrderCommand(OrderBook orderBook, Order order) {
        this.orderBook = orderBook;
        this.order = order;
    }

    @Override
    public void execute() {
        result = orderBook.submitOrder(order);
    }

    public MatchResult getResult() {
        return result;
    }
}
