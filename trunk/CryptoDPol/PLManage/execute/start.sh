#!/bin/bash
source ../configure.sh
delay=10
nb=$1

echo "Running $nb experiments"

#randomly taken ports
bport=39222
#changed this bootstrap to localhost
#bname=peeramidion.irisa.fr

bname=icbc07pc02.epfl.ch
pport=34366
nodesFile=../deploy/nodesGoodPLOk

./startTrustedThirdParty.sh

cd ../deploy
./deployKevin.sh $DEFAULT_NODEFILE $bname
head -$NB_NODES nodesGoodPL | shuf > $nodesFile

cd ../execute
for ((i=0;i<nb;i++)) do
  sdate="`date +\"%y%m%d%H%M%S\"`"
  shuf $nodesFile > tmp$sdate
  mv tmp$sdate $nodesFile

  ./startKevinBootstrap.sh $bname:$bport $sdate &
  sleep $delay
  ./startKevinNode.sh $nodesFile $bname:$bport $pport $sdate &


  wait
done


#commentd out the stats generation for now
#./check.sh
#./check.sh

#for i in 10*; do cd $i; ../getStats.sh > stats; tail -1 stats; cd ..; done


exit 0
