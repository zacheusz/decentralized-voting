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
import protocol.communication.CRYPTO_BALLOT_MSG;
import protocol.node.CryptoNode;
import runtime.executor.E_CryptoNodeID;

public class TestingCombined {

    public static void main(String[] args) {
            HashMap<String, String> arguments = new HashMap<String, String>();
        for (int i = 0; i < args.length; i++) {
            arguments.put(args[i], args[i + 1]);
            i++;
        }
         int bits_num =512;
        int servers = 3;
        int threshold = 1;
        int rounds = 1;
        int candidatesLength = 2;


      
//        int bits_num = Integer.parseInt(arguments.get("-bitsnum"));
//        int servers = Integer.parseInt(arguments.get("-servers"));
//        int threshold = Integer.parseInt(arguments.get("-threshold"));
//        int rounds = Integer.parseInt(arguments.get("-rounds"));
//        int candidatesLength = Integer.parseInt(arguments.get("-candidatesLength"));

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

//           launchers.executor.CryptoPrepareTrusted.writeToFile("keys/pkeys",p );
           paillierp.testingPaillier.TestingRest.printArray(genTime,"genTime");
       
   //     System.out.println(" Create new keypairs .");
          
        int [] sizes=new int[rounds];



// Alice encrypts a message
        ///////////////////////////////////////////////////
        
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
            bits = p[0].getPublicKey().getNS().bitLength() / candidatesLength;
            base = (new BigInteger("2")).pow(bits);
            temp = base;

            for (i = 1; i < candidatesLength; i++) {
                votes[i] = temp;
                temp = temp.multiply(base);
            }
            preTime[j] = System.nanoTime()-startInstant ;

            
            startInstant = System.nanoTime();
            alice = new Paillier(p[0].getPublicKey());

          //  BigInteger msg = BigInteger.valueOf(0);
             Emsg = alice.encrypt(votes[0]);
             E_CryptoNodeID id = new E_CryptoNodeID("node-1.polling1.abstracts.emulab.net", 23414,false);
             CRYPTO_BALLOT_MSG mes = new CRYPTO_BALLOT_MSG(id, id, Emsg);


            System.out.println("size: "+CryptoNode.getObjectSize(mes));
            encTime[j] = System.nanoTime()-startInstant ;
         //   sizes[j]=Emsg.toByteArray().length;

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
            
            sharedecTime[i] = (System.nanoTime()-startInstant)/threshold;

            startInstant = System.nanoTime();

             BigInteger pdecrypt =p[0].combineShares(pshare);
            
            decTime[i] = System.nanoTime()-startInstant;
            startInstant = System.nanoTime();

            getResult(pdecrypt, candidatesLength, votes);
            postTime[i] = System.nanoTime()-startInstant;

        }
        
        printArray (preTime,"preTime");
//        printArray(genTime,"genTime");
       printArray(encTime, "encTime");
        printArray(addTime, "addTime");
        printArray(sharedecTime,"sharedecTime");

        printArray(decTime,"decTime");
        printArray(postTime,"postTime");
  //      printArray(sizes, "size");
        
        
        
        
    }
    public static void printArray(long [] A,String name){
        System.out.print(name+": ");
        for (int i=0;i<A.length;i++){
            System.out.print(A[i]/1000000.0+" ");
        }
        System.out.println('\n');
    }
        public static void printArray(int [] A,String name){
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
            System.out.print("vote " + i + ":");

            if (i < (candidatesLength - 1)) {
                temp = result.mod(votes[i + 1]);
            } else {
                temp = result;
            }
            temp = temp.divide(votes[i]);
            System.out.println(" " + temp);
        }
    }
}
