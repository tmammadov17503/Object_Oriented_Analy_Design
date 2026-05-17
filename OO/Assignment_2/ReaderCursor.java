package OO.Assignment_2; // have your own package here 

import java.util.concurrent.atomic.AtomicLong;

public class ReaderCursor<T> { 
    private final Storage<T> storage;
    private final AtomicLong readSequence; // for the current position reader 

    public ReaderCursor(Storage<T> storage, long startSequence) { //again constructor 
        this.storage = storage; // just storage 
        this.readSequence = new AtomicLong(startSequence); // start position for the reader 
    }

    public T read() {
        long currentRead = readSequence.getAndIncrement(); // this si the main inrementation part for the position 
        return storage.read(currentRead);
    }
    
    public long getPosition() { // returns where reader will be reading next
        return readSequence.get();
    }
}
