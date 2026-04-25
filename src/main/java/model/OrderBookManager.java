package model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderBookManager {

    private volatile static OrderBookManager instance;


    private Map<String, OrderBook> books = new ConcurrentHashMap<>();


    private OrderBookManager(){

        bootstrapOrderBooks();
    }

    public static OrderBookManager getInstance() {
        if (instance == null) {
            synchronized (OrderBookManager.class) {
                if (instance == null) {
                    instance = new OrderBookManager();
                }
            }
        }
        return instance;
    }

    public OrderBook getOrderBook(String title){
        return books.get(title);
    }


    public boolean checkIfBookExists(String title){
        boolean match = false;
        if(books.containsKey(title)) {
            match = true;
        }
        return match;
    }


    public void registerObserverWithAllBooks(OrderBookObserver observer){
        for(OrderBook book : books.values()){
            book.register(observer);
        }
    }

    public String[] getAllTitles(){
        String[] titles = new String[books.size()];
        int i = 0;
        for(String title : books.keySet()){
            titles[i] = title;
            i++;
        }
        return titles;
    }

    private void bootstrapOrderBooks(){
        books.put("GTA5", new OrderBook("GTA5"));
        books.put("NBA2K", new OrderBook("NBA2K"));
        books.put("Fortnite", new OrderBook("Fortnite"));
    }

}
