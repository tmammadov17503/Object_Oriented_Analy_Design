// package OO.Assignment_1; - insert yout own pakage here so it works properly

/*
 * Name: Taghi Mammadov
 * Project Name: Random Number Generator Analysis
 * Class: Object Oriented Analysis and Design
 * Date: 02/08/2026
 * Description: This program generates random numbers using three different Java approaches, 
 * mainly (java.util.Random, Math.random, ThreadLocalRandom) and calculates descriptive, 
 * and statistics (mean, standard deviation, min, max) to analyze their behavior.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Generator { // this is the main class

    private final int USE_RANDOM_CLASS = 1;
    private final int USE_MATH_RANDOM = 2;
    private final int USE_THREAD_LOCAL = 3; // this is mainly used to switch between generator types easily

    public ArrayList<Double> populate(int n, int randNumGen) {
        ArrayList<Double> list = new ArrayList<>();
        
        Random random = new Random(); // here I have created an instance of Random()

        for (int i = 0; i < n; i++) {
            double value = 0.0;

            if (randNumGen == USE_RANDOM_CLASS) {
                value = random.nextDouble(); // the first approach: java.util.Random
            } else if (randNumGen == USE_MATH_RANDOM) {
                value = Math.random(); // second: Math.random()
            } else if (randNumGen == USE_THREAD_LOCAL) {
                value = ThreadLocalRandom.current().nextDouble(); // third: ThreadLocalRandom
            }

            list.add(value);
        }
        return list;
    }

    public ArrayList<Double> statistics(ArrayList<Double> randomValues) {
        double n = randomValues.size();
        double sum = 0.0;
        double min = Double.MAX_VALUE; // so here we start high so the first number replaces it
        double max = Double.MIN_VALUE; // and here we start low so the first number replaces it

        for (Double val : randomValues) { // Basic calculation of sum, min, and max
            sum += val;
            if (val < min) min = val;
            if (val > max) max = val;
        }

        double mean = sum / n;

        double sumSquaredDiffs = 0.0;
        for (Double val : randomValues) { // Calculation of STD using this formula: sum of (value - mean)^2, divided by (n-1), then square root
            double diff = val - mean;
            sumSquaredDiffs += (diff * diff);
        }

        double variance = sumSquaredDiffs / (n - 1);
        double stdDev = Math.sqrt(variance);

        ArrayList<Double> stats = new ArrayList<>(); // Result list
        stats.add(n);
        stats.add(mean);
        stats.add(stdDev);
        stats.add(min);
        stats.add(max);

        return stats;
    }

    public void display(ArrayList<Double> results, boolean headerOn) {
        if (headerOn) {
            System.out.printf("%-10s %-10s %-10s %-10s %-10s%n", 
                "Sizes", "Mean", "StdDev", "Min", "Max");
        }

        System.out.printf("%-10.0f %-10.5f %-10.5f %-10.5f %-10.5f%n", // data of row formatted to 5 decimal places for precision
            results.get(0), // n
            results.get(1), // mean
            results.get(2), // stddev
            results.get(3), // min
            results.get(4)  // max
        );
    }

    public void execute() { // main logic method
        int[] sizes = {100, 1000, 100000}; // testing on 3 different sample sizes (small, medium, large)
        
        System.out.println("Generating Random Number Statistics...\n");

        for (int genType = 1; genType <= 3; genType++) { // looping through each generator type
            
            if (genType == USE_RANDOM_CLASS) System.out.println("Generator: java.util.Random");
            else if (genType == USE_MATH_RANDOM) System.out.println("Generator: Math.random()");
            else System.out.println("Generator: ThreadLocalRandom");

            for (int i = 0; i < sizes.length; i++) { // looping through each sample size for this generator
                int n = sizes[i];

                ArrayList<Double> data = populate(n, genType); // populate data
                ArrayList<Double> stats = statistics(data); // calculate stats
            
                boolean showHeader = (i == 0); // results - only the header for the first row of each block
                display(stats, showHeader);
            }
            System.out.println(); 
        }
    }

    public static void main(String[] args) { // main method 
        Generator g = new Generator();
        g.execute();
    }
}