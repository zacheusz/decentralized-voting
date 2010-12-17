#!/bin/bash
source ../configure.sh
delay=10
nb=$1

echo "Running $nb experiments"

bport=12346
bname=peeramidion.irisa.fr
pport=22222
nodesFile=../deploy/nodesGoodPLOk

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

./check.sh
./check.sh

for i in 09*; do cd $i; ../getStats.sh > stats; tail -1 stats; cd ..; done


exit 0
