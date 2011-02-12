#!/bin/bash
source ../configure.sh
delay=10
nb=$1

echo "Running $nb experiments"



nodesFile=../deploy/nodesGoodPLOk

node=$bname
#cd ../../$PROJECT_NAME/script/executor/;
#./compJava.sh
#cd -;

ssh -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@$node "echo $BOOTSTRAP $BOOTSTRAP_PORT 0 > $HOME/bootstrapset.txt"
cd ../../;
rsync -R -p -e "ssh -c arcfour -l $LOGIN_NAME -i $HOME/.ssh/id_rsa -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete $PROJECT_NAME/bin $LOGIN_NAME@$node:$HOME/$PROJECT_NAME
cd -;

cd ../deploy;


head -$NB_NODES nodesGoodPL | shuf > $nodesFile



cd ../execute;

START=$(date +%s)
./startTrustedThirdParty.sh
AFTERTRUSTED=$(date +%s)
DIFF1=$(( $AFTERTRUSTED - $START ))
echo "time for trusted 3rd party $DIFF1"

for ((i=0;i<nb;i++)) do
#  sdate="`date +\"%y%m%d%H%M%S\"`"
#  shuf $nodesFile > tmp$sdate
#  mv tmp$sdate $nodesFile
  ./startKevinBootstrap.sh $bname:$bport  &
  sleep $delay
  ./startKevinNode.sh $nodesFile $bname:$bport $pport  &
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
