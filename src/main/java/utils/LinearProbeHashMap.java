package utils;

public class LinearProbeHashMap<K,V> {

    private int size;

    private Entry<K,V>[] data;

    private int capacity = 10;



    // default constructor with a capacity of 10

    public LinearProbeHashMap() {
        data = (Entry<K,V>[]) new Entry[capacity];
        size = 0;
    }

    // put a key value pair into the map

    public V put(K key, V value){


        int hash = Math.abs(key.hashCode());

        int destinationIndex = hash % data.length;

        if (data[destinationIndex] ==  null){

            Entry<K,V> newEntry = new Entry<>(key, value);

            data[destinationIndex] = newEntry;

            size++;

            return null;
        }else if (data[destinationIndex].getKey().equals(key)){

            V oldValue = data[destinationIndex].getValue();

            data[destinationIndex].setValue(value);

            return oldValue;
        } else {

            int probeIndex = destinationIndex + 1;

            if (probeIndex == data.length) {
                probeIndex = 0;
            }

            while (probeIndex != destinationIndex) {
                if (data[probeIndex] == null) {
                    Entry<K,V> newEntry = new Entry<>(key, value);
                    data[probeIndex] = newEntry;
                    size++;
                    return null;
                } else if (data[probeIndex].getKey().equals(key)) {
                    V oldValue = data[probeIndex].getValue();
                    data[probeIndex].setValue(value);
                    return oldValue;
                }else if (probeIndex == data.length - 1) {
                    probeIndex = 0;
                } else {
                    probeIndex++;
                }

            }

            grow();
            return put(key, value);
        }
    }


    // get the value associate dwith the key

    public V get(K key){

        int hash = Math.abs(key.hashCode());

        int destinationIndex = hash % data.length;

        if (data[destinationIndex] == null){
            return null;
        } else if (data[destinationIndex].getKey().equals(key)){
            return data[destinationIndex].getValue();
        } else {
            int probeIndex = destinationIndex + 1;

            if (probeIndex == data.length) {
                probeIndex = 0;
            }

            while (probeIndex != destinationIndex) {
                if (data[probeIndex] == null) {
                    return null;
                } else if (data[probeIndex].getKey().equals(key)) {
                    return data[probeIndex].getValue();
                } else if (probeIndex == data.length - 1) {
                    probeIndex = 0;
                } else {
                    probeIndex++;
                }
            }
            return null;
        }
    }


    // check if map contains a key

    public boolean containsKey(K key){
        return get(key) != null;
    }

    // get all the keys stored in the map

    public Object[] keySet(){
        Object[] keys = new Object[size];
        int index = 0;

        for (int i =0; i < data.length; i++){
            if (data[i] != null){
                keys[index] = data[i].getKey();
                index++;
            }
        }

        return keys;
    }

    // get the amount f key pairs in the map
    public int size(){
        return size;
    }

    // double capacity of map when ful
    private void grow(){

        Entry<K,V>[] oldData = data;
        capacity = capacity * 2;

        data = (Entry<K,V>[]) new Entry[capacity];

        size = 0;

        for (int i =0; i < oldData.length; i++){
            if (oldData[i] != null){
                put(oldData[i].getKey(), oldData[i].getValue());
            }
        }
    }
    private static class Entry<K,V> {
       private final K key;
       private V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }
}
