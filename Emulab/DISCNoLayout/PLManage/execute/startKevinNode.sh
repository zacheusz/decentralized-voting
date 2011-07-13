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
#    rsync -p -e "ssh -c arcfour -l $LOGIN_NAME -i $SSHHOME -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete keys/secKey$1  $LOGIN_NAME@$node:$PROJECT_HOME/keys/
if [ $1 -eq $GOSSIP_PORT ]
then
    ssh -i $SSHHOME -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@$node "mkdir -p $BINHOME2 ;cp -r $BINHOME/* $BINHOME2 ;pkill java; $JAVA_ -classpath $BINHOME2 $NODELAUNCHERCLASSNAME -bset $PROJECT_HOME/bootstrapset.txt -name $node_local_name -port $1 -alpha $alpha -beta 1 -decision 0.3 -secretKeyFile $PROJECT_HOME/keys/secKey -votecount $VOTECOUNT -nbBallots $NB_BALLOTS -kvalue $KVALUE -nodesPerMachine $nodesPerMachine -basicPort $GOSSIP_PORT -nbVoters $VOTERCOUNT -order $2 -epsilon $EPSILON -logging $LOGGING"
else
ssh -i $SSHHOME -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@$node "$JAVA_ -classpath $BINHOME2 $NODELAUNCHERCLASSNAME -bset $PROJECT_HOME/bootstrapset.txt -name $node_local_name -port $1 -alpha $alpha -beta 1 -decision 0.3 -secretKeyFile $PROJECT_HOME/keys/secKey -votecount $VOTECOUNT -nbBallots $NB_BALLOTS -kvalue $KVALUE -nodesPerMachine $nodesPerMachine -basicPort $GOSSIP_PORT -nbVoters $VOTERCOUNT -order $2 -epsilon $EPSILON -logging $LOGGING"
fi
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

i=1
gid=0
j=0
#thresh=$((( 0.5 - $EPSILON ) * $VOTERCOUNT ))
#echo $thresh
for (( k=0 ; k<$nodesPerMachine ; k++)) do
for node in `tac $nodesFile | grep -iv "#" | cut -d ' ' -f 1`
do
  # gid=$(($i%$NB_GROUPS))
  # j=$(($i/$NB_GROUPS))
  # echo "gid:" $gid
 #  echo "j:" $j
   node_local_name=`expr match "$node" '\(node-[0-9]*\)'`
#   if [ $(($i)) -lt $(($thresh)) ]
#then
#      launch  $(( $GOSSIP_PORT + $k  )) $i &

#else
      launch  $(( $GOSSIP_PORT + $k  )) $i &
#   fi
   i=$(($i+1))
   sleep 0.4

done
done
wait

#./getOutputs.sh $nodesFile $sdate

#sort $nodesFile > $sdate/$fileName

#cd $sdate
#ls *.out | sed "s/\t/\n/g" | sed "s/$date.out//g" | sort > list
#cd ..

exit 0

