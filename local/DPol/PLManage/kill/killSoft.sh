#!/bin/bash

# Usage: command nodes_file bootstrap [publicator]

source ../configure.sh

simultaneousSSH=30
SSH_TIMEOUT=10

if [ $# -gt 4 ]
then
   echo -e "\E[31;31mERROR: Wrong number of arguments (>3)"; tput sgr0 # red
   echo -e "\E[35;35mThese arguments can be provided: $0 nodes_files bootstrap [node_name]"; tput sgr0 # magenta
   exit 1
fi

if [ $# -lt 4 ]
then
   echo -e "\E[32;32mThese arguments must be provided: $0 nodes_files bootstrap [node_name]"; tput sgr0 # magenta
   nodeFile="../deploy/nodesGoodPLOk"
   PORT=22222
   BOOTSTRAP=$DEFAULT_BOOTSTRAP
   BOOTSTRAP_PORT=12346
else
   nodesFile=$1
   PORT=$2
   BOOTSTRAP=$3
   BOOTSTRAP_PORT=$4
fi

for node in `tac $nodesFile | grep -iv "#" | cut -d ' ' -f 1`
do
    cd "../../p2pvoting/bin/"
    echo -e "\E[32;32mSending STOP_MSG to $node"; tput sgr0 # green
    java launchers.executor.TheKiller -name $node -port $PORT -message "end of simulation" &
done

# We try to keep the bootstrap at last so that not all the participating nodes have an exception...
# Or maybe we should kill all the nodes once the bootstrap is killed...

cd "../../p2pvoting/bin/"
echo -e "\E[32;32mSending STOP_MSG to \033[1mbootstrap\033[0m\E[32;32m on $BOOTSTRAP"; tput sgr0 # green + bold (bootstrap)
java launchers.executor.TheKiller -name $BOOTSTRAP -port $BOOTSTRAP_PORT -message "end of simulation" &

wait
tput sgr0
