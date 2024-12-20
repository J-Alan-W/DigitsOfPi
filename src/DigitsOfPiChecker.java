/* -----------------------------------------------
 * Lead Contributor: J. Alan Wallace (alanwallaceii2_at_gmail.com)
 * Contributors: 
 * 
 * Last Updated: Dec 19, 2024
 * This project was developed as an activity of the Francis Marion University chapter of the Association for Computing Machinery.
 * Special Thanks go to the FMU Physics and Engineering department for their help and gracious use of the Patriot Supercomputing Cluster.
 * 
 * Purpose:
 * This class is designed to be used in conjunction with the DigitsOfPi.java file.
 * This class checks the given digits of pi against a reference value that was taken from https://www.piday.org/million/
 * 
 * IMPORTANT: This will only check the first 1 million digits of pi. 
 * After the one millionth place, the Reader will reach the end of the file.
 * -----------------------------------------------*/
import java.io.*;

public class DigitsOfPiChecker {
	File file = null;
	FileReader reader = null;
	
	DigitsOfPiChecker() {
	    // Attempt to initialize the File and FileReader
	    try {
	        this.file = new File("src/PI_REFERENCE.txt");
	        this.reader = new FileReader(file);
	    } catch (FileNotFoundException e) {
	        /* Yes, exiting on an error is pretty strict, 
	         * but it avoids more complicated error checking down the line*/
	        System.out.println(e.getLocalizedMessage());
	        System.exit(0);
	    } 
	} // end constructor
  
	public boolean check(String val) {
 	    // For each char in the string, check that it matches the next digit of pi.
	    for (int num : val.toCharArray()) {
	        try {
	            // .read() reads in one character at a time
	            int ref = reader.read();
	            // Return false if, at any point, val doesn't match the reference file.
	            if (num != ref) return false;
	                
	        } catch (Exception e) {
	            System.out.println(e.getLocalizedMessage());
	        }
	    }
	    return true;
	} // end check()
	
	public void close() {
	    // Close the Reader gracefully
        try {
            reader.close(); 
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
	} // end close()
	
} // end DigitsOfPiChecker
