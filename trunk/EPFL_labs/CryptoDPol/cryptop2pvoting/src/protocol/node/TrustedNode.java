/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protocol.node;

import java.math.*;
import java.util.*;

/**
 *
 * @author Hamza
 */
public class TrustedNode {

    static public class MessageToBigException extends Exception {
        public MessageToBigException () {super ();}
        public MessageToBigException (String s) {super (s);}
    }

    /** Exception thrown when trying to combine two ciphertexts that are have
     *  different estimated s values. */
    static public class MitchmatchedSizeException extends Exception {
        public MitchmatchedSizeException () {super ();}
        public MitchmatchedSizeException (String s) {super (s);}
    }

    /** The private key class that handles private information and decrypts
     *  messages. */
    static public class PrivateKey {

        /* The private values */
        private BigInteger p, q;


        /** Constructor that generates a 512 bit key.
         */
        public PrivateKey () {
            this (512, 64);
        }

        /** Constructor used for setting up the Private key.
         *  @param bits   number of bits of modulus.
         */
        public PrivateKey (int bits) {
            this (bits, 64);
        }

        /** Constructor used for setting up the Private key.
         *  @param bits       number of bits in modulus.
         *  @param certainty  the chance that p and q are not primes should be < 2^{-certainty}.
         */
        public PrivateKey (int bits, int certainty) {
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
        public PrivateKey (BigInteger p, BigInteger q) {
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


    static public class PublicKey {

        /* The public values */
        protected BigInteger n;

        /** Constructor used for setting up the Public key.
         *  @param n   a large composite number corresponding to some private key.
         */
        public PublicKey (BigInteger n) {
            this.n = n;
        }

        /** Method for extracting the modulus (which is the public key
         *  @return corresponding to this private key.
         */
        public BigInteger GetN () {
            return n;
        }

        /** Gets the modulus used for ciphertexts with a specific s.
         *  @param s the size of the message space.
         *  @return modulus used for encrypting messages of with s.
         */
        public BigInteger GetCiphertextModulus (int s) {
            return n.pow (s+1);
        }

        /** Gets the modulus used for ciphertexts with a specific s.
         *  @param s the size of the message space.
         *  @return modulus used from which plaintext messages can be chosen
         *  given a certain s.
         */
        public BigInteger GetPlaintextModulus (int s) {
            return n.pow (s);
        }

        /** Method that encrypts a message using the surrounding public key. It
         *  calculates what s should be used and calls the Encrypt with specific s.
         *  @see    #Encrypt(java.math.BigInteger, int)
         *  @param msg the message to be encrypted.
         *  @return the encryption of the message msg.
         */
        public BigInteger Encrypt (BigInteger msg) throws MessageToBigException {
            /* Find an s that is big enough */
            int s = (msg.bitLength ()/n.bitLength()) + 1;
            BigInteger n_s = n.pow (s);          // n^s

            while (n_s.compareTo (msg) <= 0) {
                n_s = n_s.multiply (n);
                s++;
            }
            try {
                return Encrypt (msg, s);
            }
            catch (MessageToBigException e) {
                // We have checked msg so this will never happen.
                System.out.println ("Assertion failure");
                System.exit (0);
            }
            return null;
        }

        /** Method that encrypts a message using the surrounding public key. It
         *  calculates what s should be used and calls the Encrypt with specific s.
         *  @param msg the message to be encrypted.
         *  @param s   the size of s used in the decryption, n<SUP>s</SUP> &gt; msg
         *             or an exception is thrown.
         *  @return the encryption of the message msg.
         */
        public BigInteger Encrypt (BigInteger msg, int s) throws MessageToBigException {
            BigInteger n_s = n.pow (s);          // n^s

            if (msg.compareTo (n_s) >= 0)
                throw new MessageToBigException ("Message to big for encryption with " + s);

            BigInteger n_s1 = n.multiply (n_s);  // n^{s+1}

            // Finds a random value and makes sure it's a good random value.
            Random rand = new Random ();
            BigInteger r = new BigInteger (n_s1.bitLength (), rand);
            while ((r.compareTo (n_s1) >= 0) ||
                   (r.compareTo (BigInteger.ZERO) == 0) ||
                   (BigInteger.ONE.compareTo (r.gcd (n)) < 0))
                r = new BigInteger (n_s1.bitLength (), rand);

            // The result is calculated.
            BigInteger res = BigInteger.ONE.add (msg.multiply (n)); // For building result
            BigInteger binomial = msg;   // The value of the binomial in the i'th iteration
            BigInteger msg_i    = msg;   // msg - i + 1
            BigInteger big_i    = BigInteger.ONE; // BigInteger containing i
            BigInteger n_i      = n;     // The i'th power of n

            for (int i = 2; i <= s; i++) {
                n_i = n_i.multiply (n);
                msg_i = msg_i.subtract (BigInteger.ONE);
                big_i = big_i.add (BigInteger.ONE);
                binomial = ((binomial.multiply (msg_i)).multiply (big_i.modInverse (n_s1))).mod (n_s1);
                res = (res.add (binomial.multiply (n_i))).mod (n_s1);
            }
            res = (res.multiply (r.modPow (n_s, n_s1))).mod (n_s1);

            return res;
        }
    }

    /**
     * Combines two ciphertexts such that the resulting ciphertext encrypt the
     * sum of the two plaintexts in the two given encryptions.
     * @param pub the public key
     * @param cip1 the first ciphertext
     * @param cip2 the second ciphertext
     * @return the combined ciphertext
     * @throws MitchmatchedSizeException thrown if cip1 and cip2 have different
     *         sizes.  To avoid this exception use the other CombineCiphertexts
     *         method.
     */
    public static BigInteger CombineCiphertexts(PublicKey pub,
            BigInteger cip1,
            BigInteger cip2) throws MitchmatchedSizeException {

        int s1 = (cip1.bitLength () - 1)/pub.GetPlaintextModulus(1).bitLength();
        int s2 = (cip2.bitLength () - 1)/pub.GetPlaintextModulus(1).bitLength();
        if (s1 != s2) {
            throw new MitchmatchedSizeException("Sizes of ciphertexts does not match (" + s1 + " != " + s2 + ").");
        }

        return CombineCiphertexts(pub, cip1, cip2, s1);
    }

    /**
     * Combines two ciphertexts such that the resulting ciphertext encrypt the
     * sum of the two plaintexts in the two given encryptions.
     * @param pub the public key
     * @param cip1 the first ciphertext
     * @param cip2 the second ciphertext
     * @param s the s that should be used when handling the ciphertexts
     * @return the combined ciphertext
     */
    public static BigInteger CombineCiphertexts(PublicKey pub,
            BigInteger cip1,
            BigInteger cip2,
            int s) {

        return cip1.multiply(cip2).mod(pub.GetCiphertextModulus(s));
    }

    /**
     * Combines two plaintextstexts modulo the message space.
     * @param pub the public key
     * @param msg1 the first plaintext
     * @param msg2 the second plaintext
     * @return the combined plaintext
     */
    public static BigInteger CombinePlaintexts(PublicKey pub,
            BigInteger msg1,
            BigInteger msg2) {

        int s = 1;
        while (msg1.compareTo(pub.GetPlaintextModulus(s)) >= 0 ||
               msg2.compareTo(pub.GetPlaintextModulus(s)) >= 0) {
            s++;
        }

        return CombinePlaintexts(pub, msg1, msg2, s);
    }

    /**
     * Combines two plaintextstexts modulo the message space.
     * @param pub the public key
     * @param msg1 the first plaintext
     * @param msg2 the second plaintext
     * @param s the s that should be used when handling the plaintexts
     * @return the combined plaintext
     */
    public static BigInteger CombinePlaintexts(PublicKey pub,
            BigInteger msg1,
            BigInteger msg2,
            int s) {

        return msg1.add(msg2).mod(pub.GetPlaintextModulus(s));
    }
}
