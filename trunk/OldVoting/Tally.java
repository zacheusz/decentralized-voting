package OldVoting;

import java.util.*;
import java.math.*;
import java.security.*;
//import SecretKey;
//import PublicKey;
import Exception.*;

public class Tally {
    static boolean DEBUG1 = false;
    static boolean DEBUG2 = false;
    static boolean DEBUG3 = false;
    
    private SecretKey sec;
    private PublicKey pub;

    private BigInteger[] n;
    private int power;

    private BigInteger pow2;

    public Tally (SecretKey _sec, PublicKey _pub) {
	int i;

if (DEBUG1) {System.out.println ("Tally.Checkpoint 1");}
	pub = _pub;
	sec = _sec;

if (DEBUG1) {System.out.println ("Tally.Checkpoint 2");}
	power = pub.power;
	n = new BigInteger[power+1];
	n[0] = BigInteger.ONE;
	n[1] = pub.n;
	for (i = 2; i <= power; i++) n[i] = n[i-1].multiply (n[1]);
	pow2 = (new BigInteger ("2")).pow (pub.signsize);
if (DEBUG1) {System.out.println ("Tally.Checkpoint 3");}
    }
    
    private boolean CheckVote (Vote vote) throws NoSuchAlgorithmException {
	int i;
	MessageDigest hash = MessageDigest.getInstance("SHA");
	int votecount = pub.votes.length;
	BigInteger sum = BigInteger.ZERO;
	BigInteger temp1, temp2;
	byte[] challenge;

if (DEBUG1) {System.out.println ("Tally.CheckVote.Checkpoint 1");}
if (DEBUG2) {System.out.println ("votecount = " + votecount);}
	hash.update (n[1].toByteArray ());
	for (i = 0; i < votecount; i++) {
	    /* Check for om alle laengder er ens ellers exception */
if (DEBUG2) {System.out.println ("e[" + i + "] = " + vote.e[i] + "\na[" + i +
				 "] = " + vote.a[i] + "\nz[" + i + "] = " +
				 vote.z[i] + "\nvote(" + i + ") = " +
				 vote.vote);}
if (DEBUG1) {System.out.println ("Tally.CheckVote.Checkpoint loop 1");}
	    sum = sum.add (vote.e[i]);
if (DEBUG1) {System.out.println ("Tally.CheckVote.Checkpoint loop 2");}
	    hash.update (vote.a[i].toByteArray ());
if (DEBUG1) {System.out.println ("Tally.CheckVote.Checkpoint loop 3");}
            temp1 = (pub.g.modPow (pub.votes[i], n[power])).modInverse (n[power]);
if (DEBUG2) {System.out.println ("vote(" + i + ")^-1 = " + temp1);}
	    temp1 = (vote.vote.multiply (temp1)).mod (n[power]);
	    temp1 = vote.a[i].multiply (temp1.modPow (vote.e[i], n[power]));
if (DEBUG1) {System.out.println ("Tally.CheckVote.Checkpoint loop 4");}
	    temp1 = temp1.mod (n[power]);
	    temp2 = vote.z[i].modPow (n[1], n[power]);
if (DEBUG1) {System.out.println ("Tally.CheckVote.Checkpoint loop 5");}
if (DEBUG2) {System.out.println ("temp1 = " + temp1 + "\ntemp2 = " + temp2);}
	    if (temp2.compareTo (temp1) != 0) return false;
	}
if (DEBUG1) {System.out.println ("Tally.CheckVote.Checkpoint 2");}
	challenge = hash.digest ();
	temp1 = (new BigInteger (challenge)).mod (pow2);
	temp2 = sum.mod (pow2);
if (DEBUG2) {System.out.println ("temp1 = " + temp1 + "\ntemp2 = " + temp2);}
	if (temp2.compareTo(temp1) != 0) return false;

if (DEBUG1) {System.out.println ("Tally.CheckVote.Checkpoint 3");}
if (DEBUG2) {System.out.println ("return = true");}
	return true;
    }


    public DecodingShare Decode (Vote cip[]) throws NoLegalVotes, 
                                                    NoSuchAlgorithmException {
        Random rand = new Random ();
	DecodingShare share;
	BigInteger temp, res;
	int i;
	BigInteger r, v_rand, m_rand, c, reply;
	byte[] challenge;
	MessageDigest hash =  MessageDigest.getInstance ("SHA");

if (DEBUG1) {System.out.println ("Tally.Decode.Checkpoint 1");}
	i = 0;
	while ((i < cip.length) && (!CheckVote (cip[i]))) i++;
	
if (DEBUG1) {System.out.println ("Tally.Decode.Checkpoint 2");}
	if (i == cip.length) 
	    throw new NoLegalVotes ("All votes had faulty proofs.");

if (DEBUG1) {System.out.println ("Tally.Decode.Checkpoint 3");}
	temp = cip[i++].vote;
	for (; i < cip.length; i++)
	    if (CheckVote (cip[i]))
		temp = (temp.multiply (cip[i].vote)).mod (n[power]);

if (DEBUG1) {System.out.println ("Tally.Decode.Checkpoint 4");}
	res = (temp.multiply (temp)).mod (n[power]);
	res = res.modPow (sec.key, n[power]);

if (DEBUG2) {System.out.println ("msg = " + temp + "\nres = " + res);}
if (DEBUG1) {System.out.println ("Tally.Decode.Checkpoint 5");}
        r = new BigInteger ((n[power].bitLength () + 2*pub.signsize), rand);
	
if (DEBUG2) {System.out.println ("r = " + r);}
if (DEBUG1) {System.out.println ("Tally.Decode.Checkpoint 6");}
	v_rand = pub.verification[0].modPow (r, n[power]);
	m_rand = temp.modPow (r, n[power]);

if (DEBUG2) {System.out.println ("v^r = " + v_rand + "\nm^r = " + m_rand);}
if (DEBUG1) {System.out.println ("Tally.Decode.Checkpoint 7");}
	hash.update (n[1].toByteArray ());
	hash.update (temp.toByteArray ());
	hash.update (res.toByteArray ());
	hash.update (v_rand.toByteArray ());
	hash.update (m_rand.toByteArray ());
	hash.update (pub.verification[0].toByteArray ());
	hash.update (pub.verification[sec.nr].toByteArray ());
	challenge = hash.digest ();

if (DEBUG2 || DEBUG3) {System.out.println ("v_0 = " + pub.verification[0] + "\nv_i = " + 
				 pub.verification[sec.nr] + "\nsecret = " + 
				 sec.key);}
if (DEBUG1) {System.out.println ("Tally.Decode.Checkpoint 8");}
	c = (new BigInteger (challenge)).mod (pow2);

if (DEBUG2) {System.out.println ("challenge = " + challenge + "\nchal = " + c);}
	reply = c.multiply (sec.key);
	reply = reply.add (reply);
	reply = reply.add (r);

if (DEBUG2) {System.out.println ("proof = " + reply);}
if (DEBUG1) {System.out.println ("Tally.Decode.Checkpoint 9");}
	return new DecodingShare (res, v_rand, m_rand, reply, sec.nr);
    }
    
}
