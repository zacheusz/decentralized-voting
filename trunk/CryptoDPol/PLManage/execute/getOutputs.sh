#!/bin/bash

# Usage: command nodes_file bootstrap [publicator]

source ../configure.sh


if [ $# -gt 2 ]
then
   echo -e "\E[31;31mERROR: Wrong number of arguments (>2)"; tput sgr0 # red
   echo -e "\E[35;35mThese arguments can be provided: $0 nodes_files date"; tput sgr0 # magenta
   exit 1
fi

if [ $# -lt 2 ]
then
   echo -e "\E[35;35mThese arguments should be provided: $0 nodes_files date"; tput sgr0 # magenta
   nodesFile="../deploy/nodesGoodPLOk"
   DIR=`date +"%y%m%d%H%M%S/"`
else
   nodesFile=$1
   date="-$2"
   DIR=$2
fi

mkdir $DIR

for node in `tac $nodesFile | grep -iv "#" | cut -d ' ' -f 1`
do
#      scp -i $HOME/.ssh/id_rsa -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@$node:/home/$LOGIN_NAME/myfiles/tmp/$node$date.out $DIR &
	sshpass -e scp -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@$node:/home/$LOGIN_NAME/myfiles/tmp/$node$date.out $DIR &
done

#scp -i $HOME/.ssh/id_rsa -o StrictHostKeyChecking=no ${LOGIN_NAME}@$DEFAULT_BOOTSTRAP:/home/$LOGIN_NAME/myfiles/tmp/$DEFAULT_BOOTSTRAP$date.out $DIR 
sshpass -e scp -o StrictHostKeyChecking=no ${LOGIN_NAME}@$DEFAULT_BOOTSTRAP:/home/$LOGIN_NAME/myfiles/tmp/$DEFAULT_BOOTSTRAP$date.out $DIR 
wait

