package OldVoting;

import java.security.*;
import java.math.*;
import Exception.*;

//import PublicKey;

public class Result {
    static boolean DEBUG1 = false;
    static boolean DEBUG2 = false;
    static boolean DEBUG3 = false;

    private PublicKey pub;
    private BigInteger[] n;
    private int power;
    private BigInteger[] fac = new BigInteger[power+1]; /* 0 isn't used */

    private BigInteger pow2;
    
    public Result (PublicKey _pub) {
	super ();
	int i;
	BigInteger temp;

if (DEBUG1) {System.out.println ("Result.Checkpoint 1");}
	pub = _pub;
	power = pub.power;
	n = new BigInteger[power+1];
	n[0] = BigInteger.ONE;
	n[1] = pub.n;
	for (i = 2; i <= power; i++) n[i] = n[i-1].multiply (n[1]);
if (DEBUG1) {System.out.println ("Result.Checkpoint 2");}
	fac = new BigInteger[power+1];
	fac[1] = temp = new BigInteger ("1");
	for (i = 2; i <= power; i++)
	    fac[i] = (temp = temp.multiply 
		      (new BigInteger ("" + i))).modInverse (n[power-1]);
	pow2 = (new BigInteger ("2")).pow (pub.signsize);
if (DEBUG1) {System.out.println ("Result.Checkpoint 3");}
    }
	
    public boolean CheckShare (DecodingShare share, BigInteger votes)
	                                        throws NoSuchAlgorithmException {
	int i;
	MessageDigest hash = MessageDigest.getInstance("SHA");
	BigInteger sum = BigInteger.ZERO;
	BigInteger temp1, temp2, chal;
	byte[] challenge;

if (DEBUG1) {System.out.println ("Result.CheckShare.Checkpoint 1");}
	hash.update (n[1].toByteArray ());
	hash.update (votes.toByteArray ());
	hash.update (share.share.toByteArray ());
	hash.update (share.vrandom.toByteArray ());
	hash.update (share.mrandom.toByteArray ());
	hash.update (pub.verification[0].toByteArray ());
	hash.update (pub.verification[share.tallynr].toByteArray ());
	challenge = hash.digest ();

	chal = (new BigInteger (challenge)).mod (pow2);
if (DEBUG2) {System.out.println ("challenge = " + challenge + "\nchal = " + chal);}
if (DEBUG1) {System.out.println ("Result.CheckShare.Checkpoint 2");}
	temp1 = (share.mrandom.multiply (share.share.modPow 
					(chal, n[power]))).mod (n[power]);
	temp2 = votes.modPow (share.proof, n[power]);
if (DEBUG2) {System.out.println ("temp1 = " + temp1 + "\ntemp2 = " + temp2);}
	if (temp1.compareTo (temp2) != 0) return false;

if (DEBUG1) {System.out.println ("Result.CheckShare.Checkpoint 3");}
	temp1 = (share.vrandom.multiply (pub.verification[share.tallynr].modPow
		      (chal, n[power]))).mod (n[power]);
	temp2 = pub.verification[0].modPow (share.proof, n[power]);
if (DEBUG2) {System.out.println ("temp1 = " + temp1 + "\ntemp2 = " + temp2);}
	if (temp1.compareTo (temp2) != 0) return false;
if (DEBUG1) {System.out.println ("Result.CheckShare.Checkpoint 4");}

	return true;
    }

    private boolean CheckVote (Vote vote) throws NoSuchAlgorithmException {
	int i;
	MessageDigest hash = MessageDigest.getInstance("SHA");
	int votecount = pub.votes.length;
	BigInteger sum = BigInteger.ZERO;
	BigInteger temp1, temp2;
	byte[] challenge;

if (DEBUG1) {System.out.println ("Result.CheckVote.Checkpoint 1");}
	hash.update (n[1].toByteArray ());
if (DEBUG2) {System.out.println ("Vote count = " + votecount);}
if (DEBUG2) {System.out.println ("e length = " + vote.e.length);}
if (DEBUG2) {System.out.println ("a length = " + vote.a.length);}
if (DEBUG2) {System.out.println ("z length = " + vote.z.length);}
	for (i = 0; i < votecount; i++) {
if (DEBUG2) {System.out.println ("e[" + i + "] = " + vote.e[i] + "\na[" + i +
				 "] = " + vote.a[i] + "\nz[" + i + "] = " +
				 vote.z[i] + "\nvote(" + i + ") = " +
				 vote.vote);}
	    /* Check for om alle laengder er ens ellers exception */
if (DEBUG1) {System.out.println ("Result.CheckVote.Checkpoint 1a");}
	    sum = sum.add (vote.e[i]);
if (DEBUG1) {System.out.println ("Result.CheckVote.Checkpoint 1b");}
	    hash.update (vote.a[i].toByteArray ());
if (DEBUG1) {System.out.println ("Result.CheckVote.Checkpoint 1c");}
            temp1 = (pub.g.modPow (pub.votes[i], n[power])).modInverse (n[power]);
if (DEBUG2) {System.out.println ("vote(" + i + ")^-1 = " + temp1);}
if (DEBUG1) {System.out.println ("Result.CheckVote.Checkpoint 1d");}
	    temp1 = (vote.vote.multiply (temp1)).mod (n[power]);
	    temp1 = vote.a[i].multiply (temp1.modPow (vote.e[i], n[power]));
if (DEBUG1) {System.out.println ("Result.CheckVote.Checkpoint 1e");}
	    temp1 = temp1.mod (n[power]);
	    temp2 = vote.z[i].modPow (n[1], n[power]);
if (DEBUG1) {System.out.println ("Result.CheckVote.Checkpoint 1f");}
if (DEBUG2) {System.out.println ("temp1 = " + temp1 + "\ntemp2 = " + temp2);}
	    if (temp2.compareTo (temp1) != 0) return false;
	}
if (DEBUG1) {System.out.println ("Result.CheckVote.Checkpoint 2");}
	challenge = hash.digest ();
	temp1 = (new BigInteger (challenge)).mod (pow2);
	temp2 = sum.mod (pow2);
	if (temp2.compareTo (temp1) != 0) return false;

if (DEBUG1) {System.out.println ("Result.CheckVote.Checkpoint 3");}
	return true;
    }

    /** Exception thrown when trying to encrypt or decrypt a message with
     *  a fixed s, and then message or ciphertext exceeds the modulus size. */
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
    public static BigInteger CombineCVotes(CPublicKey pub,
            BigInteger cip1,
            BigInteger cip2) throws MitchmatchedSizeException {

        int s1 = (cip1.bitLength () - 1)/pub.GetPlaintextModulus(1).bitLength();
        int s2 = (cip2.bitLength () - 1)/pub.GetPlaintextModulus(1).bitLength();
        if (s1 != s2) {
            throw new MitchmatchedSizeException("Sizes of ciphertexts does not match (" + s1 + " != " + s2 + ").");
        }

        return CombineCVotes(pub, cip1, cip2, s1);
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
    public static BigInteger CombineCVotes(CPublicKey pub,
            BigInteger cip1,
            BigInteger cip2,
            int s) {

        return cip1.multiply(cip2).mod(pub.GetCiphertextModulus(s));
    }


public BigInteger CombineVotes ( BigInteger vote1,BigInteger vote2)
                   throws NoLegalVotes, NoSuchAlgorithmException, NotEnoughTallies {

	BigInteger result;

if (DEBUG1) {System.out.println ("Result.Combine.Checkpoint 1");}

if (vote1.equals(BigInteger.ZERO))
    return vote2;
else if (vote2 .equals(BigInteger.ZERO))
    return vote1;
else
    {

    result = (vote1.multiply (vote2))
            .mod (n[power]);
    if (DEBUG1) {System.out.println ("Result.Combine.Checkpoint 2");}
    if (DEBUG3) {System.out.println ("Combined Votes = " + result);}

    return result;
    }
    }

    public BigInteger DistDecryptVotes (DecodingShare[] cip, BigInteger result)
                   throws NoLegalVotes, NoSuchAlgorithmException, NotEnoughTallies {

	BigInteger temp, t1, t2, power_d, msg, delta, lambda, w;
	BigInteger[] L = new BigInteger[power+1];
	int[] Passed = new int[cip.length];
	int passptr;
	int i, j;

if (DEBUG1) {System.out.println ("Result.Combine.Checkpoint 4");}
if (DEBUG3) {System.out.println ("Combined Votes = " + result);}

	passptr = 0;
	for (i = 0; i < cip.length; i++) {
	    if (CheckShare (cip[i], result)) {
		Passed[passptr++] = i;
	    }
	}

if (DEBUG1) {System.out.println ("Result.Combine.Checkpoint 5");}
	if (passptr < pub.k) 
	    throw new NotEnoughTallies ("Too many tallies where corrupt.");

if (DEBUG1) {System.out.println ("Result.Combine.Checkpoint 6");}
	/* No need to do more work than needed so only an exact qualified set
	   is choosen (the k first ones). */
	passptr = pub.k;

	/* Check for at der er nok til at faa resultatet */
	delta = n[0];
	for (i = 2; i <= pub.l; i++)
	    delta = delta.multiply (new BigInteger ("" + i));
if (DEBUG1) {System.out.println ("Result.Combine.Checkpoint 7");}

	w = n[0];
	for (i = 0; i < passptr; i++) {
	    lambda = n[0];
	    for (j = 0; j < passptr; j++) {
		if (j != i) {
		    lambda = lambda.multiply (new BigInteger ("-" + (Passed[j]+1)));
		}
	    }
	    
	    lambda = lambda.multiply (delta);
	    for (j = 0; j < passptr; j++) {
		if (j != i) {
		    lambda = lambda.divide (
			        (new BigInteger ("" + (Passed[i]+1))).subtract
				 (new BigInteger ("" + (Passed[j]+1))));
		}
	    }
	    w = (w.multiply (cip[Passed[i]].share.modPow (lambda.multiply
                             (new BigInteger ("2")),n[power]))).mod (n[power]);
	}
if (DEBUG1) {System.out.println ("Result.Combine.Checkpoint 8");}

        power_d = w;

 	for (i = 2; i <= power; i++) {
	    temp = power_d.mod (n[i]);
 	    temp = temp.subtract (n[0]);
	    L[i-1] = temp.divide (n[1]);
	}
	temp = power_d.subtract (n[0]);
	L[power-1] = temp.divide (n[1]);
if (DEBUG1) {System.out.println ("Result.Combine.Checkpoint 9");}

	msg = null;
	for (i = 1; i < power; i++) {
	    t1 = L[i];
	    t2 = msg;
	    for (j = 2; j <= i; j++) {
		msg = msg.subtract (n[0]);
		t2 = (t2.multiply (msg)).mod (n[i]);
		t1 = (t1.subtract (n[j-1].multiply (
                         t2.multiply (fac[j])))).mod (n[i]);
	    }
	    msg = t1;
	}
if (DEBUG1) {System.out.println ("Result.Combine.Checkpoint 10");}

	temp = delta.multiply (new BigInteger ("4"));
	temp = temp.modInverse (n[power-1]);
	msg = (msg.multiply (temp)).mod (n[power-1]);

if (DEBUG1) {System.out.println ("Result.Combine.Checkpoint 11");}
        return msg;
    }


    public void PrintResult (BigInteger result) {
	int length, strlen;
	int i, j;
	BigInteger temp;

if (DEBUG1) {System.out.println ("Result.PrintResult.Checkpoint 1");}
        if (pub.votes[1].compareTo (BigInteger.ZERO) < 0) {
	    System.out.println (pub.ElectionQuestion);
	    if (n[power].compareTo (result.multiply (new BigInteger ("2"))) > 0) {
		System.out.println ("Yes won by: " + result + " votes");
	    } else {
		System.out.println ("No won by: " + n[power].subtract (result) +
				    " votes");
	    }
	} else {
	    length = 0;
	    for (i = 0; i < pub.vote_names.length; i++) {
		strlen = pub.vote_names[i].length ();
		if (strlen > length)
		    length = strlen;
	    }
	
if (DEBUG1) {System.out.println ("Result.PrintResult.Checkpoint 1");}
	    length++;
	    System.out.println (pub.ElectionQuestion);
	
if (DEBUG1) {System.out.println ("Result.PrintResult.Checkpoint 1");}
            for (i = 0; i < pub.vote_names.length; i++) {
		strlen = pub.vote_names[i].length ();
		System.out.print (pub.vote_names[i] + ":");
		for (j = 0; j + strlen < length; j++)
		    System.out.print (" ");
		if (i < (pub.votes.length-1))
		    temp = result.mod (pub.votes[i+1]);
		else
		    temp = result;
		temp = temp.divide (pub.votes[i]);
		System.out.println (" " + temp);
	    }
	}

if (DEBUG1) {System.out.println ("Result.PrintResult.Checkpoint 1");}

    }
}
