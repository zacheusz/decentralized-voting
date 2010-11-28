    package OldVoting;

import Cryptosystem.Cryptosystem.MitchmatchedSizeException;
import java.math.*;
import Exception.*;

public class PublicKey {
    static boolean DEBUG1 = false;
    static boolean DEBUG2 = false;

    public BigInteger g;    // the generator g
    public BigInteger n;    // the modulo base
    public int power;       // the modulo power
    public int k;           // Number of honest tallies needed to decrypt
    public int l;           // Number of tallies
    public int signsize;    // Signature size
    public BigInteger[] verification;   // Verification array for the l tallies

    public BigInteger[] votes;       // The base for the votes
    public String[] vote_names;      // The name of the choice

    public String ElectionQuestion;  // The election question

    // Change to only use get methods and make them private.

    public PublicKey (BigInteger _g, BigInteger _n, int _power, int _signsize,
		      String _ElectionQuestion,
		      BigInteger[] _votes, String[] _vote_names, 
		      BigInteger[] _verification) throws WrongLength {

if (DEBUG1) {System.out.println ("PublicKey.Checkpoint 1");}
        if (_vote_names.length != _votes.length)
	    throw new WrongLength ("Size of Arrays don't match");

if (DEBUG1) {System.out.println ("PublicKey.Checkpoint 2");}
	k = l = 1;
	g = _g;
	n = _n;
	power = _power;
	ElectionQuestion = _ElectionQuestion;
	votes = _votes;
	vote_names = _vote_names;
	signsize = _signsize;
if (DEBUG1) {System.out.println ("PublicKey.Checkpoint 3");}
    }

    public PublicKey (BigInteger _g, BigInteger _n, int _power, int _signsize,
		      int _k, int _l, String _ElectionQuestion, 
		      BigInteger[] _votes, String[] _vote_names, 
		      BigInteger[] _verification) throws WrongLength {
	
if (DEBUG1) {System.out.println ("PublicKey.2.Checkpoint 1");}
if (DEBUG2) {System.out.println ("names.length = " + _vote_names.length +
				 "\nvotes.length = " + _votes.length);}
	if (_vote_names.length != _votes.length) 
	    throw new WrongLength ("Size of Vote Arrays don't match");
if (DEBUG1) {System.out.println ("PublicKey.2.Checkpoint 2");}
if (DEBUG2) {System.out.println ("verification.length = " + 
				 _verification.length + "\nl = " + _l);}
	if (_verification.length != _l+1) 
	    throw new WrongLength ("Size of Verification Array don't match");

if (DEBUG1) {System.out.println ("PublicKey.2.Checkpoint 3");}
	k = _k;
	l = _l;
	g = _g;
	n = _n;
	power = _power;
	ElectionQuestion = _ElectionQuestion;
	votes = _votes;
	vote_names = _vote_names;
	verification = _verification;
	signsize = _signsize;
if (DEBUG1) {System.out.println ("PublicKey.2.Checkpoint 4");}
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
    public  BigInteger CombineCiphertexts(
            BigInteger cip1,
            BigInteger cip2) throws MitchmatchedSizeException {

        int s1 = (cip1.bitLength () - 1)/this.GetPlaintextModulus(1).bitLength();
        int s2 = (cip2.bitLength () - 1)/this.GetPlaintextModulus(1).bitLength();
        if (s1 != s2) {
            throw new MitchmatchedSizeException("Sizes of ciphertexts does not match (" + s1 + " != " + s2 + ").");
        }

        return CombineCiphertexts( cip1, cip2, s1);
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
    public  BigInteger CombineCiphertexts(
            BigInteger cip1,
            BigInteger cip2,
            int s) {

        return cip1.multiply(cip2).mod(this.GetCiphertextModulus(s));
    }

    /**
     * Combines two plaintexts modulo the message space.
     * @param pub the public key
     * @param msg1 the first plaintext
     * @param msg2 the second plaintext
     * @return the combined plaintext
     */
    public  BigInteger CombinePlaintexts(
            BigInteger msg1,
            BigInteger msg2) {

        int s = 1;
        while (msg1.compareTo(this.GetPlaintextModulus(s)) >= 0 ||
               msg2.compareTo(this.GetPlaintextModulus(s)) >= 0) {
            s++;
        }

        return CombinePlaintexts(msg1, msg2, s);
    }

    /**
     * Combines two plaintextstexts modulo the message space.
     * @param pub the public key
     * @param msg1 the first plaintext
     * @param msg2 the second plaintext
     * @param s the s that should be used when handling the plaintexts
     * @return the combined plaintext
     */
    public  BigInteger CombinePlaintexts(
            BigInteger msg1,
            BigInteger msg2,
            int s) {

        return msg1.add(msg2).mod(this.GetPlaintextModulus(s));
    }
    public String toString () {
	String str = new String ();
	
	str = str + "Public Key:\n  g = ";
	str = str + g.toString ();
	str = str + "\n  n = ";
	str = str + n.toString ();
	str = str + "\n  power = ";
	str = str + power;
	str = str + "\n  l = ";
	str = str + l;
	str = str + "\n  k = ";
	str = str + k;
	str = str + "\n\n";

        return str;
    }
}
