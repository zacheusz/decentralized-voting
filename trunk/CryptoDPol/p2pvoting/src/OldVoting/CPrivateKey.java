/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package OldVoting;

import java.math.BigInteger;
import java.util.Random;

/**
 *
 * @author Hamza
 */

 /** The private key class that handles private information and decrypts
     *  messages. */
    public class CPrivateKey {

        /* The private values */
        private BigInteger p, q;


        /** Constructor that generates a 512 bit key.
         */
        public CPrivateKey () {
            this (512, 64);
        }

        /** Constructor used for setting up the Private key.
         *  @param bits   number of bits of modulus.
         */
        public CPrivateKey (int bits) {
            this (bits, 64);
        }

        /** Constructor used for setting up the Private key.
         *  @param bits       number of bits in modulus.
         *  @param certainty  the chance that p and q are not primes should be < 2^{-certainty}.
         */
        public CPrivateKey (int bits, int certainty) {
            Random rand = new Random ();
            p = new BigInteger (bits / 2, certainty, rand);
            q = new BigInteger (bits / 2, certainty, rand);
            while (q.compareTo (p) == 0)
                q = new BigInteger (bits / 2, certainty, rand);
        }

        /** Constructor used for setting up the Private key.
         *  @param p   a large prime.
         *  @param q   another large prime of about the same size.
         */
        public CPrivateKey (BigInteger p, BigInteger q) {
            this.p = p;
            this.q = q;
        }

        /** Method for extracting the modulus (which is the public key)
         *  @return corresponding to this private key.
         */
        public BigInteger GetN () {
            return p.multiply (q);
        }

        /** Method for decrypting with surrounding private key. This guesses
         *  what s have been used and calles the method with a specific s.
         *  This will be correct with 1/n probability.
         *  @see    #Decrypt(java.math.BigInteger, int)
         *  @param cip the ciphertext.
         *  @return the resulting message.
         */
        public BigInteger Decrypt (BigInteger cip) {

            BigInteger n = GetN ();

            /* Here the s value that corresponds to cip will be calculated.
               The -1 is to insure that n's bitlength doesn't divide cip's
               so we know it has been rounded down and we get the message size.
               The chance that we get a wrong s is app. 1/n ie. negligible.
            */
            int s = (cip.bitLength () - 1)/n.bitLength();

            return Decrypt (cip, s);
        }

        /** Method for decrypting with surrounding private key. This uses a
         *  specified s, and will throw an exception if it is not big enough.
         *  This will be correct with 1/n probability.
         *  @param cip the ciphertext.
         *  @param s   the size of s used in the decryption, n<SUP>s+1</SUP> &gt; cip
         *             or an exception is thrown.
         *  @return the resulting message.
         */
        public BigInteger Decrypt (BigInteger cip, int s) {

            BigInteger temp; // For holding intermidiate results.
            BigInteger count; // Used for when a BigInteger counter is need.

            BigInteger[] n = new BigInteger[s+1]; // entry i will have n^{i+1}.
            n[0] = GetN ();
            for (int i = 0; i < s; i++) n[i+1] = n[i].multiply (n[0]);

            BigInteger ONE = BigInteger.ONE;

            // Find lambda so we can find d.
            BigInteger p1_q1 = (p.subtract (ONE)).multiply (q.subtract (ONE)); // (p - 1)(q - 1)
            BigInteger lambda = p1_q1.divide ((p.subtract (ONE)).gcd (q.subtract (ONE)));

            // Find d so we can decrypt.
            BigInteger d;
            d = lambda.multiply (lambda.modInverse (n[s-1]));

            // Decrypt cip.
            BigInteger cip_d; // cip raised to power d.
            BigInteger[] L = new BigInteger[s]; // Array of L values, index is -1.
            BigInteger[] mult = new BigInteger[s-1]; // Common mult values in computations.
            BigInteger msg; // The resulting message.

            cip_d = cip.modPow (d, n[s]);

            // L[i] = ((c^d mod n^{i+2}) - 1) / n
            for (int i = 0; i < s; i++)
                L[i] = ((cip_d.mod (n[i+1])).subtract (ONE)).divide (n[0]);

            temp = ONE;  // used to hold (i+1)!
            count = ONE;
            for (int i = 1; i < s; i++) {
                // mult[i] = n^{i+1} / (i+2)! mod n^s   (n^s, so it can be reduced to all n^i)
                count = count.add (ONE);
                temp = temp.multiply (count);
                mult[i-1] = (n[i-1].multiply (temp.modInverse (n[s-1]))).mod (n[s-1]);
            }

            BigInteger t1, t2; // Temp values.
            msg = null;
            for (int j = 1; j <= s; j++) {
                t1 = L[j-1];
                t2 = msg;
                for (int k = 2; k <= j; k++) {
                    msg = msg.subtract (ONE);
                    t2 = (t2.multiply (msg)).mod (n[j-1]);
                    t1 = (t1.subtract (t2.multiply (mult[k-2].mod (n[j-1])))).mod (n[j-1]);
                }
                msg = t1;
            }

            return msg;
        }
    }
