package model;

import utils.LinearProbeHashMap;

public class OrderBookManager {

    private volatile static OrderBookManager instance;

    private LinearProbeHashMap<String, OrderBook> books = new LinearProbeHashMap<>();


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
        Object[] titles = books.keySet();

        for (int i = 0; i < titles.length; i++) {
            if (titles[i] !=null) {
                books.get((String) titles[i]).register(observer);
            }
        }
    }

    public String[] getAllTitles(){
        Object[] keys = books.keySet();
        String[] titles = new String[keys.length];

        for (int i = 0; i < keys.length; i++){
            if(keys[i] != null){
                titles[i] = (String) keys[i];
            }
        }
        return titles;
    }

    private void bootstrapOrderBooks(){
        books.put("GTA5", new OrderBook("GTA5"));
        books.put("NBA2K", new OrderBook("NBA2K"));
        books.put("Fortnite", new OrderBook("Fortnite"));
    }

}
