package OO.Assignment_2; // have your own package here 

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class InteractiveRingBuffer {
    
    private static RingBuffer<String> buffer; // the ring buffer 
    private static List<ReaderCursor<String>> readers = new ArrayList<>(); // list of readers 
    private static Scanner scanner = new Scanner(System.in); // input reader 
    private static int capacity = 0; // inital buffer size capacity is set to 0 
    
    public static void main(String[] args) { 
        System.out.println("Ring Buffer Program");
        System.out.println("Commands:");
        System.out.println("  capacity <N>  - Create buffer with capacity N");
        System.out.println("  write <item>  - Write item to buffer");
        System.out.println("  addreader     - Add a new reader");
        System.out.println("  read <N>      - Reader #N reads next item");
        System.out.println("  status        - Show buffer and reader status");
        System.out.println("  exit          - Quit");
        System.out.println();
        
        while (true) { // will stop only if we write exit 
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) continue;
            
            String[] parts = input.split(" ", 2);
            String command = parts[0].toLowerCase();
            String argument = parts.length > 1 ? parts[1] : "";
            
            switch (command) {
                case "capacity":
                    setCapacity(argument);
                    break;
                case "write":
                    writeItem(argument);
                    break;
                case "addreader":
                    addReader();
                    break;
                case "read":
                    readItem(argument);
                    break;
                case "status":
                    showStatus();
                    break;
                case "exit":
                case "quit":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Unknown command. Try: capacity, write, addreader, read, status, exit");
            }
        } // these are just commands and arguments 
    }
    
    private static void setCapacity(String arg) {
        try {
            int cap = Integer.parseInt(arg); // parsing of the "3" to 3
            if (cap <= 0) {
                System.out.println("Capacity must be positive!"); // check for positive 
                return;
            }
            capacity = cap; // saving of the capacity 
            buffer = new RingBuffer<>(cap); // create buffer
            readers.clear(); // mainly new buffer new reader 
            System.out.println("Created buffer with capacity " + cap);
        } catch (NumberFormatException e) {
            System.out.println("Usage: capacity <number>");
        }
    }
    
    private static void writeItem(String item) { // first check if the buffer exist 
        if (buffer == null) {
            System.out.println("Create buffer first! Use: capacity <N>");
            return;
        }
        if (item.isEmpty()) {
            System.out.println("Usage: write <item>"); // check if item is not empty
            return;
        }
        long pos = buffer.getWritePosition(); // before writing we haveto know where it will go
        buffer.write(item); // store item
        int index = (int) (pos % capacity); // calculate index 
        System.out.println("Wrote: \"" + item + "\" at sequence " + pos + " (index " + index + ")");
    }
    
    private static void addReader() {
        if (buffer == null) {
            System.out.println("Create buffer first! Use: capacity <N>"); // first again we check if buffer exists
            return;
        }
        ReaderCursor<String> reader = buffer.createReader(); // creation of reader 
        readers.add(reader); // adding 
        System.out.println("Added Reader #" + readers.size() + " (starts at position " + buffer.getWritePosition() + ")");
    }
    
    private static void readItem(String arg) {
        if (buffer == null) {
            System.out.println("Create buffer first!"); // again checking of buffer 
            return;
        }
        try {
            int readerNum = Integer.parseInt(arg) - 1; // again parsing 
            if (readerNum < 0 || readerNum >= readers.size()) {
                System.out.println("Invalid reader number. Use 1 to " + readers.size());
                return;
            }
            
            ReaderCursor<String> reader = readers.get(readerNum);
            long readFromPosition = reader.getPosition(); // here we get position before reading 
            String item = reader.read(); // and read
            
            if (item == null) {
                System.out.println("Reader #" + (readerNum + 1) + ": (empty - no data written yet or overwritten)");
            } else {
                System.out.println("Reader #" + (readerNum + 1) + ": \"" + item + "\" (from position " + readFromPosition + ")");
            }
        } catch (NumberFormatException e) {
            System.out.println("Usage: read <readerNumber>");
        }
    }
    
    private static void showStatus() {
        if (buffer == null) {
            System.out.println("No buffer created yet");
            return;
        }
        System.out.println("Buffer capacity: " + capacity);
        System.out.println("Next write position: " + buffer.getWritePosition());
        System.out.println("Number of readers: " + readers.size());
        for (int i = 0; i < readers.size(); i++) {
            System.out.println("  Reader #" + (i + 1) + " next read at position " + readers.get(i).getPosition());
        }
    } // just printing of current states when status command is written 
}
