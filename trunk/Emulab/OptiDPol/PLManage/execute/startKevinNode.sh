#!/bin/bash

# This script allows to launch Simple Gossip executor
#
# Usage: command bootstrap_name:bootstrap_port port timestamp

# creating the bootstrapset file from these info (no need to redeploy the whole thing when changing the bootstrap)
# Everything is set in the boostrap as it is the first script to be run

source ../configure.sh

#alpha=`myrand 0.65 0.70`
alpha=0.70
beta=$BETA

function launch () {

  ssh -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@$node "cd $HOME; /proj/abstracts/jre/bin/java -classpath $PROJECT_NAME/bin $NODELAUNCHERCLASSNAME -bset $HOME/bootstrapset.txt -name $node -port $GOSSIP_PORT -alpha $alpha -beta 1 -decision 0.3 -nbGroups $NB_GROUPS -groupId $2 -nbBallots $NB_BALLOTS"
}

function launch2 () {
  ssh -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@$node "cd $HOME; /proj/abstracts/jre/bin/java -classpath $PROJECT_NAME/bin $NODELAUNCHERCLASSNAME -bset $HOME/bootstrapset.txt -name $node -port $GOSSIP_PORT -alpha $alpha -beta 0 -decision 0.3 -nbGroups $NB_GROUPS -groupId $2 -nbBallots $NB_BALLOTS"

if [ $# -ne 4 ]
then
   echo -e "\e[35;31mNot enough arguments provided"; tput sgr0 # magenta
   exit 1;
else
   nodesFile=$1
   if [[ `expr $2 : "[a-zA-Z.]*:[0-9]*"` ]]
   then
      BOOTSTRAP_PORT=${2#*:}
      echo port: $BOOTSTRAP_PORT
      BOOTSTRAP=${2%:*}
      echo bootstrap: $BOOTSTRAP
   else
      BOOTSTRAP=${2%:*}
      echo $BOOTSTRAP
      echo $BOOTSTRAP_PORT
   fi
   GOSSIP_PORT=$3
   #sdate=$4
   date="-$sdate"
fi

fileName=`basename $nodesFile`

echo -e "\E[32;32mLaunching the experiment on all nodes"; tput sgr0 # green

i=0
gid=0
#j=0
for node in `tac $nodesFile | grep -iv "#" | cut -d ' ' -f 1`
do
   gid=$(($i%$NB_GROUPS))
 
  echo "gid:" $gid
  echo "j:" $j
  if [ $(($i)) -lt $(($NB_MALICIOUS)) ]
  then
     launch $gid $i &
  else
      launch2 $gid $i &
   fi
	
   i=$(($i+1))

done

wait

#./getOutputs.sh $nodesFile $sdate

#sort $nodesFile > $sdate/$fileName

#cd $sdate
#ls *.out | sed "s/\t/\n/g" | sed "s/$date.out//g" | sort > list
#cd ..

exit 0
