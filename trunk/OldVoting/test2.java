package OldVoting;

import java.security.*;
import java.math.*;
import java.util.*;
import java.lang.*;
//import SecretKey;
//import SecretDistributedKey;
//import PublicKey;
import Exception.*;

public class test2 {
    static boolean DEBUG1 = true;
    static boolean DEBUG2 = false;

    static int virtualvotercount = 10;
    static int votercount = 10;
    static int votecount = 5;
    static int tallycount = 5;
    static int mintallies = 2;
    static int power = 2;
    static int bits = 256;
    static int hashsize = 16;

    public static void main (String argv[]) throws NoLegalVotes, WrongLength, 
                                               IllegalVote, NotEnoughTallies, 
					       NoSuchAlgorithmException {
	PublicKey pub; // the public key is shared by all the voters
	SecretKey sec;
	long start, stop;

	Trusted trusted;
	Voter voter;
	Tally[] tallies = new Tally[tallycount];
	Result res;
	  
	Vote[] ciparray = new Vote[votercount];
	Vote[] virtualciparray;
	DecodingShare[] tallyarray =  new DecodingShare[tallycount];
	BigInteger msg, temp;
	
	BigInteger n[];
	int i, j;

	String[] vote_names = new String[votecount]; //candidates


	System.out.println ("Generating Votenames...."); //candidates names generation
	for (i = 0; i < votecount; i++)   
	    vote_names[i] = "Vote " + i;

	System.out.println ("Generating Keysystem....");
	trusted = new Trusted (bits, power, hashsize, tallycount, mintallies, 64);
	System.out.println ("Election details....");
	trusted.MakeSelectionElection ("Gore for president?", vote_names);
	pub = trusted.GetPublicKey ();

	System.out.println ("Distributing Secret Keys....");
	for (i = 0; i < tallycount; i++) 
	    tallies[i] = new Tally (trusted.GetSecretDistributedKeyPart (i),pub);

	System.out.println ("Voting....");
	start = (new Date ()).getTime ();
	for (i = 0; i < votercount; i++) {
	    voter = new Voter (pub);
	    ciparray[i] = voter.Vote (i % votecount);
	    System.out.print (".");
	}
	stop = (new Date ()).getTime ();
	System.out.println ("Avg. Vote time (msec):" + (stop-start)/votercount);
	
	virtualciparray = new Vote[virtualvotercount];
	System.out.println ("Tallying....");
	for (i = 0; i < virtualvotercount; i++) 
	    virtualciparray[i] = ciparray[i % votercount];
    
	start = (new Date ()).getTime ();
	for (i = 0; i < tallycount; i++ ) {
	    tallyarray[i] = tallies[i].Decode (virtualciparray);
	    System.out.print (".");
	}
	stop = (new Date ()).getTime ();
	System.out.println ("Avg. Tally time (msec):" + (stop-start)/tallycount);

	System.out.println ("Combining....");
	start = (new Date ()).getTime ();
	res = new Result (pub);

	msg = res.Combine (tallyarray, ciparray);
	stop = (new Date ()).getTime ();
	System.out.println ("Combining time (msec):" + (stop-start)/tallycount);

	System.out.println ("Result of election is: " + msg);
	System.out.println ("");

	res.PrintResult (msg);

    }
}
