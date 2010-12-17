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
   nodesFile="../deploy/nodesGoodPLOk"
   BOOTSTRAP=$DEFAULT_BOOTSTRAP
else
   nodesFile=$1
   BOOTSTRAP=$2
fi

for node in `tac $nodesFile | grep -iv "#" | cut -d ' ' -f 1`
do
      ssh -c arcfour -o Compression=no -x -i $HOME/.ssh/id_rsa -o StrictHostKeyChecking=no ${LOGIN_NAME}@$node "killall -9 java 2> output; if [[ \`wc -l output | cut -d ' ' -f1\` -gt 0 ]]; then echo -e \"\E[35;32mNothing to kill: $node\"; else echo -e \"\E[35;31mKilled java on $node\"; fi; if [[ -e output ]]; then rm output; fi" &
done

# We try to keep the bootstrap at last so that not all the participating nodes have an exception...
# Or maybe we should kill all the nodes once the bootstrap is killed...

echo -e "\E[32;33mKilling java \033[1mbootstrap\033[0m\E[32;33m on $BOOTSTRAP"; tput sgr0 # green + bold (bootstrap)
ssh -c arcfour -o Compression=no -x -i $HOME/.ssh/id_rsa -o StrictHostKeyChecking=no ${LOGIN_NAME}@$BOOTSTRAP "killall java 2>/dev/null;" &

wait
tput sgr0