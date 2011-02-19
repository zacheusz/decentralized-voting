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
    rsync -p -e "ssh -c arcfour -l $LOGIN_NAME -i $SSHHOME -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete keys/secKey$1 keys/pubKey  $LOGIN_NAME@$node:$PROJECT_HOME/keys/
    ssh -i $SSHHOME -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@$node "$JAVA_ -classpath $BINHOME $NODELAUNCHERCLASSNAME -bset $PROJECT_HOME/bootstrapset.txt -name $node_local_name -port $GOSSIP_PORT -alpha $alpha -beta 1 -decision 0.3 -nbGroups $NB_GROUPS -secretKeyFile $PROJECT_HOME/keys/secKey$1 -publicKeyFile $PROJECT_HOME/keys/pubKey -groupId $2 -votecount $VOTECOUNT -mintallies $MINTALLIES -number $1 -nbBallots $NB_BALLOTS -shareOrder $1"
}

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
j=0
for node in `tail -r $nodesFile | grep -iv "#" | cut -d ' ' -f 1`
do
   gid=$(($i%$NB_GROUPS))
   j=$(($i/$NB_GROUPS))
   echo "gid:" $gid
   echo "j:" $j
   node_local_name=`expr match "$node" '\(node-.*[0-9]\)'`
 #  if [ $(($i)) -lt $(($NB_MALICIOUS)) ]
  # then
      launch $j $gid $i &
   #else
   #   launch2 &
   #fi
   i=$(($i+1))
done

wait

#./getOutputs.sh $nodesFile $sdate

#sort $nodesFile > $sdate/$fileName

#cd $sdate
#ls *.out | sed "s/\t/\n/g" | sed "s/$date.out//g" | sort > list
#cd ..

exit 0

