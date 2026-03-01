package OO.Assignment_2; // have your own package here 
import java.util.concurrent.atomic.AtomicLong;

public class RingBuffer<T> {
    private final Storage<T> storage; // array that will store the data
    private final AtomicLong writeSequence; // this is the counter for the position 

    public RingBuffer(int capacity) { // our main constructor
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.storage = new Storage<>(capacity); // create storage 
        this.writeSequence = new AtomicLong(0); // and initialization of the coutner 
    }

    public void write(T item) {
        long sequence = writeSequence.getAndIncrement(); // just incremets starts with 0 then 1 and etc
        storage.store(sequence, item); // storage at the sequence number 
    }

    public ReaderCursor<T> createReader() { // gets current write position and the creates ReaderCursor and will start reading from strt position 
        return new ReaderCursor<>(storage, writeSequence.get());
    }

    public long getWritePosition() { // just returns current write position 
        return writeSequence.get();
    }
}