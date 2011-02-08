source ../configure.sh
for ((i=0;i<10;i++)) do
START=$(date +%s)
java -classpath ../../$PROJECT_NAME/bin $TRUSTEDTHIRDPARTYCLASS -votercount $VOTERCOUNT -votecount $VOTECOUNT -tallycount $TALLYCOUNT -mintallies $MINTALLIES -certainty $CERTAINTY -bits $BITS

AFTERTRUSTED=$(date +%s)
DIFF1=$(( $AFTERTRUSTED - $START ))
echo $DIFF1
done
