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
public class CPublicKey {

    static public class MessageToBigException extends Exception {

        public MessageToBigException() {
            super();
        }

        public MessageToBigException(String s) {
            super(s);
        }
    }

    /** Exception thrown when trying to combine two ciphertexts that are have
     *  different estimated s values. */
    static public class MitchmatchedSizeException extends Exception {

        public MitchmatchedSizeException() {
            super();
        }

        public MitchmatchedSizeException(String s) {
            super(s);
        }
    }

    /* The public values */
    protected BigInteger n;

    /** Constructor used for setting up the Public key.
     *  @param n   a large composite number corresponding to some private key.
     */
    public CPublicKey(BigInteger n) {
        this.n = n;
    }

    /** Method for extracting the modulus (which is the public key
     *  @return corresponding to this private key.
     */
    public BigInteger GetN() {
        return n;
    }

    /** Gets the modulus used for ciphertexts with a specific s.
     *  @param s the size of the message space.
     *  @return modulus used for encrypting messages of with s.
     */
    public BigInteger GetCiphertextModulus(int s) {
        return n.pow(s + 1);
    }

    /** Gets the modulus used for ciphertexts with a specific s.
     *  @param s the size of the message space.
     *  @return modulus used from which plaintext messages can be chosen
     *  given a certain s.
     */
    public BigInteger GetPlaintextModulus(int s) {
        return n.pow(s);
    }

    /** Method that encrypts a message using the surrounding public key. It
     *  calculates what s should be used and calls the Encrypt with specific s.
     *  @see    #Encrypt(java.math.BigInteger, int)
     *  @param msg the message to be encrypted.
     *  @return the encryption of the message msg.
     */
    public BigInteger Encrypt(BigInteger msg) throws MessageToBigException {
        /* Find an s that is big enough */
        int s = (msg.bitLength() / n.bitLength()) + 1;
        BigInteger n_s = n.pow(s);          // n^s

        while (n_s.compareTo(msg) <= 0) {
            n_s = n_s.multiply(n);
            s++;
        }
        try {
            return Encrypt(msg, s);
        } catch (MessageToBigException e) {
            // We have checked msg so this will never happen.
            System.out.println("Assertion failure");
            System.exit(0);
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
    public BigInteger Encrypt(BigInteger msg, int s) throws MessageToBigException {
        BigInteger n_s = n.pow(s);          // n^s

        if (msg.compareTo(n_s) >= 0) {
            throw new MessageToBigException("Message to big for encryption with " + s);
        }

        BigInteger n_s1 = n.multiply(n_s);  // n^{s+1}

        // Finds a random value and makes sure it's a good random value.
        Random rand = new Random();
        BigInteger r = new BigInteger(n_s1.bitLength(), rand);
        while ((r.compareTo(n_s1) >= 0)
                || (r.compareTo(BigInteger.ZERO) == 0)
                || (BigInteger.ONE.compareTo(r.gcd(n)) < 0)) {
            r = new BigInteger(n_s1.bitLength(), rand);
        }

        // The result is calculated.
        BigInteger res = BigInteger.ONE.add(msg.multiply(n)); // For building result
        BigInteger binomial = msg;   // The value of the binomial in the i'th iteration
        BigInteger msg_i = msg;   // msg - i + 1
        BigInteger big_i = BigInteger.ONE; // BigInteger containing i
        BigInteger n_i = n;     // The i'th power of n

        for (int i = 2; i <= s; i++) {
            n_i = n_i.multiply(n);
            msg_i = msg_i.subtract(BigInteger.ONE);
            big_i = big_i.add(BigInteger.ONE);
            binomial = ((binomial.multiply(msg_i)).multiply(big_i.modInverse(n_s1))).mod(n_s1);
            res = (res.add(binomial.multiply(n_i))).mod(n_s1);
        }
        res = (res.multiply(r.modPow(n_s, n_s1))).mod(n_s1);

        return res;
    }
}
