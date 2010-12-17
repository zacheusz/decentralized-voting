    package OldVoting;

import Cryptosystem.Cryptosystem.MitchmatchedSizeException;
import java.math.*;
import Exception.*;

public class PublicKey {
    static boolean DEBUG1 = false;
    static boolean DEBUG2 = false;

    public BigInteger g;    // the generator g
    public BigInteger n;    // the modulo base
    public int power;       // the modulo power
    public int k;           // Number of honest tallies needed to decrypt
    public int l;           // Number of tallies
    public int signsize;    // Signature size
    public BigInteger[] verification;   // Verification array for the l tallies

    public BigInteger[] votes;       // The base for the votes
    public String[] vote_names;      // The name of the choice

    public String ElectionQuestion;  // The election question

    // Change to only use get methods and make them private.

    public PublicKey (BigInteger _g, BigInteger _n, int _power, int _signsize,
		      String _ElectionQuestion,
		      BigInteger[] _votes, String[] _vote_names, 
		      BigInteger[] _verification) throws WrongLength {

if (DEBUG1) {System.out.println ("PublicKey.Checkpoint 1");}
        if (_vote_names.length != _votes.length)
	    throw new WrongLength ("Size of Arrays don't match");

if (DEBUG1) {System.out.println ("PublicKey.Checkpoint 2");}
	k = l = 1;
	g = _g;
	n = _n;
	power = _power;
	ElectionQuestion = _ElectionQuestion;
	votes = _votes;
	vote_names = _vote_names;
	signsize = _signsize;
if (DEBUG1) {System.out.println ("PublicKey.Checkpoint 3");}
    }

    public PublicKey (BigInteger _g, BigInteger _n, int _power, int _signsize,
		      int _k, int _l, String _ElectionQuestion, 
		      BigInteger[] _votes, String[] _vote_names, 
		      BigInteger[] _verification) throws WrongLength {
	
if (DEBUG1) {System.out.println ("PublicKey.2.Checkpoint 1");}
if (DEBUG2) {System.out.println ("names.length = " + _vote_names.length +
				 "\nvotes.length = " + _votes.length);}
	if (_vote_names.length != _votes.length) 
	    throw new WrongLength ("Size of Vote Arrays don't match");
if (DEBUG1) {System.out.println ("PublicKey.2.Checkpoint 2");}
if (DEBUG2) {System.out.println ("verification.length = " + 
				 _verification.length + "\nl = " + _l);}
	if (_verification.length != _l+1) 
	    throw new WrongLength ("Size of Verification Array don't match");

if (DEBUG1) {System.out.println ("PublicKey.2.Checkpoint 3");}
	k = _k;
	l = _l;
	g = _g;
	n = _n;
	power = _power;
	ElectionQuestion = _ElectionQuestion;
	votes = _votes;
	vote_names = _vote_names;
	verification = _verification;
	signsize = _signsize;
if (DEBUG1) {System.out.println ("PublicKey.2.Checkpoint 4");}
    }



}
