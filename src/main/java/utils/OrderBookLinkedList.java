package utils;


import model.Order;

public class OrderBookLinkedList {

    private Node first;
    private int size;

    private final boolean descendingOrder;

    public OrderBookLinkedList( boolean descendingOrder) {
        first = null;
        this.descendingOrder = descendingOrder;
    }

    public boolean isEmpty(){

        return first == null;
    }

    public int size(){
        return size;
    }

    public void add (Order order){

        Node newNode = new Node(order);

        if(isEmpty() || shouldInsertBefore(order, first.data)){
            newNode.next = first;
            first = newNode;
        } else {
            Node current = first;
            while(current.hasNext() && !shouldInsertBefore(order, current.next.data)){
                current = current.next;
            }
            newNode.next = current.next;
            current.next = newNode;
        }

        size++;
    }

    private boolean shouldInsertBefore(Order newOrder, Order existingOrder){

        if (descendingOrder) {
            return newOrder.getPrice() > existingOrder.getPrice();
        } else {
            return newOrder.getPrice() < existingOrder.getPrice();
        }
    }

    public Order getFirst(){
        if(isEmpty()){
            throw new IllegalStateException("Order book is empty");
        }
        return first.data;
    }

    public Order removeFirst(){
        if(isEmpty()){
            throw new IllegalStateException("Order book is empty");
        }

        Order data = first.data;
        first = first.next;
        size--;
        return data;
    }

    public boolean removeByUsername(String username){
        if(isEmpty()){
            return false;
        }

        if(first.data.getUsername().equals(username)){
            first = first.next;
            size--;
            return true;
        }

        Node current = first;
        while(current.hasNext()){
            if(current.next.data.getUsername().equals(username)){
                current.next = current.next.next;
                size--;
                return true;
            }
            current = current.next;
        }

        return false;
    }


    public Order get(int pos){

        if(isEmpty() || pos < 0 || pos >= size){
            throw new IndexOutOfBoundsException("Position out of bounds");
        }

        if (pos == 0) {
            return first.data;
        }

        Node current = first;

        for (int i = 0; i < pos; i++) {
            current = current.next;
        }

        return current.data;
    }

    public Order[] toArray(){
        Order[] orders = new Order[size];
        Node current = first;

        for(int i = 0; i < size; i++){
            orders[i] = current.data;
            current = current.next;
        }
        return orders;
    }


    public static void bubbleSort(Order[] orders, boolean descendingOrder) {
        int n = orders.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {

                boolean shouldSwap;
                if(descendingOrder){
                    shouldSwap = orders[j].getPrice() < orders[j + 1].getPrice();
                } else {
                    shouldSwap = orders[j].getPrice() > orders[j + 1].getPrice();
                }

                if (shouldSwap){
                    Order temp = orders[j];
                    orders[j] = orders[j + 1];
                    orders[j + 1] = temp;
                }
            }
        }
    }

    private static class Node
    {
        private Order data;
        private Node next;

        public Node(Order data)
        {
            next = null;
            this.data = data;
        }

        public boolean hasNext()
        {
            return next != null;
        }
    }

}
