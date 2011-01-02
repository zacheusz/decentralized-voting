#!/bin/bash
source ../configure.sh
delay=10
nb=$1

echo "Running $nb experiments"

#randomly taken ports
bport=$(($RANDOM +10000))
#changed this bootstrap to localhost
#bname=peeramidion.irisa.fr

bname=icbc07pc02.epfl.ch
pport=$(($RANDOM +10000))
nodesFile=../deploy/nodesGoodPLOk



cd ../deploy
./deployKevin.sh $DEFAULT_NODEFILE $bname
head -$NB_NODES nodesGoodPL | shuf > $nodesFile

START=$(date +%s)





cd ../execute
./startTrustedThirdParty.sh
AFTERTRUSTED=$(date +%s)
DIFF1=$(( $AFTERTRUSTED - $START ))
echo "time for trusted 3rd party $DIFF1"
for ((i=0;i<nb;i++)) do
  sdate="`date +\"%y%m%d%H%M%S\"`"
  shuf $nodesFile > tmp$sdate
  mv tmp$sdate $nodesFile

  ./startKevinBootstrap.sh $bname:$bport $sdate &
  sleep $delay
  ./startKevinNode.sh $nodesFile $bname:$bport $pport $sdate &


  wait
done
END=$(date +%s)


DIFF2=$(( $END - $AFTERTRUSTED ))


echo "time for voting $DIFF2"

#commentd out the stats generation for now
./check.sh
./check.sh

for i in 10*; do cd $i; ../getStats.sh > stats; tail -1 stats; cd ..; done


exit 0
