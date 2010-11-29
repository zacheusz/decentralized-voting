package OldVoting;

import OldVoting.CPublicKey.MessageToBigException;
import java.math.*;
import java.util.*;
import java.security.*;
//import PublicKey
import Exception.*;

public class Voter {

    static boolean DEBUG1 = false; // Structure Debug
    static boolean DEBUG2 = false; // Local Analysis
    static boolean DEBUG3 = false; // Global Analysis
    private PublicKey pub;
    private BigInteger[] n;
    private int power;

    private BigInteger cip, r;
    private BigInteger temp1, temp2;
    private Random rand;
    private BigInteger  r_choice;
    private  byte[] challenge;
    private BigInteger[] a ;
    private BigInteger[] z ;
    private BigInteger[] e;
    private BigInteger response;

    public Voter(PublicKey _pub) {
        int i;

        pub = _pub;
        power = pub.power;
        n = new BigInteger[power + 1];
        n[0] = BigInteger.ONE;
        n[1] = pub.n;
        for (i = 2; i <= power; i++) {
            n[i] = n[i - 1].multiply(n[1]);
        }
    }


    private void encryptVote(int choice) {
        rand= new Random();
        r = new BigInteger(n[power].bitLength(), rand);
        while ((r.compareTo(n[power]) >= 0)
                || ((n[0].compareTo(r.gcd(n[1]))) < 0)) {
            r = new BigInteger(n[power].bitLength(), rand);
        }
        if (DEBUG3) {
            System.out.println("choice = " + choice);
        }
        if (DEBUG2 || DEBUG3) {
            System.out.println("r = " + r);
        }
        temp1 = pub.g.modPow(pub.votes[choice], n[power]);
        temp2 = r.modPow(n[power - 1], n[power]);
        cip = (temp1.multiply(temp2)).mod(n[power]);
    }

    public void produceProof(int choice) throws NoSuchAlgorithmException {
        int power = n.length - 1;
        int votecount = pub.votes.length;
        BigInteger pow2 = (new BigInteger("2")).pow(pub.signsize);

        int i;
        a = new BigInteger[votecount];
        z = new BigInteger[votecount];
        e = new BigInteger[votecount];
        MessageDigest hash = MessageDigest.getInstance("SHA");

        r_choice = BigInteger.ONE;

        hash.update(n[1].toByteArray());
        for (i = 0; i < votecount; i++) {
            if (i != choice) {
                z[i] = new BigInteger(n[power].bitLength(), rand);
                while (z[i].compareTo(n[power]) >= 0) {
                    z[i] = new BigInteger(n[power].bitLength(), rand);
                }

                if (DEBUG2) {
                    System.out.println("z'[" + i + "] = " + z[i]);
                }
                e[i] = new BigInteger(pub.signsize, rand);
                if (DEBUG2) {
                    System.out.println("e'[" + i + "] = " + e[i]);
                }
                temp2 = pub.g.modPow(pub.votes[i], n[power]);
                temp2 = temp2.modInverse(n[power]);
                if (DEBUG2) {
                    System.out.println("vote(" + i + ")^-1 = " + temp2);
                }
                temp2 = (cip.multiply(temp2)).mod(n[power]);
                if (DEBUG2) {
                    System.out.println("v(" + i + ") = " + temp2);
                }
                temp2 = temp2.modPow(e[i], n[power]);
                if (DEBUG2) {
                    System.out.println("v(" + i + ")^e = " + temp2);
                }
                temp2 = temp2.modInverse(n[power]);
                if (DEBUG2) {
                    System.out.println("v(" + i + ")^-e = " + temp2);
                }
// Changed from n[1] to n[power-1]                   ==========
                a[i] = (temp2.multiply(z[i].modPow(n[power - 1], n[power]))).mod(n[power]);
                if (DEBUG2) {
                    System.out.println("a'[" + i + "] = " + a[i]);
                }
            } else {
                temp2 = new BigInteger(n[power].bitLength(), rand);
                while (temp2.compareTo(n[power]) >= 0) {
                    temp2 = new BigInteger(n[power].bitLength(), rand);
                }
                if (DEBUG2) {
                    System.out.println("temp2(" + i + ") = " + temp2);
                }
// Changed from n[1] to n[power-1]   ==========
                a[i] = temp2.modPow(n[power - 1], n[power]);
                if (DEBUG2) {
                    System.out.println("a[" + i + "] = " + a[i]);
                }
                r_choice = temp2;
            }
            hash.update(a[i].toByteArray());
        }
        challenge = hash.digest();
        if (DEBUG2) {
            System.out.println("challenge: " + challenge);
        }


        temp1 = (new BigInteger(challenge)).mod(pow2);
        if (DEBUG2) {
            System.out.println("challenge(BI): " + temp1);
        }
        for (i = 0; i < votecount; i++) {
            if (i != choice) {
                temp1 = (temp1.subtract(e[i])).mod(pow2);
            }
        }

        e[choice] = temp1;
        if (DEBUG2) {
            System.out.println("e[" + choice + "] = " + e[choice]);
        }



        temp2 = r.modPow(e[choice], n[power]);
        if (DEBUG2) {
            System.out.println("r = " + r);
        }
        if (DEBUG2) {
            System.out.println("temp2 = " + temp2);
        }
        if (DEBUG2) {
            System.out.println("r_choice = " + r_choice);
        }
        z[choice] = (r_choice.multiply(temp2)).mod(n[power]);
        if (DEBUG2) {
            System.out.println("z[" + choice + "] = " + z[choice]);
        }

    }

    public Vote Vote(int choice) throws IllegalVote, NoSuchAlgorithmException {
      
        Vote vote;
 
        int power = n.length - 1;
        int votecount = pub.votes.length;
     

        r_choice = BigInteger.ONE;
        /* Check that it's a legal plaintext */
        if ((choice >= votecount) || (choice < 0)) {
            throw new IllegalVote("Vote index is out of range");
        }
        encryptVote(choice);

        if (DEBUG2) {
            System.out.println("encrypted vote(" + choice + ") = " + cip);
        }

        produceProof(choice);
        vote = new Vote(cip, a, challenge, z, e);

        return vote;
    }

    public Vote CVote(int choice,CPublicKey key) throws IllegalVote, NoSuchAlgorithmException, MessageToBigException {

        Vote vote;

        int power = n.length - 1;
        int votecount = pub.votes.length;


        r_choice = BigInteger.ONE;
        /* Check that it's a legal plaintext */
        if ((choice >= votecount) || (choice < 0)) {
            throw new IllegalVote("Vote index is out of range");
        }
        //cip=key.Encrypt(new BigInteger(new Integer(choice).toString()));
        cip=key.Encrypt(pub.votes[choice]);
        if (DEBUG2) {
            System.out.println("encrypted vote(" + choice + ") = " + cip);
        }
  //      produceProof(choice);


        vote = new Vote(cip, a, challenge, z, e);

        return vote;
    }

}
