/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package paillierp.testingPaillier;

import java.math.BigInteger;
import paillierp.key.KeyGen;
import sun.security.util.BigInt;

/**
 *
 * @author Hamza
 */
public class testUtils {
public static void main(String[] args) {
 //   System.out.println(modAdd(BigInteger.ONE.multiply(BigInteger.valueOf(31)), BigInteger.ONE, BigInteger.ONE.multiply(BigInteger.valueOf(32))));
 //   System.out.println(modMult(BigInteger.ONE.multiply(BigInteger.valueOf(31)), BigInteger.valueOf(2), BigInteger.valueOf(32)));
   // System.out.println(BigInteger.valueOf(6).modInverse(BigInteger.valueOf(6)));
  
    long startInstant;

         startInstant= System.nanoTime();

        System.out.println(KeyGen.factorial(16000));
         System.out.println(System.nanoTime()-startInstant);
   
}
public static BigInteger modAdd(BigInteger a, BigInteger b, BigInteger n)
        {
            BigInteger c=a.add(b);
            
            if (c.compareTo(n)!=-1)                    
                return (c.subtract(n));
            return c;
        }

   public static BigInteger modMult(BigInteger a, BigInteger b, BigInteger n)
        {
            if((a.compareTo(n)!=-1)||((b.compareTo(n)!=-1)))
            {// System.out.println("error");
            	throw new IllegalArgumentException("error");

            }
            
            BigInteger x=BigInteger.ZERO;
            BigInteger y=a;
            String bstr=b.toString(2);
            
            int l=bstr.length();
                    
            for (int i=0;i<l;i++)
            {
                if (bstr.charAt(l-1-i)=='1')
                {
                    x=modAdd(x, y, n);                    
                }
                y=modAdd(y, y, n);    
            }    
            return x;
        }
}
        