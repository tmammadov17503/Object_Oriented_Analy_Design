package OO.Assignment_2.tests;
import OO.Assignment_2.Storage;
import OO.Assignment_2.RingBuffer;
import OO.Assignment_2.ReaderCursor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// Tests for the ring buffer project — Storage, RingBuffer, and ReaderCursor
// InteractiveRingBuffer is a CLI app, so it's not unit tested here (see PR notes)
public class RingBufferTest {

    // ─────────────────────────────────────────────────
    //  Storage tests
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("Storage: basic store and read at same sequence")
    void storage_storeAndRead_basicCase() {
        Storage<String> s = new Storage<>(5);
        s.store(0, "hello");
        assertEquals("hello", s.read(0));
    }

    @Test
    @DisplayName("Storage: capacity is what you set it to")
    void storage_getCapacity_returnsCorrectValue() {
        Storage<Integer> s = new Storage<>(10);
        assertEquals(10, s.getCapacity());
    }

    @Test
    @DisplayName("Storage: modular index wraps around correctly")
    void storage_modularIndex_wrapsAround() {
        Storage<String> s = new Storage<>(3);
        // sequence 0 and 3 both map to index 0
        s.store(0, "first");
        s.store(3, "overwrite");
        // index 0 now holds "overwrite"
        assertEquals("overwrite", s.read(0));
        assertEquals("overwrite", s.read(3)); // same physical slot
    }

    @Test
    @DisplayName("Storage: reading empty slot returns null")
    void storage_readEmptySlot_returnsNull() {
        Storage<String> s = new Storage<>(5);
        assertNull(s.read(2)); // nothing was ever stored there
    }

    @Test
    @DisplayName("Storage: can store null explicitly")
    void storage_storeNull_works() {
        Storage<String> s = new Storage<>(3);
        s.store(0, null);
        assertNull(s.read(0)); // null is valid data here
    }

    @Test
    @DisplayName("Storage: each slot is independent")
    void storage_multipleSlots_dontInterfere() {
        Storage<String> s = new Storage<>(4);
        s.store(0, "a");
        s.store(1, "b");
        s.store(2, "c");
        assertEquals("a", s.read(0));
        assertEquals("b", s.read(1));
        assertEquals("c", s.read(2));
    }

    @Test
    @DisplayName("Storage: capacity of 1 stores and overwrites on sequence 0 and 1")
    void storage_capacityOne_alwaysSameSlot() {
        Storage<String> s = new Storage<>(1);
        s.store(0, "x");
        assertEquals("x", s.read(0));
        s.store(1, "y"); // sequence 1 % 1 = 0, overwrites
        assertEquals("y", s.read(0));
    }

    @Test
    @DisplayName("Storage: works with integer type too")
    void storage_genericType_integer() {
        Storage<Integer> s = new Storage<>(4);
        s.store(0, 42);
        assertEquals(42, s.read(0));
    }

    // ─────────────────────────────────────────────────
    //  RingBuffer tests
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("RingBuffer: write position starts at 0")
    void ringBuffer_initialWritePosition_isZero() {
        RingBuffer<String> rb = new RingBuffer<>(5);
        assertEquals(0L, rb.getWritePosition());
    }

    @Test
    @DisplayName("RingBuffer: write advances position by 1 each time")
    void ringBuffer_write_advancesPosition() {
        RingBuffer<String> rb = new RingBuffer<>(5);
        rb.write("a");
        assertEquals(1L, rb.getWritePosition());
        rb.write("b");
        assertEquals(2L, rb.getWritePosition());
    }

    @Test
    @DisplayName("RingBuffer: throws on capacity <= 0")
    void ringBuffer_invalidCapacity_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new RingBuffer<>(0));
        assertThrows(IllegalArgumentException.class, () -> new RingBuffer<>(-5));
    }

    @Test
    @DisplayName("RingBuffer: createReader starts at current write position")
    void ringBuffer_createReader_startsAtWritePosition() {
        RingBuffer<String> rb = new RingBuffer<>(5);
        rb.write("a"); // write pos is now 1
        ReaderCursor<String> r = rb.createReader();
        assertEquals(1L, r.getPosition()); // reader starts where writer is
    }

    @Test
    @DisplayName("RingBuffer: multiple readers are independent")
    void ringBuffer_multipleReaders_dontSharePosition() {
        RingBuffer<String> rb = new RingBuffer<>(5);
        rb.write("x");
        rb.write("y");

        ReaderCursor<String> r1 = rb.createReader();
        rb.write("z"); // only written after r1 is created
        ReaderCursor<String> r2 = rb.createReader();

        // r1 starts at 2, r2 starts at 3
        assertEquals(2L, r1.getPosition());
        assertEquals(3L, r2.getPosition());
    }

    @Test
    @DisplayName("RingBuffer: write then read gets correct value")
    void ringBuffer_writeAndRead_correctValue() {
        RingBuffer<String> rb = new RingBuffer<>(5);
        ReaderCursor<String> r = rb.createReader(); // starts at 0
        rb.write("hello");
        // wait, we need to read from position 0 — but createReader was BEFORE the write
        // so the reader's position is 0 and the item is at sequence 0
        // re-create reader at position 0 (before any writes)
        RingBuffer<String> rb2 = new RingBuffer<>(5);
        ReaderCursor<String> reader = rb2.createReader(); // pos 0
        rb2.write("hello"); // goes to sequence 0
        assertEquals("hello", reader.read());
    }

    @Test
    @DisplayName("RingBuffer: reader created before writes can read all items in order")
    void ringBuffer_readerBeforeWrites_readsInOrder() {
        RingBuffer<String> rb = new RingBuffer<>(10);
        ReaderCursor<String> reader = rb.createReader(); // starts at 0
        rb.write("one");
        rb.write("two");
        rb.write("three");

        assertEquals("one", reader.read());
        assertEquals("two", reader.read());
        assertEquals("three", reader.read());
    }

    @Test
    @DisplayName("RingBuffer: overwrite — slow reader gets new value at wrapped slot")
    void ringBuffer_overwrite_readerGetsFreshValue() {
        RingBuffer<String> rb = new RingBuffer<>(3);
        ReaderCursor<String> reader = rb.createReader(); // starts at 0

        rb.write("A"); // seq 0, index 0
        rb.write("B"); // seq 1, index 1
        rb.write("C"); // seq 2, index 2
        rb.write("D"); // seq 3, index 0 — overwrites A

        // reader hasn't moved yet, reads index 0 = "D" (A was overwritten)
        assertEquals("D", reader.read());
    }

    // ─────────────────────────────────────────────────
    //  ReaderCursor tests
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("ReaderCursor: position starts at given startSequence")
    void readerCursor_initialPosition_isStartSequence() {
        Storage<String> s = new Storage<>(5);
        ReaderCursor<String> r = new ReaderCursor<>(s, 3);
        assertEquals(3L, r.getPosition());
    }

    @Test
    @DisplayName("ReaderCursor: read advances position by 1")
    void readerCursor_read_advancesPosition() {
        Storage<String> s = new Storage<>(5);
        s.store(0, "item");
        ReaderCursor<String> r = new ReaderCursor<>(s, 0);
        r.read();
        assertEquals(1L, r.getPosition());
    }

    @Test
    @DisplayName("ReaderCursor: reads correct item from storage")
    void readerCursor_read_returnsCorrectItem() {
        Storage<String> s = new Storage<>(5);
        s.store(2, "target");
        ReaderCursor<String> r = new ReaderCursor<>(s, 2);
        assertEquals("target", r.read());
    }

    @Test
    @DisplayName("ReaderCursor: sequential reads go through items in order")
    void readerCursor_sequentialReads_inOrder() {
        Storage<String> s = new Storage<>(5);
        s.store(0, "first");
        s.store(1, "second");
        s.store(2, "third");

        ReaderCursor<String> r = new ReaderCursor<>(s, 0);
        assertEquals("first", r.read());
        assertEquals("second", r.read());
        assertEquals("third", r.read());
        assertEquals(3L, r.getPosition()); // ended up at 3
    }

    @Test
    @DisplayName("ReaderCursor: reading past write returns null (no data yet)")
    void readerCursor_readAhead_returnsNull() {
        Storage<String> s = new Storage<>(5);
        // nothing stored at position 4
        ReaderCursor<String> r = new ReaderCursor<>(s, 4);
        assertNull(r.read()); // nothing there, so null
    }

    @Test
    @DisplayName("ReaderCursor: two readers on same storage don't affect each other")
    void readerCursor_twoReaders_independentPositions() {
        Storage<String> s = new Storage<>(5);
        s.store(0, "alpha");
        s.store(1, "beta");

        ReaderCursor<String> r1 = new ReaderCursor<>(s, 0);
        ReaderCursor<String> r2 = new ReaderCursor<>(s, 0);

        r1.read(); // r1 moves to 1

        // r2 should still be at 0 and read "alpha"
        assertEquals(0L, r2.getPosition());
        assertEquals("alpha", r2.read());
    }

    // ─────────────────────────────────────────────────
    //  Edge cases and integrated scenarios
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("Edge: capacity 1 buffer — every write overwrites the only slot")
    void edge_capacityOne_continuousOverwrite() {
        RingBuffer<String> rb = new RingBuffer<>(1);
        ReaderCursor<String> reader = rb.createReader(); // pos 0

        rb.write("first");
        rb.write("second"); // overwrites first, both at index 0
        rb.write("third");  // overwrites again

        // reader is at pos 0, reads index 0 which holds "third"
        assertEquals("third", reader.read());
    }

    @Test
    @DisplayName("Edge: reading from an empty buffer returns null")
    void edge_emptyBuffer_readReturnsNull() {
        RingBuffer<String> rb = new RingBuffer<>(5);
        ReaderCursor<String> reader = rb.createReader(); // starts at 0
        // no writes happened, slot 0 is empty
        assertNull(reader.read());
    }

    @Test
    @DisplayName("Edge: large number of sequential writes and reads stay consistent")
    void edge_manyWrites_positionTrackedCorrectly() {
        RingBuffer<Integer> rb = new RingBuffer<>(10);
        ReaderCursor<Integer> reader = rb.createReader();

        int count = 50;
        for (int i = 0; i < count; i++) {
            rb.write(i);
        }

        // writer is at position 50
        assertEquals(50L, rb.getWritePosition());

        // reader hasn't moved, still at 0
        assertEquals(0L, reader.getPosition());

        // but since capacity is 10 and we wrote 50 items, positions 0-39 are overwritten
        // position 0 maps to index 0, which was last written with value 40
        assertEquals(40, reader.read()); // slot was overwritten
    }

    @Test
    @DisplayName("Edge: writer laps reader — reader gets overwritten value, not original")
    void edge_writerLapsReader_readerSeesNewValue() {
        RingBuffer<String> rb = new RingBuffer<>(3);
        ReaderCursor<String> reader = rb.createReader(); // starts at 0

        rb.write("A"); // seq 0 → index 0
        rb.write("B"); // seq 1 → index 1
        rb.write("C"); // seq 2 → index 2
        rb.write("D"); // seq 3 → index 0, overwrites A

        // reader is at 0 → index 0 → now has "D"
        String val = reader.read();
        assertEquals("D", val); // "A" is gone
    }

    @Test
    @DisplayName("Edge: null can be written and read back")
    void edge_writeNull_readBackNull() {
        RingBuffer<String> rb = new RingBuffer<>(5);
        ReaderCursor<String> reader = rb.createReader();
        rb.write(null); // null is technically allowed
        assertNull(reader.read());
    }

    // ─────────────────────────────────────────────────
    //  Concurrency tests (basic smoke tests)
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("Concurrency: single writer, multiple readers — no exceptions thrown")
    void concurrency_writerAndReaders_noExceptions() throws InterruptedException {
        RingBuffer<Integer> rb = new RingBuffer<>(100);

        // create 3 readers before writing starts
        List<ReaderCursor<Integer>> readers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            readers.add(rb.createReader());
        }

        int itemCount = 500;
        AtomicInteger errors = new AtomicInteger(0);

        // one writer thread
        Thread writer = new Thread(() -> {
            for (int i = 0; i < itemCount; i++) {
                rb.write(i);
            }
        });

        // three reader threads
        List<Thread> readerThreads = new ArrayList<>();
        for (ReaderCursor<Integer> reader : readers) {
            Thread t = new Thread(() -> {
                for (int i = 0; i < itemCount; i++) {
                    try {
                        reader.read(); // just read, don't assert value (may be overwritten)
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                }
            });
            readerThreads.add(t);
        }

        writer.start();
        readerThreads.forEach(Thread::start);

        writer.join(5000);
        for (Thread t : readerThreads) t.join(5000);

        assertEquals(0, errors.get(), "No exceptions expected during concurrent access");
    }

    @Test
    @DisplayName("Concurrency: write position ends up correct after concurrent writes")
    void concurrency_multipleWrites_writePositionAccurate() throws InterruptedException {
        // NOTE: the design says single writer — this tests that AtomicLong handles
        // two writer threads without data corruption on the counter itself
        RingBuffer<Integer> rb = new RingBuffer<>(1000);
        int writesPerThread = 500;

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < writesPerThread; i++) rb.write(i);
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < writesPerThread; i++) rb.write(i);
        });

        t1.start();
        t2.start();
        t1.join(5000);
        t2.join(5000);

        // AtomicLong guarantees 1000 total increments even with two threads
        assertEquals(1000L, rb.getWritePosition());
    }

}
