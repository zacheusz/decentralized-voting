package OldVoting;

import Cryptosystem.Cryptosystem.MitchmatchedSizeException;
import java.security.*;
import java.math.*;
import java.util.*;
import java.lang.*;
//import SecretKey;
//import SecretDistributedKey;
//import PublicKey;
import Exception.*;

public class test2 {
    static boolean DEBUG1 = false;
    static boolean DEBUG2 = false;

   // static int virtualvotercount = 10;
    static int votercount = 10;
    static int votecount = 3;
    static int tallycount = 10;
    static int mintallies = 10;
    static int power = 2;
    static int bits = 256;
    static int hashsize = 16;

    public static void main (String argv[]) throws NoLegalVotes, WrongLength, 
                                               IllegalVote, NotEnoughTallies, 
					       NoSuchAlgorithmException, MitchmatchedSizeException {
	PublicKey pub; // the public key is shared by all the voters
	SecretKey sec;
	long start, stop;

	Trusted trusted;//entity doing the key generation
	Voter voter; //entity voting
	Tally[] tallies = new Tally[tallycount];//contains the distributed key shares
	Result res;
	  
	Vote[] ciparray = new Vote[votercount]; //array of cipher texts representing the votes
//	Vote[] virtualciparray;// array of cipher texts representing the scaled version of the votes
	DecodingShare[] tallyarray =  new DecodingShare[tallycount];//contains the decoding shares
	BigInteger msg, temp;
        BigInteger result;
	
	BigInteger n[];
	int i, j;

	String[] vote_names = new String[votecount]; //candidates


	System.out.println ("Generating Votenames...."); //candidates names generation
	for (i = 0; i < votecount; i++)   
	    vote_names[i] = "Vote " + i;

	System.out.println ("Generating Keysystem....");
       	start = (new Date ()).getTime ();

	trusted = new Trusted (bits, power, hashsize, tallycount, mintallies, 64);//generates the secret key
	System.out.println ("Election details....");
        	trusted.produceKeyShares();

	trusted.MakeSelectionElection ("Gore for president?", vote_names);//generates the public key specific for this setup
	pub = trusted.GetPublicKey ();//gets the public key shared between the voters

        stop = (new Date ()).getTime ();
	System.out.println ("Key Generation time per tallier (msec):" + (stop-start));

	System.out.println ("Distributing Secret Keys....");
        for (i = 0; i < tallycount; i++)
	    tallies[i] = new Tally (trusted.GetSecretDistributedKeyPart (i),pub);//returns the distributed key share

	System.out.println ("Voting....");
	start = (new Date ()).getTime ();
	for (i = 0; i < votercount; i++) {
	    voter = new Voter (pub);
	    ciparray[i] = voter.Vote (i % votecount);//vote for arbitrary candidate
            System.out.print ("Vote("+i+")= "+i%votecount);
	    System.out.print (".");
	}
	stop = (new Date ()).getTime ();
	System.out.println ("\nAvg. Vote time (msec):" + (stop-start)/votercount);
	
//	virtualciparray = new Vote[virtualvotercount];
/*	for (i = 0; i < virtualvotercount; i++) //create more virtual voters by duplicating the entries of voter count and not by voting again
	    virtualciparray[i] = ciparray[i % votercount];
 */


	System.out.println ("Combining....");
	start = (new Date ()).getTime ();
	res = new Result (pub);


        temp=ciparray[0].vote;
        for (i = 1; i < votercount; i++) {
	    temp=res.CombineVotes(temp, ciparray[i].vote);
	}

        stop = (new Date ()).getTime ();
	System.out.println ("\nCombining time (msec):" + (stop-start)/votercount);

   /*     System.out.println ("Decrypting....");
	start = (new Date ()).getTime ();
        result=trusted.Decrypt(msg);
        stop = (new Date ()).getTime ();
	System.out.println ("Decryption time (msec):" + (stop-start));
*/
        System.out.println ("Tallying....");

        start = (new Date ()).getTime ();
	for (i = 0; i < tallycount; i++ ) {
            System.out.print (i);
	    tallyarray[i] = tallies[i].Decode (temp); //get the decoding share
	    System.out.print (".");
	}
	stop = (new Date ()).getTime ();
	System.out.println ("\nAvg. Tally time (msec):" + (stop-start)/tallycount);
        msg=res.DistDecryptVotes(tallyarray,temp);
	System.out.println ("Result of election is: " + msg);
	System.out.println ("");

	res.PrintResult (msg);

    }
}
