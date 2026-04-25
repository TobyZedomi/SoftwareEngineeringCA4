package model;

import utils.OrderBookLinkedList;

import java.util.ArrayList;

public class OrderBook  {

    private final ArrayList<OrderBookObserver> observers = new ArrayList<>();

    private final String gameTitle;
    private final OrderBookLinkedList bids;

    private final OrderBookLinkedList offers;


    public OrderBook(String gameTitle) {
        this.gameTitle = gameTitle;
        this.bids = new OrderBookLinkedList(true);
        this.offers = new OrderBookLinkedList(false);
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public OrderBookLinkedList getBids() {
        return bids;
    }

    public OrderBookLinkedList getOffers() {
        return offers;
    }

    public boolean register(OrderBookObserver observer) {
        synchronized (observers){
            if(observer != null && !observers.contains(observer)){
                observers.add(observer);
                return true;
            }

            return false;
        }
    }

    public boolean unregister(OrderBookObserver observer) {
        synchronized (observers){
            if(observer != null && observers.contains(observer)){
                observers.remove(observer);
                return true;
            }

            return false;
        }
    }


    private void notifyObservers() {
        synchronized (observers){
            observers.stream().forEach((o) ->
            {
                o.update(this);
            });
        }
    }


    public synchronized MatchResult submitOrder(Order order) {
        if (order.getBuyerOrSeller().equalsIgnoreCase("B")){

            bids.removeByUsername(order.getUsername());
            bids.add(order);
        }else if(order.getBuyerOrSeller().equalsIgnoreCase("S")){
            offers.removeByUsername(order.getUsername());
            offers.add(order);
        }

        MatchResult matchResult = attemptMatch();
        printBook();
        notifyObservers();

        return matchResult;
    }

    public synchronized boolean cancelOrder(String username, String buyerOrSeller){

        boolean cancelled = false;

        if (buyerOrSeller.equalsIgnoreCase("B")) {
            cancelled = bids.removeByUsername(username);
        }
        else if (buyerOrSeller.equalsIgnoreCase("S")) {
            cancelled = offers.removeByUsername(username);
        }

        if (cancelled){
            printBook();
            notifyObservers();
        }

        return cancelled;
    }

    private MatchResult attemptMatch(){

        if(bids.isEmpty() || offers.isEmpty()){
            return null;
        }

        Order bestBid = bids.getFirst();
        Order bestOffer = offers.getFirst();


        if(bestBid.getPrice() >= bestOffer.getPrice()){

            int tradeQuantity = Math.min(bestBid.getQuantity(), bestOffer.getQuantity());
            double tradePrice = bestOffer.getPrice();

            bestBid.setQuantity(bestBid.getQuantity() - tradeQuantity);
            bestOffer.setQuantity(bestOffer.getQuantity() - tradeQuantity);


            if (bestBid.getQuantity() == 0) {
                bids.removeFirst();
            }

            if(bestOffer.getQuantity() == 0) {
                offers.removeFirst();
            }

            return new MatchResult(bestBid.getBuyerOrSeller(), bestOffer.getTitle(), tradePrice, tradeQuantity, bestOffer.getUsername());
        }

        return null;
    }


    public Double getBestBidPrice() {
       if(bids.isEmpty()){
           return null;
       }

       return bids.getFirst().getPrice();
    }

    public Double getBestOfferPrice() {
        if(offers.isEmpty()){
            return null;
        }

        return offers.getFirst().getPrice();
    }

    public void printBook() {
        System.out.println("Order Book: " + gameTitle);
        System.out.printf("Bids", "Offers");


        Order[] sortedBids = bids.toArray();
        Order[] sortedOffers = offers.toArray();

        OrderBookLinkedList.bubbleSort(sortedBids, true);
        OrderBookLinkedList.bubbleSort(sortedOffers, false);

        int maxRows = Math.max(sortedBids.length, sortedOffers.length);

        for (int i = 0; i < maxRows; i++)
        {
            String bidStr = "";
            String offerStr = "";

            if (i < sortedBids.length)
            {
                bidStr = sortedBids[i].getPrice() + " (qty:" + sortedBids[i].getQuantity() + ")";
            }

            if (i < sortedOffers.length)
            {
                offerStr = sortedOffers[i].getPrice() + " (qty:" + sortedOffers[i].getQuantity() + ")";
            }

            System.out.printf(bidStr, offerStr);
        }
        System.out.println();
    }



}
