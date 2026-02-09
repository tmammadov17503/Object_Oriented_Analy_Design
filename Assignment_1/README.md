# Random Number Generator Analysis

## Project Description
This Java project is an assignment for the **Object-Oriented Analysis and Design** course. It analyzes the performance and statistical behavior of three different random number generators available in the Java language:

1. `java.util.Random`
2. `Math.random()`
3. `java.util.concurrent.ThreadLocalRandom`

The program generates random samples of varying sizes (n = 100, 1,000, 100,000) and calculates descriptive statistics for each, including:
* **Mean**: Should approach 0.5
* **Standard Deviation**: Should approach ~0.29
* **Minimum**: Should approach 0.0
* **Maximum**: Should approach 1.0

## Files in this Repository
* `Generator.java`: The main source code containing the class definition, logic, and main method.
* `README.md`: This documentation file.

## How to Run
### Using the Command Line / Terminal:
1.  **Compile** the Java file:
    ```bash
    javac Generator.java
    ```

2.  **Run** the program:
    ```bash
    java Generator
    ```

## Expected Output
The program will output three tables (one for each generator type). Each table displays the statistical results for different sample sizes.

**Example of the output format:**
```text
Generator: java.util.Random
Sizes    Mean       StdDev     Min        Max       
---------------------------------------------------------
100        0.51432    0.28120    0.01234    0.98765   
1000       0.49812    0.28910    0.00123    0.99981   

100000     0.50015    0.28870    0.00001    0.99999
