package commands;

import model.Order;
import model.OrderManager;
import service.UserUtilities;
import utils.DynamicArrayList;

import java.util.StringJoiner;

public class ViewOrdersCommand implements Command{


    public final OrderManager orderManager;
    private String response;

    public ViewOrdersCommand(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    @Override

    public void execute() {

        DynamicArrayList<Order> allOrders = orderManager.getAllOrders();

        if (!allOrders.isEmpty()) {
            response = serializeOrders(allOrders);

        } else {
            response = UserUtilities.YOU_HAVE_NO_ORDERS + "%%No orders found";
        }
    }

    private String serializeOrders(DynamicArrayList<Order> orders) {

        StringJoiner joiner = new StringJoiner("##");

        for (int i = 0; i < orders.size(); i++) {

            Order order = orders.get(i);
            joiner.add("BuyerOrSeller: " + order.getBuyerOrSeller() + ", Title: " + order.getTitle() + ", Price: " + order.getPrice() + ", Quantity: " + order.getQuantity() + ", Username: " + order.getUsername());
        }

        return UserUtilities.ORDERS_RETRIEVED_SUCCESSFULLY + "%%" + joiner.toString();
    }


    public String getResponse() {
        return response;
    }


}
