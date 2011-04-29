#!/bin/bash
source ../configure.sh
delay=10
nb=$1

echo "Running $nb experiments"



nodesFile=../deploy/nodesGoodPLOk

#node=$bname
cd ../../$PROJECT_NAME/script/executor/;
./compJava.sh
cd -;

ssh -i $SSHHOME -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@$home_node "pkill java;mkdir -p $PROJECT_HOME; echo $bname $bport 0 > $PROJECT_HOME/bootstrapset.txt"
cd ../../;
rsync -p -e "ssh -c arcfour -l $LOGIN_NAME -i $SSHHOME -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete $PROJECT_NAME/bin/ $LOGIN_NAME@$home_node:$BINHOME
cd -;

cd ../deploy;


#head -$NB_NODES nodesGoodPL | shuf > $nodesFile

head -$NB_NODES nodesGoodPL > $nodesFile



cd -;

START=$(date +%s)
./startTrustedThirdParty.sh
AFTERTRUSTED=$(date +%s)
DIFF1=$(( $AFTERTRUSTED - $START ))
echo "time for trusted 3rd party $DIFF1"

rsync -p -e "ssh -c arcfour -l $LOGIN_NAME -i $SSHHOME -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete keys/secKey*  $LOGIN_NAME@$home_node:$PROJECT_HOME/keys/

sleep $delay

for ((i=0;i<nb;i++)) do

#  sdate="`date +\"%y%m%d%H%M%S\"`"
#  shuf $nodesFile > tmp$sdate
#  mv tmp$sdate $nodesFile
 # ./startKevinBootstrap.sh $bname:$bport stamp &
 # sleep $delay
  ./startKevinNode.sh $nodesFile $bname:$bport $pport stamp &
  wait
done

END=$(date +%s)


DIFF2=$(( $END - $AFTERTRUSTED ))


echo "time for voting $DIFF2"

#commentd out the stats generation for now
#./check.sh
#./check.sh

#for i in 10*; do cd $i; ../getStats.sh > stats; tail -1 stats; cd ..; done


exit 0
