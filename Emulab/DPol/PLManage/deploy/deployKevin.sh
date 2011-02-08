#!/bin/bash

# Usage: command nodes_file bootstrap_name [publicator_name]

RSYNC_TIMEOUT="20"
SSH_TIMEOUT="20"
simultaneousRSYNC=100 # it is doubled later (ps a | grep rsync is doubled because of ssh presence)
simultaneousRSYNC=$(($simultaneousRSYNC*2))
DEPLOY_LIST="p2pvoting" # list of projects to deploy (must correspond to folders)

source ../configure.sh

rm -rf nodesGoodPL
touch nodesGoodPL

function myRsync () {
#    rsync -p -e "sshpass -e ssh -c arcfour -l $LOGIN_NAME -i $HOME/.ssh/id_rsa -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete package $LOGIN_NAME@$node:/tmp 2>/dev/null
#rsync -p -e "sshpass -e ssh -c arcfour -l $LOGIN_NAME  -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete package $LOGIN_NAME@$node:/home/$LOGIN_NAME/myfiles/tmp 2>/dev/null
#rsync -p -e "sshpass -e ssh -c arcfour -l $LOGIN_NAME  -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete dummy $LOGIN_NAME@$node:/home/$LOGIN_NAME/myfiles/tmp2 2>/dev/null

rsync -R -p -e "sshpass -e ssh -l $LOGIN_NAME -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete bin $LOGIN_NAME@$node:/home/$LOGIN_NAME/myfiles/tmp/$node/package/$PROJECT_NAME 2>/dev/null


    exit=$?;
    if [[ $exit -eq 0 ]];
    then
	echo $node >> nodesGoodPL
 #   else
#	echo 'failed'
    fi
}

if [ $# -lt 1 ]
then
    echo -e "\e[33;33mThese arguments can be provided: $0 nodes_file bootstrap_name"; tput sgr0 # magenta
    nodesFile=$DEFAULT_NODEFILE
    BOOTSTRAP=$DEFAULT_BOOTSTRAP
    echo -e "\e[33;33mDeploying with $nodesFile and on $BOOTSTRAP"; tput sgr0 # magenta
else
    nodesFile=$1
    BOOTSTRAP=$2
fi

echo -e "\e[32;32mPreparing the package to deploy..."; tput sgr0 # green
if [ ! -e package ]
then
    mkdir package
fi
#rsync -p -al --exclude '.svn' jre package/ 

for project in $DEPLOY_LIST
do
    	ant -f ../../$project/build.xml
#	rsync -p -al --exclude '.svn' ../../$project/bin package/$project
	rsync -p -al --exclude '.svn' ../../$project/bin .
done

echo -e "\e[32;32m\033[1m\rPreparation finished!             "; tput sgr0 # green

nodes=$nodesFile
NBOFDEPLOYEDNODES=$((`cat $nodes | grep -iv "#" | cut -d ' ' -f 1 | wc -l`))
echo -e "\e[32;32mTrying to deploy on $NBOFDEPLOYEDNODES nodes"; tput sgr0 # green
i=0
for node in `cat $nodes | grep -iv "#" | cut -d ' ' -f 1`
do
    while [ true ]
    do
	nbOfConcurrentJobs=`ps a | grep rsync | wc -l`
	if [ $nbOfConcurrentJobs -le $simultaneousRSYNC ]
	then
	     echo -e "\E[32;40mDeploying on $node"; tput sgr0 # green
	    myRsync $i &
	    break
	else
	    sleep 1
	fi
    done
   i=$(($i+1))
done

echo -e "\e[32;32mDeploying on \033[1mbootstrap\033[0m\e[0;32m on $BOOTSTRAP"; tput sgr0 # green + bold (bootstrap)
#rsync -p -e "sshpass -e ssh -c arcfour -l $LOGIN_NAME -i $HOME/.ssh/id_rsa -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete package $LOGIN_NAME@$BOOTSTRAP:/home/$LOGIN_NAME/myfiles/tmp &
rsync -p -e -R "sshpass -e ssh -l $LOGIN_NAME -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete bin $LOGIN_NAME@$BOOTSTRAP:/home/$LOGIN_NAME/myfiles/tmp/$BOOTSTRAP/package/p2pvoting 2>/dev/null
#rsync -p -al --exclude '.svn' --delete package /home/$LOGIN_NAME/myfiles/tmp
#echo rsysnc_END
while [ true ]
do
    nbOfConcurrentJobs=`ps a | grep rsync | wc -l`
    if [ $nbOfConcurrentJobs -le 1 ]
    then
	break
    else
	if [ $(($nbOfConcurrentJobs/2)) -gt 1 ]
	then
            echo -e "\e[32;32m\033[1m\rWaiting for $(($nbOfConcurrentJobs/2)) nodes, `cat nodesGoodPL | wc -l` good nodes so far \c"; tput sgr0 # green
	else
            echo -e "\e[32;32m\033[1m\rWaiting for $(($nbOfConcurrentJobs/2)) node, `cat nodesGoodPL | wc -l` good nodes so far \c"; tput sgr0 # green
	fi
	sleep 0.2
    fi
done

wait

sed s/$BOOTSTRAP/\#$BOOTSTRAP\ is\ the\ bootstrap/ nodesGoodPL > nodesGoodPLaftersed
##changed this to only remove one loalhost and denote it as bootstrap..otherwise all localhosts would be commented out.
#sed '0,/$BOOTSTRAP/s//\#$BOOTSTRAP\ is\ the\ bootstrap/' nodesGoodPL > nodesGoodPLaftersed
mv nodesGoodPLaftersed nodesGoodPL

echo -e "\e[32;32m\033[1m\rDeployed correctly on `cat nodesGoodPL | wc -l` nodes"; tput sgr0 # green
