package paillierp.testingPaillier;

import paillierp.Paillier;
import paillierp.PaillierThreshold;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import paillierp.PartialDecryption;
import paillierp.key.KeyGen;
import paillierp.key.PaillierPrivateThresholdKey;
import zkp.DecryptionZKP;
import launchers.executor.*;

public class Testing {

    public static void main(String[] args) {
        HashMap<String, String> arguments = new HashMap<String, String>();
        for (int i = 0; i < args.length; i++) {
            arguments.put(args[i], args[i + 1]);
            i++;
        }
//         int bits_num =128;
//        int servers = 5;
//        int threshold = 3;
//        int rounds = 2;
//        int candidatesLength = 2;


      
        int bits_num = Integer.parseInt(arguments.get("-bitsnum"));
        int servers = Integer.parseInt(arguments.get("-servers"));
        int threshold = Integer.parseInt(arguments.get("-threshold"));
        int rounds = Integer.parseInt(arguments.get("-rounds"));
        int candidatesLength = Integer.parseInt(arguments.get("-candidatesLength"));

  //      System.out.println(" Create new keypairs .");
        PaillierPrivateThresholdKey[] keys = null;
        long startInstant;
        long[] preTime = new long[rounds];

        long[] genTime = new long[rounds];
        long[] encTime = new long[rounds];
        long[] sharedecTime = new long[rounds];
        long[] decTime = new long[rounds];
        long[] addTime = new long[rounds];
        long[] postTime = new long[rounds];

        PaillierThreshold[] p = new PaillierThreshold[servers];
        
        for (int i = 0; i < rounds; i++) {
            startInstant = System.nanoTime();
            Random rnd = new Random();
            keys = KeyGen.PaillierThresholdKey(bits_num, servers, threshold, rnd.nextLong());
            for (int k=0;k<servers;k++)
            {
                p[k]= new PaillierThreshold(keys[k]);
            }
            genTime[i] = System.nanoTime()-startInstant;
        }

           launchers.executor.CryptoPrepareTrusted.writeToFile("keys/pkeys",p );
           paillierp.testingPaillier.TestingRest.printArray(genTime,"genTime");
        //       System.out.println(" Six keys are generated , with a threshold of 3.");

        /*     System.out.println(" Six people use their keys : p1 , p2 , p3 , p4 , p5 , p6 ");
        PaillierThreshold p1 = new PaillierThreshold(keys[0]);
        PaillierThreshold p2 = new PaillierThreshold(keys[1]);
        PaillierThreshold p3 = new PaillierThreshold(keys[2]);
        PaillierThreshold p4 = new PaillierThreshold(keys[3]);
        PaillierThreshold p5 = new PaillierThreshold(keys[4]);
        PaillierThreshold p6 = new PaillierThreshold(keys[5]);
        
        System.out.println(" Alice is given the public key .");
         */

// Alice encrypts a message
        ///////////////////////////////////////////////////
        /*
        BigInteger Emsg = null;
        BigInteger[] votes = null;
        Paillier alice = null;
        
        for (int j = 0; j < rounds; j++) {
            startInstant = System.nanoTime();
            votes = new BigInteger[candidatesLength]; //a vector with same length as the candidates
            int bits;
            BigInteger base, temp;
            int i;

            votes[0] = BigInteger.ONE;
            //n[pow-1] is n^s
            //   System.out.println("ns length: "+keys[0].getPublicKey().getNS().bitLength());
            //   System.out.println("ns+1 length: "+keys[0].getPublicKey().getNSPlusOne().bitLength());
            bits = keys[0].getPublicKey().getNS().bitLength() / candidatesLength;
            base = (new BigInteger("2")).pow(bits);
            temp = base;

            for (i = 1; i < candidatesLength; i++) {
                votes[i] = temp;
                temp = temp.multiply(base);
            }
            preTime[j] = System.nanoTime()-startInstant ;

            
            startInstant = System.nanoTime();
            alice = new Paillier(keys[0].getPublicKey());

          //  BigInteger msg = BigInteger.valueOf(0);
             Emsg = alice.encrypt(votes[0]);

            encTime[j] = System.nanoTime()-startInstant ;
        }
        
        for (int j = 0; j < rounds; j++) {
            startInstant = System.nanoTime();

            Emsg=alice.add(Emsg, Emsg);
            
           addTime[j] = System.nanoTime()-startInstant;

        }
        ////////////////////////////////////////
        PartialDecryption [] pshare=new PartialDecryption[threshold];
        
        for (int i=0;i<rounds;i++){
            
            startInstant = System.nanoTime();

            for (int k=0;k<threshold;k++){
                
                pshare[k]=p[k].decrypt(Emsg);
            }
            
            sharedecTime[i] = System.nanoTime()/threshold-startInstant;

            startInstant = System.nanoTime();

             BigInteger pdecrypt =p[0].combineShares(pshare);
            
            decTime[i] = System.nanoTime()-startInstant;
            startInstant = System.nanoTime();

            getResult(pdecrypt, candidatesLength, votes);
            postTime[i] = System.nanoTime()-startInstant;

        }
        
        printArray (preTime,"preTime");
        printArray(genTime,"genTime");
        printArray(addTime, "addTime");
        printArray(decTime,"addTime");
        printArray(postTime,"postTime");
        
        
        //    System.out.println(" Alice encrypts the message " + msg + " and sends "
        //           + Emsg + " to everyone .");
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
  /*      System.out.println("p4 , p5 , p6 receive Alice 's original message and "
                + " create partial decryptions .");
        DecryptionZKP p4share = p4.decryptProof(Emsg);
        DecryptionZKP p5share = p5.decryptProof(Emsg);
        DecryptionZKP p6share = p6.decryptProof(Emsg);
// p4 , p5 , and p6 share each of their partial decryptions with each other

        System.out.println(" p4 receives and combines each partial decryption "
                + " to decrypt whole message :");

        BigInteger p4decrypt = p4.combineShares(p4share, p6share);
        getResult(p4decrypt, candidatesLength, votes);
        BigInteger p5decrypt = p4.combineShares(p5share, p6share);
        getResult(p5decrypt, candidatesLength, votes);
        BigInteger p6decrypt = p4.combineShares(p6share, p4share);
        getResult(p6decrypt, candidatesLength, votes);

        /*      if (p4decrypt.equals(msg)) {
        
        System.out.println(" p4 succeeds decrypting the message with p5 and p6.");
        } else {
        System.out.println(" p4 fails decrypting the message with p5 and p6.:(");
        }*/
        
    }
    /*
    public static void printArray(long [] A,String name){
        System.out.print(name+": ");
        for (int i=0;i<A.length;i++){
            System.out.print(A[i]+" ");
        }
        System.out.println('\n');
    }
    public static void getResult(BigInteger result, int candidatesLength, BigInteger[] votes) {

        BigInteger temp;
        //String []vote_names=new String[candidatesLength];

        for (int i = 0; i < candidatesLength; i++) {
           // int strlen = candidatesLength;
   //         System.out.print("vote " + i + ":");

            if (i < (candidatesLength - 1)) {
                temp = result.mod(votes[i + 1]);
            } else {
                temp = result;
            }
            temp = temp.divide(votes[i]);
     //       System.out.println(" " + temp);
        }
    }*/
}
