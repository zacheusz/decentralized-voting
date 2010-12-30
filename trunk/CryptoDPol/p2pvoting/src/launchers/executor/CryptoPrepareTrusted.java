package launchers.executor;

import OldVoting.PublicKey;
import OldVoting.SecretKey;
import OldVoting.Trusted;
import java.io.*;
import java.util.HashMap;

import protocol.node.CryptoNode;


public class CryptoPrepareTrusted {

	public static void main(String[] args) throws Exception {

		HashMap<String, String> arguments = new HashMap<String, String>();
		for (int i = 0; i < args.length; i++) {
			arguments.put(args[i], args[i + 1]);
			i++;
		}
                //setup voting
                int VOTERCOUNT = Integer.parseInt(arguments.get("-votercount"));
                CryptoNode.VOTECOUNT = Integer.parseInt(arguments.get("-votecount"));
                int TALLYCOUNT = Integer.parseInt(arguments.get("-tallycount"));
                CryptoNode.MINTALLIES = Integer.parseInt(arguments.get("-mintallies"));
                int CERTAINTY = Integer.parseInt(arguments.get("-certainty"));

                int power = 2;
                int bits = 256;
                int hashsize = 16;
                int i;
                Trusted trusted;
                PublicKey pub; // the public key is shared by all the voters

                String[] vote_names = new String[CryptoNode.VOTECOUNT]; //candidates
                for (i = 0; i < CryptoNode.VOTECOUNT; i++)
                            vote_names[i] = "Vote " + i;

                trusted = new Trusted (bits, power, hashsize, TALLYCOUNT, CryptoNode.MINTALLIES, CERTAINTY);//generates the secret key
                trusted.produceKeyShares();
                trusted.MakeSelectionElection ("Gore for president?", vote_names);//generates the public key specific for this setup
                pub = trusted.GetPublicKey ();//gets the public key shared between the voters

                writeToFile("keys/pubKey",pub);
                SecretKey sec;
                int numGroups=VOTERCOUNT/CryptoNode.MINTALLIES;
       //         int index;
         //       for (int j=0;j<numGroups;j++){
                 for (i =0;i<CryptoNode.MINTALLIES;i++)
                 {
                     sec=trusted.GetSecretDistributedKeyPart(i);             
                     writeToFile("keys/secKey"+i,sec );
                 }
           // }



	}

private static void writeToFile(String filename,Object obj){
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
