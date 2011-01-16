#!/bin/bash

# This script allows to launch Simple Gossip executor
#
# Usage: command bootstrap_name:bootstrap_port port timestamp

# creating the bootstrapset file from these info (no need to redeploy the whole thing when changing the bootstrap)
# Everything is set in the boostrap as it is the first script to be run

source ../configure.sh

#alpha=`myrand 0.65 0.70`
#changed this to constant alpha since I didn't have the myrand function
beta=$BETA

function launch () {
	GOSSIP_PORT=$(($GOSSIP_PORT+$3))
#    sshpass -e ssh -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@$node "cd /home/$LOGIN_NAME/myfiles/tmp; echo $BOOTSTRAP $BOOTSTRAP_PORT 0 > package/bootstrapset$node.txt; /home/$LOGIN_NAME/myfiles/tmp/package/jre/bin/java -classpath package/$PROJECT_NAME/bin $NODELAUNCHERCLASSNAME -fileName $node$date.out -bset package/bootstrapset$node.txt -name $node -port $GOSSIP_PORT -alpha 0.7 -beta 1 -decision 0.3 -nbGroups $NB_GROUPS"

rsync -R -p -e --timeout=$RSYNC_TIMEOUT -al --force --delete keys/secKey$1 keys/pubKey /home/$LOGIN_NAME/myfiles/tmp/$node/package/$PROJECT_NAME
#echo "sent secKey$1 to $node in group $2"
#rsync -R -p -e "sshpass -e ssh -l $LOGIN_NAME -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete keys  $LOGIN_NAME@$node:/home/$LOGIN_NAME/myfiles/tmp/$node/package/$PROJECT_NAME
   
#sshpass -e ssh -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@$node "cd /home/$LOGIN_NAME/myfiles/tmp/$node; echo $BOOTSTRAP $BOOTSTRAP_PORT 0 > bootstrapset$node.txt; java -classpath package/$PROJECT_NAME/bin launchers.executor.checkKeys"

  cd /home/$LOGIN_NAME/myfiles/tmp/$node; echo $BOOTSTRAP $BOOTSTRAP_PORT 0 > bootstrapset$node$GOSSIP_PORT.txt; java  -classpath package/$PROJECT_NAME/bin $NODELAUNCHERCLASSNAME -secretKeyFile package/$PROJECT_NAME/keys/secKey$1 -publicKeyFile package/$PROJECT_NAME/keys/pubKey -fileName $node$date$GOSSIP_PORT.out -bset bootstrapset$node$GOSSIP_PORT.txt -name $node -port $GOSSIP_PORT -number $1 -alpha 0.7 -beta 1 -decision 0.3 -nbGroups $NB_GROUPS -groupId $2 -votecount $VOTECOUNT -mintallies $MINTALLIES -shareOrder $1

rm bootstrapset$node$GOSSIP_PORT.txt;
rm $node$date$GOSSIP_PORT.out;

cd -;

##changed this to simply perform the operations on localhost without needing ssh
#cd /home/$LOGIN_NAME/myfiles/tmp; echo $BOOTSTRAP $BOOTSTRAP_PORT 0 > package/bootstrapset.txt; /home/$LOGIN_NAME/myfiles/tmp/package/jre/bin/java -classpath package/$PROJECT_NAME/bin $NODELAUNCHERCLASSNAME -fileName $node$date.out -bset package/bootstrapset.txt -name $node -port $GOSSIP_PORT -alpha 0.7 -beta 1 -decision 0.3 -nbGroups $NB_GROUPS
 # exit=$?;
  #  if [[ $exit -eq 0 ]];
 #   then
#	echo 'launched'
 #   else
#	echo 'failed'
#    fi
}

function launch2 () {
#	GOSSIP_PORT=$(($GOSSIP_PORT+$1))
rsync -R -p -e "sshpass -e ssh -l $LOGIN_NAME -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete keys/secKey$1 keys/pubKey /home/$LOGIN_NAME/myfiles/tmp/$node/package/$PROJECT_NAME
#rsync -R -p -e "sshpass -e ssh -l $LOGIN_NAME -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete keys  $LOGIN_NAME@$node:/home/$LOGIN_NAME/myfiles/tmp/$node/package/$PROJECT_NAME
echo "sent secKey$1 to $node in group $2"
    sshpass -e ssh -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@$node "cd /home/$LOGIN_NAME/myfiles/tmp/$node; echo $BOOTSTRAP $BOOTSTRAP_PORT 0 > bootstrapset$node.txt; java -classpath package/$PROJECT_NAME/bin $NODELAUNCHERCLASSNAME -secretKeyFile package/$PROJECT_NAME/keys/secKey$1 -publicKeyFile package/$PROJECT_NAME/keys/pubKey -fileName $node$date$GOSSIP_PORT.out -bset bootstrapset$node.txt -name $node -port $GOSSIP_PORT -number $1 -alpha 0.7 -beta 0 -decision 0.3 -nbGroups $NB_GROUPS -groupId $2 -votecount $VOTECOUNT -mintallies $MINTALLIES"

# sshpass -e ssh -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@$node "cd /home/$LOGIN_NAME/myfiles/tmp/$node; echo $BOOTSTRAP $BOOTSTRAP_PORT 0 > bootstrapset$node.txt; java -classpath package/$PROJECT_NAME/bin launchers.executor.checkKeys"

##changed this to simply perform the operations on localhost without needing ssh
#cd /home/$LOGIN_NAME/myfiles/tmp; echo $BOOTSTRAP $BOOTSTRAP_PORT 0 > package/bootstrapset.txt; /home/$LOGIN_NAME/myfiles/tmp/package/jre/bin/java -classpath package/$PROJECT_NAME/bin $NODELAUNCHERCLASSNAME -fileName $node$date.out -bset package/bootstrapset.txt -name $node -port $GOSSIP_PORT -alpha 0.7 -beta 0 -decision 0.3 -nbGroups $NB_GROUPS
#  exit=$?;
#    if [[ $exit -eq 0 ]];
#    then
#	echo 'launched'
#    else
#	echo 'failed to launch'
#    fi
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
   sdate=$4
   date="-$sdate"
fi

fileName=`basename $nodesFile`

echo -e "\E[32;32mLaunching the experiment on all nodes"; tput sgr0 # green

i=0
gid=0
j=0
for node in `tac $nodesFile | grep -iv "#" | cut -d ' ' -f 1`
do
 #  echo 'entered for loop'
   gid=$(($i%$NB_GROUPS))
   j=$(($i/$NB_GROUPS))
  echo "gid:" $gid
  echo "j:" $j
#   if [ $(($i)) -lt $(($NB_MALICIOUS)) ]
 #  then
#	echo 'launch'
#gave launch a parameter i in order to change the port number of the deployed node
      launch $j $gid $i&
#      launch &
  # else
#	echo 'launch2'
 #     launch2 &
  #    launch2 $j $gid &
 #  fi
	
   i=$(($i+1))

done
#echo 'exited the loop'
wait

#./getOutputs.sh $nodesFile $sdate

#sort $nodesFile > $sdate/$fileName

#cd $sdate
#ls *.out | sed "s/\t/\n/g" | sed "s/$date.out//g" | sort > list
#cd ..

exit 0
