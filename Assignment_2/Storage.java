package OO.Assignment_2; // have your own package here 
import java.util.concurrent.atomic.AtomicReferenceArray;

public class Storage<T> {
    private final int capacity;
    private final AtomicReferenceArray<T> buffer; // the actual buffer that will be holding values 

    public Storage(int capacity) { // again constructor just for the storage, so if we say constructor 3 it will be buffer size of 3
        this.capacity = capacity;
        this.buffer = new AtomicReferenceArray<>(capacity);
    }

    public void store(long sequence, T item) { // this is the main part it uses the sequence % capacity 
        int index = (int) (sequence % capacity);
        buffer.set(index, item);
    }

    public T read(long sequence) { // and this is for calculating index 
        int index = (int) (sequence % capacity);
        return buffer.get(index);
    }

    public int getCapacity() { // just returns capacity 
        return capacity;
    }
}