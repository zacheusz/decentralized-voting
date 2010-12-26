java -classpath ../../$PROJECT_NAME/bin $TRUSTEDTHIRDPARTYCLASS -votercount $NB_NODES -votecount $VOTECOUNT -tallycount $TALLYCOUNT -mintallies $MINTALLIES -certainty $CERTAINTY

java -classpath ../../p2pvoting/bin CryptoPrepareTrusted -votercount 10 -votecount 3 -tallycount 10 -mintallies 5 -certainty 64
