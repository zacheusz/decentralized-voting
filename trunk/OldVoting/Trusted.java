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

    private int keymade = 0;

    public Trusted (int bits, int power, int hashsize, int distsize, int needed,
		    int certainty) {
        Random rand;
	BigInteger temp1, temp2;
	BigInteger ns_m;
	BigInteger[][] X = new BigInteger[distsize+1][needed];
	BigInteger[] a = new BigInteger[needed];
	BigInteger delta, delta_inv, mod, temp, secret;
	int i, j;

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

if (DEBUG3) {System.out.println ("Master secret = " + secret);}
if (DEBUG1) {System.out.println ("Trusted.Checkpoint 6");}
	distsec = new SecretDistributedKey (distsize);

	mod = n[power-1].multiply (m);

if (DEBUG1) {System.out.println ("Trusted.Checkpoint 7");}
	delta = n[0];
	for (i = 2; i <= distsize; i++)
	    delta = delta.multiply (new BigInteger ("" + i));

	
	delta = delta.mod (mod);
	delta_inv = delta.modInverse (mod);

if (DEBUG1) {System.out.println ("Trusted.Checkpoint 8");}
	for (i = 0; i <= distsize; i++) {
	    X[i][0] = n[0];
	    X[i][1] = new BigInteger("" + i);
	}

if (DEBUG1) {System.out.println ("Trusted.Checkpoint 9");}
	for (i = 2; i < needed; i++) {
	    for (j = 0; j <= distsize; j++)
		X[j][i] = (X[j][i-1].multiply (X[j][1])).mod (mod);
	}

if (DEBUG1) {System.out.println ("Trusted.Checkpoint 10");}
        a[0] = secret;
	for (i = 1; i < needed; i++) {
	    a[i] = new BigInteger (mod.bitLength (), rand);
	    while (mod.compareTo (a[i]) <= 0)
		a[i] = new BigInteger (mod.bitLength (), rand);
	}

if (DEBUG1) {System.out.println ("Trusted.Checkpoint 11");}
	for (i = 0; i < distsize; i++) {
	    temp = a[0];
	    for (j = 1; j < needed; j++) {
		temp = (temp.add ((a[j].multiply (X[i+1][j])))).mod (mod);
	    }
	    distsec.keys[i].key = temp;
	    distsec.keys[i].nr = i+1;
	}

if (DEBUG1) {System.out.println ("Trusted.Checkpoint 12");}
	temp = new BigInteger (n[power].bitLength (), rand);
        while ((n[0].compareTo (temp.gcd (n[1])) < 0) ||
               (temp.compareTo (n[power]) >= 0))
	    temp = new BigInteger (n[power].bitLength (), rand);
	
if (DEBUG1) {System.out.println ("Trusted.Checkpoint 13");}
	vk = new BigInteger[distsize+1];
	vk[0] = (temp.multiply (temp)).mod (n[power]);
	
if (DEBUG1) {System.out.println ("Trusted.Checkpoint 14");}
	for (i = 0; i < distsize; i++)
	    vk[i+1] = vk[0].modPow (distsec.keys[i].key.multiply
				       (new BigInteger ("2")),
				    n[power]);

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

}
 
   
