package utils;

public class DynamicArrayList<T> {

    private Object[] store;

    private int num_elements;
    private int capacity = 10;
    private int expansionFactor = 10;


    // make the length of the array store 10 and capacity is 10

    public DynamicArrayList() {
        capacity = 10;
        store = new Object[capacity];
    }

    // added constructor

    public DynamicArrayList(int expansionFactor) {
        if (expansionFactor == 0 || expansionFactor < 0) {
            throw new IllegalArgumentException("Out of Bounds");
        }
        capacity = expansionFactor;
        store = new Object[expansionFactor];
    }

    // get how much elements ate in the array

    public int size() {
        return num_elements;
    }


    // check if array is empty

    public boolean isEmpty() {
        return num_elements == 0;
    }


    // get the value in the array at a cerain position

    public T get(int pos) {
        if (isEmpty() || pos < 0 || pos >= num_elements) {
            throw new IndexOutOfBoundsException("Position out of bounds");
        }
        return (T) store[pos];
    }


    // get the first position of a value in the array

    public int indexOf(T value) {
        for (int i = 0; i < num_elements; i++) {
            if (store[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    // add a value in the array

    public boolean add (T value) {

        boolean found = false;

        if (num_elements == store.length) {

            Object[] duplicatedData = new Object[store.length + expansionFactor];

            System.arraycopy(store, 0, duplicatedData, 0, num_elements);

            store = duplicatedData;

            found = true;
        }

        store[num_elements] = value;

        num_elements++;

        return found;
    }


    /// remove the first occurrence of a value from the array


    public boolean remove(T value) {

       boolean found = false;

        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }


        for (int i = 0; i < num_elements; i++) {
            if (store[i].equals(value)) {
                for (int j = i; j < num_elements - 1; j++) {
                    store[j] = store[j + 1];
                }
                store[num_elements - 1] = null;
                num_elements--;
                found = true;
                break;
            }
        }

        return found;
    }


    // check if array contains a value
    public boolean contains(T value) {
        for (int i = 0; i < num_elements; i++) {
            if (store[i].equals(value)) {
                return true;
            }
        }
        return false;
    }


    // set array length to 0

    public void clear() {
        num_elements = 0;
    }

}
