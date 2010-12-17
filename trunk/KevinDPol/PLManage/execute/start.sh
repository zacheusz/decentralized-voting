#!/bin/bash
source ../configure.sh
delay=10
nb=$1

echo "Running $nb experiments"

#randomly taken ports
bport=22222
#changed this bootstrap to localhost
#bname=peeramidion.irisa.fr

bname=localhost
pport=44444
nodesFile=../deploy/nodesGoodPLOk

cd ../deploy
./deployKevin.sh $DEFAULT_NODEFILE $bname
head -$NB_NODES nodesGoodPL | shuf > $nodesFile

cd ../execute
for ((i=0;i<nb;i++)) do
  sdate="`date +\"%y%m%d%H%M%S\"`"
  shuf $nodesFile > tmp$sdate
  pport_temp=$(($pport+$i))
  echo $pport_temp
  mv tmp$sdate $nodesFile
#   echo 'startedBootstrap'
  ./startKevinBootstrap.sh $bname:$bport $sdate &
#   echo 'endedBootstrapNode'
  sleep $delay
 #  echo 'startedKevinNode'
  ./startKevinNode.sh $nodesFile $bname:$bport $pport_temp $sdate &
# echo 'endedKevinNode'
  wait
done


#commentd out the stats generation for now
#./check.sh
#./check.sh

#for i in 09*; do cd $i; ../getStats.sh > stats; tail -1 stats; cd ..; done


exit 0
