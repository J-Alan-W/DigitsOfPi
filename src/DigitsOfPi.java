/* -----------------------------------------------
 * Lead Contributor: J. Alan Wallace (alanwallaceii2_at_gmail.com)
 * Contributors: 
 * 
 * Last Updated: Dec 19, 2024
 * This project was developed as an activity of the Francis Marion University chapter of the Association for Computing Machinery.
 * Special Thanks go to the FMU Physics and Engineering department for their help and gracious use of the Patriot Supercomputing Cluster.
 * 
 * Purpose:
 * This program uses an implementation of Plouffe's Algorithm for calculating digits of pi.
 * Some of the code was adapted from a C implementation of the algorithm by Fabrice Bellard: https://bellard.org/pi/pi.c
 * Plouffe's Algorithm is a spigot algorithm, meaning it does not rely on the value of the n-1 digit to calculate the nth digit of pi.
 * For more information on Plouffe's algorithm or Bellard's implementation, see bellard.org/pi/
 * 
 * Notes:
 * The program is multithreaded, and will adjust the number of threads based on the number of CPU cores.
 * There is a faster variant of Plouffe's algorithm (in term of computation time), that uses a formula
 *    developed by Gosper in 1974. This program could be converted to use that formula,
 *    which would save computation time for large values of N. See https://bellard.org/pi/pi1.c
 * -----------------------------------------------*/
import java.util.concurrent.*;

public class DigitsOfPi {

    // Calculate N digits of pi
    public static final Integer N = 1000;
    
    public static void main(String[] args) {
        // Gets the number of available processor cores
        int maxThreadsPossible = Runtime.getRuntime().availableProcessors();
        // Establish ExecutorService with a threadpool equal to the number of available cores
        ExecutorService executor = Executors.newFixedThreadPool(maxThreadsPossible);
        
        /* Map to store the output of each calculation. 
         * Key: nth position of pi. Value: A Future<NthDigitOfPi> object that contains the n...n+8 digits of pi.*/
        ConcurrentSkipListMap<Integer, Future<NthDigitOfPi>> cslm = new ConcurrentSkipListMap<Integer, Future<NthDigitOfPi>>();
        
        // Before the threads begin, note the current system time so that the total runtime may be calculated later.
        long startTime = System.currentTimeMillis();
        
        /* Create a thread for each digit of pi.
         * (this implementation of Plouffe's algorithm actually returns n...n+8 digits, which is why the step of the loop is 9)
         * This thread is submitted to the ExecutorService, and the NthDigitOfPi object is also put into the map.
         * Technically, until the thread finished the Future object will not have the correct value */
        for (int i = 1; i < N; i+=9) {
            cslm.put(i, executor.submit(new NthDigitOfPi(i)));
        }
        /* shutdown() closes the ExecutorService in a orderly manner.
         * No new threads can be submitted, but the ExecutorService is still active as long 
         * as at least one thread has not finished.
         */
        executor.shutdown();
        
        // Waits for all threads to finish before moving on to non-multithreaded section.
        while (!executor.isTerminated()) {
            Thread.yield();
        }
        
        // Output the total time since the multithreading section began
        long multithreadTime = System.currentTimeMillis() - startTime;
        System.out.println("Multithreading section over! Time elapsed: " + multithreadTime + " ms");
        // Note the current time so later we can calculate the runtime of the non-multithreaded section
        long nonMultithreadStartTime = System.currentTimeMillis();
        
        // Print leading digit and radix point
        System.out.print("3.");
        
        /* This section checks the output of the code to ensure the accuracy of the program's calculations.
         * This utilizes the DigitsOfPiChecker class in a separate file.*/
        DigitsOfPiChecker checker = new DigitsOfPiChecker();
        Integer chunk = -1;
        for (Future<NthDigitOfPi> future : cslm.values()) {
            try {
                // Gets the value from the Future<NthDigitOfPi> object.
                chunk = future.get().getResult();
            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
            }
            
            // Makes each value chunk 9 digits long, and adds leading zeroes if necessary
            String stringWithLeadZeroes = String.format("%09d", chunk);
            // Checks the value chunk against the reference file
            if (!checker.check(stringWithLeadZeroes)) {
                // If the digits do not match in any way, indicate so in the output.
                System.out.print("\n The next 9 digits are wrong ->" + stringWithLeadZeroes);
            } else {
                // Else print out the chunk normally
                System.out.print(stringWithLeadZeroes);
            }
        }
        
//        // Print the ordered pairs in the map
//        System.out.println("\n"+cslm.toString()); // DEBUG
        
        // Output the total time since the non-multithreading section began
        long nonMultithreadTime = System.currentTimeMillis() - nonMultithreadStartTime;
        System.out.println("\nNon-Multithreading section over! Time elapsed: " + nonMultithreadTime + " ms");
        // Output total run time
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("Total Time Elapsed: " + totalTime + " ms");
        
    } // end main
} // end DigitsOfPi

class NthDigitOfPi implements Callable<NthDigitOfPi>{
    private int n;
    private int result = -1;
    
    public NthDigitOfPi (int n) {
        if ((n <= 0) || (n > Integer.MAX_VALUE-20)) {
            throw new RuntimeException("Invalid Value Provided for NthDigitOfPi.n");
        }
        this.n = n;
    } // end constructor
    
    public int getN() {
        return this.n;
    } // end getter
    
    public int getResult() {
        return this.result;
    } // end getter
    
    
    @Override
    public NthDigitOfPi call() {
        /* This method implements the call() method from the Callable interface.
         * This code is adapted from https://bellard.org/pi/pi.c (see header comment for more info)
         * This is an implementation of Plouffe's Algorithm for calculating the nth digit of pi
         * without knowing the previous digits of pi.*/
        
        //System.out.println("Thread started " + this.n); // DEBUG
        
        int av, a, vmax, N, num, den, k, kq, kq2, t, v, s, i;
        double sum;


        N = (int) ((this.n + 20) * Math.log(10) / Math.log(2));

        sum = 0;

        for (a = 3; a <= (2 * N); a = next_prime(a)) {

        vmax = (int) (Math.log(2 * N) / Math.log(a));
        av = 1;
        for (i = 0; i < vmax; i++)
            av = av * a;

        s = 0;
        num = 1;
        den = 1;
        v = 0;
        kq = 1;
        kq2 = 1;

        for (k = 1; k <= N; k++) {

            t = k;
            if (kq >= a) {
            do {
                t = t / a;
                v--;
            } while ((t % a) == 0);
            kq = 0;
            }
            kq++;
            num = mul_mod(num, t, av);

            t = (2 * k - 1);
            if (kq2 >= a) {
            if (kq2 == a) {
                do {
                t = t / a;
                v++;
                } while ((t % a) == 0);
            }
            kq2 -= a;
            }
            den = mul_mod(den, t, av);
            kq2 += 2;

            if (v > 0) {
            t = inv_mod(den, av);
            t = mul_mod(t, num, av);
            t = mul_mod(t, k, av);
            for (i = v; i < vmax; i++)
                t = mul_mod(t, a, av);
            s += t;
            if (s >= av)
                s -= av;
            }

        }

        t = pow_mod(10, this.n - 1, av);
        s = mul_mod(s, t, av);
        sum = fmod(sum + (double) s / (double) av, 1.0);
        }
        
        //System.out.println("Decimal digits of pi at position " + this.n + ": " + sum);
        //System.out.printf("Decimal digits of pi at position %d: %09d\n", this.n, (int) (sum * 1e9));
        //System.out.println("Thread finished " + this.n);
        this.result = (int) (sum * 1e9);
        return this;
        
    } // end run()
    
    /* return the inverse of x mod y */
    public static int inv_mod(int x, int y) {
        // Adapted from https://bellard.org/pi/pi.c (see header comment)
        int q, u, v, a, c, t;
        
        u = x;
        v = y;
        c = 1;
        a = 0;
        do {
        q = v / u;

        t = c;
        c = a - q * c;
        a = t;

        t = u;
        u = v - q * u;
        v = t;
        } while (u != 0);
        a = a % y;
        if (a < 0)
        a = y + a;
        return a;
    } // end inv_mod
    
    /* return (a^b) mod m */
    public static int pow_mod(int a, int b, int m) {
        // Adapted from https://bellard.org/pi/pi.c (see header comment)
        int r, aa;

        r = 1;
        aa = a;
        while (true) {
        if ((b & 1)==1)
            r = mul_mod(r, aa, m);
        b = b >> 1;
        if (b == 0)
            break;
        aa = mul_mod(aa, aa, m);
        }
        return r;
    } // end pow_mod
    
    /* return true if n is prime */
    public static boolean is_prime(int n) {
        // Adapted from https://bellard.org/pi/pi.c (see header comment)
        int r, i;
        if ((n % 2) == 0)
        return false;

        r = (int) (Math.sqrt(n));
        for (i = 3; i <= r; i += 2)
        if ((n % i) == 0)
            return false;
        return true;
    } //  end is_prime

    /* return the prime number immediatly after n */
    public static int next_prime(int n) {
        // Adapted from https://bellard.org/pi/pi.c (see header comment)
        do {
        n++;
        } while (!is_prime(n));
        return n;
    } // end next_prime
    
    public static int mul_mod(int a, int b, int m) {
        // Adapted from https://bellard.org/pi/pi.c (see header comment)
        long along, blong;
        along = (long)a;
        blong = (long)b;
        return (int)((along*blong) %m);
    } // end mul_mod
    
    public static double fmod(double a, double b) {
        // Adapted from https://bellard.org/pi/pi.c (see header comment)
        return a % b;
    } // end fmod
    
}// End NthDigitOfPi