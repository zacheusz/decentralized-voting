package testingPaillier;

import paillierp.Paillier;
import paillierp.PaillierThreshold;

import java.math.BigInteger;
import java.util.Random;
import paillierp.key.KeyGen;
import paillierp.key.PaillierPrivateThresholdKey;
import zkp.DecryptionZKP;

public class Testing {

    public static void main(String[] args) {

        System.out.println(" Create new keypairs .");
        Random rnd = new Random();
        PaillierPrivateThresholdKey[] keys =
                KeyGen.PaillierThresholdKey(256, 6, 2, rnd.nextLong());
        System.out.println(" Six keys are generated , with a threshold of 3.");

        System.out.println(" Six people use their keys : p1 , p2 , p3 , p4 , p5 , p6 ");
        PaillierThreshold p1 = new PaillierThreshold(keys[0]);
        PaillierThreshold p2 = new PaillierThreshold(keys[1]);
        PaillierThreshold p3 = new PaillierThreshold(keys[2]);
        PaillierThreshold p4 = new PaillierThreshold(keys[3]);
        PaillierThreshold p5 = new PaillierThreshold(keys[4]);
        PaillierThreshold p6 = new PaillierThreshold(keys[5]);

        System.out.println(" Alice is given the public key .");
        Paillier alice = new Paillier(keys[0].getPublicKey());
// Alice encrypts a message
 ////////////////////////////////////////////////////
        int candidatesLength=4;
        BigInteger[] votes = new BigInteger[candidatesLength]; //a vector with same length as the candidates
	int bits;
	BigInteger base, temp;
	int i;

	votes[0] = BigInteger.ONE;
        //n[pow-1] is n^s
        System.out.println("ns length: "+keys[0].getPublicKey().getNS().bitLength());
        System.out.println("ns+1 length: "+keys[0].getPublicKey().getNSPlusOne().bitLength());
	bits = keys[0].getPublicKey().getNS().bitLength() / candidatesLength;
	base = (new BigInteger ("2")).pow (bits);
	temp = base;

	for (i = 1; i < candidatesLength; i++) {
	    votes[i] = temp;
	    temp = temp.multiply (base);
	}
        
////////////////////////////////////////////////////

        BigInteger msg = BigInteger.valueOf(3);
        BigInteger Emsg = alice.encrypt(votes[3]);
        System.out.println(" Alice encrypts the message " + msg + " and sends "
                + Emsg + " to everyone .");
// Alice sends Emsg to everyone
/*
        System.out.println(" p1 receives the message and tries to decrypt all alone :");
        BigInteger p1decrypt = p1.decryptOnly(Emsg);
        if (p1decrypt.equals(msg)) {
            System.out.println(" p1 succeeds decrypting the message all alone .");
        } else {
            System.out.println(" p1 fails decrypting the message all alone . :(");
        }

        System.out.println(" p2 and p3 receive the message and "
                + " create a partial decryptions .");
        DecryptionZKP p2share = p2.decryptProof(Emsg);
        DecryptionZKP p3share = p3.decryptProof(Emsg);
// p2 sends the partial decryption to p3
        // p3 sends the partial decryption to p2
        System.out.println(" p2 receives the partial p3 's partial decryption "
                + "and attempts to decrypt the whole message using its own "
                + " share twice ");
        try {
            BigInteger p2decrypt = p2.combineShares(p2share, p3share, p2share);
            if (p2decrypt.equals(msg)) {
                System.out.println(" p2 succeeds decrypting the message with p3 .");
            } else {
                System.out.println(" p2 fails decrypting the message with p3. :(");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(" p2 fails decrypting and throws an error ");
        }*/
        System.out.println("p4 , p5 , p6 receive Alice 's original message and "
                + " create partial decryptions .");
        DecryptionZKP p4share = p4.decryptProof(Emsg);
        DecryptionZKP p5share = p5.decryptProof(Emsg);
        DecryptionZKP p6share = p6.decryptProof(Emsg);
// p4 , p5 , and p6 share each of their partial decryptions with each other

        System.out.println(" p4 receives and combines each partial decryption "
                + " to decrypt whole message :");

        BigInteger p4decrypt = p4.combineShares(p4share, p6share);
        getResult(p4decrypt,candidatesLength,votes);
        BigInteger p5decrypt = p4.combineShares(p5share, p6share);
        getResult(p5decrypt,candidatesLength,votes);
        BigInteger p6decrypt = p4.combineShares(p6share, p4share );
        getResult(p6decrypt,candidatesLength,votes);

  /*      if (p4decrypt.equals(msg)) {

            System.out.println(" p4 succeeds decrypting the message with p5 and p6.");
        } else {
            System.out.println(" p4 fails decrypting the message with p5 and p6.:(");
        }*/
    }

    public static void  getResult(BigInteger result, int candidatesLength,BigInteger[] votes){

        BigInteger temp;
        //String []vote_names=new String[candidatesLength];

        for (int i = 0; i < candidatesLength; i++) {
		int strlen = candidatesLength;
		System.out.print ("vote " + i+ ":");

		if (i < (candidatesLength-1))
		    temp = result.mod (votes[i+1]);
		else
		    temp = result;
		temp = temp.divide (votes[i]);
		System.out.println (" " + temp);
	    }
    }
}
