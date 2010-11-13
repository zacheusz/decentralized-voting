package Cryptosystem;

import java.math.*;
import java.util.*;

public class Test {

    public static void main (String[] args) throws Cryptosystem.MessageToBigException {
        BigInteger msg1, msg2, msg3, msg3dec;
        BigInteger cip1, cip2, cip3;

        // Note 512 bits is too low to be useful.  This size is chosen for illustration only.
        int bits = 512;
        int certainty = 64;

        Cryptosystem.PrivateKey priv = new Cryptosystem.PrivateKey (bits, certainty);
        Cryptosystem.PublicKey pub = new Cryptosystem.PublicKey (priv.GetN ());

        // Make message 1
        msg1 = new BigInteger ("1000");
        System.out.println ("Message 1 = " + msg1);

        // Make message 2
        msg2 = new BigInteger ("42");
        System.out.println ("Message 2 = " + msg2);

        // Encrypt message 1
        cip1 = pub.Encrypt (msg1);
        System.out.println ("Ciphertext 1 = " + cip1);

        // Encrypt message 2
        cip2 = pub.Encrypt (msg2);
        System.out.println ("Ciphertext 2 = " + cip2);

        // Multiply encryption 1 and 2
        try {
            cip3 = Cryptosystem.CombineCiphertexts(pub, cip1, cip2);
        } catch (Exception e) {
            System.out.println ("Failed to combine ciphertexts:\n" + e.getMessage());
            return;
        }

        // add the message 1 and 2.
        msg3 = Cryptosystem.CombinePlaintexts(pub, msg1, msg2);
        msg3 = msg1.add (msg2);

        // Now cip3 should be an encryption of msg3.

        // Decrypt cip3 and check that it equals msg3
        msg3dec = priv.Decrypt (cip3);
        if (msg3dec.equals (msg3)) {
            System.out.println ("Test Passed - msg3 = msg1 + msg2 (" +
                                msg3 + " = " + msg1 + " + " + msg2+ ")!");
        } else {
            System.out.println ("Test Failed - msg3 not equal to msg1 + msg2!\n" +
                                msg1 + " + " + msg2 + " != " + msg3dec);
        }
    }
}
