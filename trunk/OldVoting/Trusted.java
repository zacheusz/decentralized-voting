package OldVoting;

import java.lang.*; // Remove
import java.math.*;
import java.util.*;
//import SecretKey;
//import SecretDistributedKey;
//import PublicKey;
import Exception.*;

public class Trusted {
    static boolean DEBUG1 = false; // Structure Debug
    static boolean DEBUG2 = false; // Local Analysis
    static boolean DEBUG3 = false; // Global Analysis

    private PublicKey pub;
    private SecretKey sec;
    private SecretDistributedKey distsec;

    /* temp vars */
    private BigInteger[] n;
    private BigInteger p_,q_,p,q, m, g;
    private BigInteger[] vk;
    private int pow, l, k, hashbits;
    private Random rand;

    private int keymade = 0;

    public Trusted (int bits, int power, int hashsize, int distsize, int needed,
		    int certainty) {
	BigInteger temp1, temp2;
	BigInteger ns_m;
	
	BigInteger  secret;
	int i;

	l = distsize;
	k = needed;
	hashbits = hashsize;
	
if (DEBUG1) {System.out.println ("Trusted.Checkpoint 1");}
	pow = power;
        rand = new Random ();
	p_ = new BigInteger (bits / 2 - 1, certainty, rand);
	p = p_.add (p_.add (BigInteger.ONE));
	while (!p.isProbablePrime (certainty)) {
	    p_ = new BigInteger (bits / 2 - 1, certainty, rand);
	    p = p_.add (p_.add (BigInteger.ONE));
	}
if (DEBUG1) {System.out.println ("Trusted.Checkpoint 2");}
if (DEBUG2 || DEBUG3) {System.out.println ("p` = " + p_ + "\np = " + p);}
        q_ = new BigInteger (bits / 2 - 1, certainty, rand);
	q = q_.add (q_.add (BigInteger.ONE));
	while ((q.compareTo (p) == 0) || (!q.isProbablePrime (certainty))) {
	    q_ = new BigInteger (bits / 2 - 1, certainty, rand);
	    q = q_.add (q_.add (BigInteger.ONE));
	}
if (DEBUG1) {System.out.println ("Trusted.Checkpoint 3");}
if (DEBUG2 || DEBUG3) {System.out.println ("q` = " + q_ + "\nq = " + q);}
	m = p_.multiply (q_);
	/* Generate the different powers of n */
	n = new BigInteger[power+1];
        n[0] = BigInteger.ONE;
        n[1] = p.multiply (q);
	for (i = 2; i <= power; i++)
	    n[i] = n[i-1].multiply (n[1]);

if (DEBUG1) {System.out.println ("Trusted.Checkpoint 4");}
	/* Find the power to use for g */
        temp1 = new BigInteger (n[power].bitLength (), rand);
        while ((n[0].compareTo (temp1.gcd (n[1])) < 0) ||
               (temp1.compareTo (n[power]) >= 0))
            temp1 = new BigInteger (n[power].bitLength (), rand);
        /* Since gcd (n,temp1) > 1 is negligible for large p and q it's not 100%
	   necessary with this test */
        temp2 = n[1].add (n[0]);
        g = temp2.modPow (temp1, n[power]);

if (DEBUG1) {System.out.println ("Trusted.Checkpoint 5");}
if (DEBUG2 || DEBUG3) {System.out.println ("g = " + g + "\ng power = " + temp1);}
        /* Find the d for use in decryption, by using chinese reaminder on
	   d = 0 mod lambda, d = i^{-1} mod n^{npower-1} */
        temp1 = temp1.modInverse (n[power-1]);
        temp2 = m.modInverse (n[power-1]);
        temp1 = temp1.multiply (m);
        temp1 = temp1.multiply (temp2);
        ns_m = n[power-1].multiply (m);
	secret = temp1.mod (ns_m);
        sec=new SecretKey(secret, 0);//I used 0 to indicate that this is not a share. this is the whole key

if (DEBUG3) {System.out.println ("Master secret = " + secret);}
if (DEBUG1) {System.out.println ("Trusted.Checkpoint 6");}
        distsec = new SecretDistributedKey (l);

    }

public void produceKeyShares(){
    BigInteger mod,delta, delta_inv, temp;
    BigInteger[][] X = new BigInteger[l+1][k];
    BigInteger[] a = new BigInteger[k];
    int i, j;

    mod = n[pow-1].multiply (m);

if (DEBUG1) {System.out.println ("Trusted.Checkpoint 7");}
	delta = n[0];
	for (i = 2; i <= l; i++)
	    delta = delta.multiply (new BigInteger ("" + i));

	
	delta = delta.mod (mod);
	delta_inv = delta.modInverse (mod);

if (DEBUG1) {System.out.println ("Trusted.Checkpoint 8");}
	for (i = 0; i <= l; i++) {
	    X[i][0] = n[0];
	    X[i][1] = new BigInteger("" + i);
	}

if (DEBUG1) {System.out.println ("Trusted.Checkpoint 9");}
	for (i = 2; i < k; i++) {
	    for (j = 0; j <= l; j++)
		X[j][i] = (X[j][i-1].multiply (X[j][1])).mod (mod);
	}

if (DEBUG1) {System.out.println ("Trusted.Checkpoint 10");}
        a[0] = sec.key;
	for (i = 1; i < k; i++) {
	    a[i] = new BigInteger (mod.bitLength (), rand);
	    while (mod.compareTo (a[i]) <= 0)
		a[i] = new BigInteger (mod.bitLength (), rand);
	}

if (DEBUG1) {System.out.println ("Trusted.Checkpoint 11");}
	for (i = 0; i < l; i++) {
	    temp = a[0];
	    for (j = 1; j < k; j++) {
		temp = (temp.add ((a[j].multiply (X[i+1][j])))).mod (mod);
	    }
	    distsec.keys[i].key = temp;
	    distsec.keys[i].nr = i+1;
	}

if (DEBUG1) {System.out.println ("Trusted.Checkpoint 12");}
	temp = new BigInteger (n[pow].bitLength (), rand);
        while ((n[0].compareTo (temp.gcd (n[1])) < 0) ||
               (temp.compareTo (n[pow]) >= 0))
	    temp = new BigInteger (n[pow].bitLength (), rand);
	
if (DEBUG1) {System.out.println ("Trusted.Checkpoint 13");}
	vk = new BigInteger[l+1];
	vk[0] = (temp.multiply (temp)).mod (n[pow]);
	
if (DEBUG1) {System.out.println ("Trusted.Checkpoint 14");}
	for (i = 0; i < l; i++)
	    vk[i+1] = vk[0].modPow (distsec.keys[i].key.multiply
				       (new BigInteger ("2")),
				    n[pow]);

if (DEBUG1) {System.out.println ("Trusted.Checkpoint 15");}
    }
    
    public void MakeYesNoElection (String Question) throws WrongLength {
	BigInteger[] votes = new BigInteger[2];
	String[] vote_names = new String[2];
	
if (DEBUG1) {System.out.println ("Trusted.MakeYesNoElection.Checkpoint 1");}
	votes[0] = BigInteger.ONE;
	votes[1] = (BigInteger.ONE).negate ();
	vote_names[0] = "Yes";
	vote_names[1] = "No";
	
	keymade = 1;

if (DEBUG1) {System.out.println ("Trusted.MakeYesNoElection.Checkpoint 2");}
if (DEBUG2) {System.out.println ("k = " + k + "\nl = " + l + "\nQuestion = " +
				    Question + "\nVK.length = " + vk.length);}
        pub = new PublicKey (g, n[1], pow, hashbits, k, l, Question, votes,
			     vote_names, vk);

if (DEBUG1) {System.out.println ("Trusted.MakeYesNoElection.Checkpoint 3");}
    }

    public void MakeSelectionElection (String Question, 
				       String[] Candidates) throws WrongLength {
	BigInteger[] votes = new BigInteger[Candidates.length]; //a vector with same length as the candidates
	int bits;
	BigInteger base, temp;
	int i;

if (DEBUG1) {System.out.println ("Trusted.MakeSelectionElection.Checkpoint 1");}
	votes[0] = BigInteger.ONE;
	bits = n[pow-1].bitLength() / Candidates.length;
	base = (new BigInteger ("2")).pow (bits);
	temp = base;

if (DEBUG1) {System.out.println ("Trusted.MakeSelectionElection.Checkpoint 2");}
	for (i = 1; i < Candidates.length; i++) {
	    votes[i] = temp;
	    temp = temp.multiply (base);
	}

	keymade = 1;

if (DEBUG1) {System.out.println ("Trusted.MakeSelectionElection.Checkpoint 3");}
	pub = new PublicKey (g, n[1], pow, hashbits, k, l, Question, votes,
			     Candidates, vk);
	
if (DEBUG1) {System.out.println ("Trusted.MakeSelectionElection.Checkpoint 4");}
    }

    public PublicKey GetPublicKey () {
	
	/* Boer nok lave exception her */
	if (keymade == 0)
	    return null;
	return pub;
    }

    public SecretDistributedKey GetSecretDistributedKey () {
	
	/* Boer nok lave exception her */
	if (keymade == 0)
	    return null;
	return distsec;
    }

    public SecretKey GetSecretDistributedKeyPart (int i) {
	
	/* Boer nok lave exception her */
	if (keymade == 0)
	    return null;
	if ((i < 0) || (i >= distsec.keys.length)) return null;
	return distsec.keys[i];
    }
    
    public BigInteger GetN () {
          return p.multiply (q);
    }
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
 
   
