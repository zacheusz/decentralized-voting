#!/bin/bash

# Usage: command nodes_file bootstrap [publicator]

source ../configure.sh

simultaneousSSH=30
SSH_TIMEOUT=10

if [ $# -gt 3 ]
then
   echo -e "\E[31;31mERROR: Wrong number of arguments (>3)"; tput sgr0 # red
   echo -e "\E[35;35mThese arguments can be provided: $0 nodes_files bootstrap [node_name]"; tput sgr0 # magenta
   exit 1
fi

if [ $# -lt 2 ]
then
   echo -e "\E[35;35mThese arguments can be provided: $0 nodes_files bootstrap [node_name]"; tput sgr0 # magenta
   nodesFile=$DEFAULT_NODEFILE
   BOOTSTRAP=$DEFAULT_BOOTSTRAP
else
   nodesFile=$1
   BOOTSTRAP=$2
fi

for node in `tac $nodesFile | grep -iv "#" | cut -d ' ' -f 1`
do
      sshpass -e ssh -c arcfour -o Compression=no -x -i $HOME/.ssh/id_rsa -o StrictHostKeyChecking=no ${LOGIN_NAME}@$node "echo -e \"\E[32;33mClearing log files on $node\"; rm -f /home/$LOGIN_NAME/myfiles/tmp/*.out" &
done

# We try to keep the bootstrap at last so that not all the participating nodes have an exception...
# Or maybe we should kill all the nodes once the bootstrap is killed...

echo -e "\E[32;33mClearing log files \033[1mbootstrap\033[0m\E[32;33m on $BOOTSTRAP"; tput sgr0 # green + bold (bootstrap)
sshpass -e ssh -c arcfour -o Compression=no -x -i $HOME/.ssh/id_rsa -o StrictHostKeyChecking=no ${LOGIN_NAME}@$BOOTSTRAP "rm -f /home/$LOGIN_NAME/myfiles/tmp/*.out" &

wait
tput sgr0
