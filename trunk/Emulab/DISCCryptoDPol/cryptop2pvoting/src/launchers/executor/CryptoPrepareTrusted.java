package launchers.executor;
/*
import OldVoting.PublicKey;
import OldVoting.SecretKey;
import OldVoting.Trusted;*/
import java.io.*;
import java.util.HashMap;
import java.util.Random;
import paillierp.PaillierThreshold;
import paillierp.key.KeyGen;
import paillierp.key.PaillierPrivateThresholdKey;

import protocol.node.CryptoNode;


public class CryptoPrepareTrusted {

	public static void main(String[] args) throws Exception {

		HashMap<String, String> arguments = new HashMap<String, String>();
		for (int i = 0; i < args.length; i++) {
			arguments.put(args[i], args[i + 1]);
			i++;
		}
                //setup voting
                CryptoNode.VOTERCOUNT = Integer.parseInt(arguments.get("-votercount"));
                CryptoNode.VOTECOUNT = Integer.parseInt(arguments.get("-votecount"));
                //int TALLYCOUNT = Integer.parseInt(arguments.get("-tallycount"));
                CryptoNode.numClusters = (int) (Math.log(CryptoNode.VOTERCOUNT) / (CryptoNode.kvalue * Math.log(CryptoNode.VOTERCOUNT)));
                CryptoNode.nodesPerCluster = CryptoNode.VOTERCOUNT / CryptoNode.numClusters;
                CryptoNode.MINTALLIES = CryptoNode.nodesPerCluster / 2 + 1;
                //    int CERTAINTY = Integer.parseInt(arguments.get("-certainty"));
                int bits = Integer.parseInt(arguments.get("-bits"));
             /*   int power = 2;
               
                int hashsize = 16;
                
                Trusted trusted;
                PublicKey pub; // the public key is shared by all the voters
                */
                int i;
                String[] vote_names = new String[CryptoNode.VOTECOUNT]; //candidates
                for (i = 0; i < CryptoNode.VOTECOUNT; i++)
                            vote_names[i] = "Vote " + i;

                /*trusted = new Trusted (bits, power, hashsize, TALLYCOUNT, CryptoNode.MINTALLIES, CERTAINTY);//generates the secret key
                trusted.produceKeyShares();
                trusted.MakeSelectionElection ("Gore for president?", vote_names);//generates the public key specific for this setup
                pub = trusted.GetPublicKey ();//gets the public key shared between the voters
*/
                System.out.println(" Create new keypairs ."+"npc: "+CryptoNode.nodesPerCluster+ "mint: "+ CryptoNode.MINTALLIES);
                Random rnd = new Random();
                
                PaillierPrivateThresholdKey[] keys =KeyGen.PaillierThresholdKey(bits, CryptoNode.nodesPerCluster, CryptoNode.MINTALLIES, rnd.nextLong());


         //       writeToFile("keys/pubKey",pub);
             //   SecretKey sec;
                PaillierThreshold p;
                //int numGroups=VOTERCOUNT/CryptoNode.MINTALLIES;
       //         int index;
         //       for (int j=0;j<numGroups;j++){
                 for (i =0;i<CryptoNode.MINTALLIES;i++)
                 {
                     //sec=trusted.GetSecretDistributedKeyPart(i);
                     p=new PaillierThreshold(keys[i]);
                     writeToFile("keys/secKey"+i,p );
                 }
           // }



	}

public static void writeToFile(String filename,Object obj){
                 FileOutputStream fos = null;
                 ObjectOutputStream out = null;
                 try
                 {
                   fos = new FileOutputStream(filename);
                   out = new ObjectOutputStream(fos);
                   out.writeObject(obj);
                   out.close();
                 }
                 catch(IOException ex)
                 {
                   ex.printStackTrace();
                 }
    }
	static void printUsage() {
		System.out
				.println("SimpleGossipLauncher -name inet_name -p port"
						+ "-bset boostrapset_file" + "-fileName output_filename");
	}


}
