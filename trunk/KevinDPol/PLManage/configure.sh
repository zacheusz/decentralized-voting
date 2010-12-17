export PROJECT_NAME="p2pvoting"
#export LOGIN_NAME="irisa_$PROJECT_NAME"
#changed login name
export LOGIN_NAME="hamza"
export BOOTSTRAPCLASSNAME="launchers.executor.BootstrapLauncher"
export NODELAUNCHERCLASSNAME="launchers.executor.SimpleGossipLauncher"
export GET_VIEW_FROM_BOOTSTRAP_DELAY="50000"
export DEFAULT_NODEFILE="../deploy/nodesPLAllNew"
#export DEFAULT_BOOTSTRAP="peeramidion.irisa.fr"
#changed bootstrap to localhost
export DEFAULT_BOOTSTRAP="localhost"
export SSH_TIMEOUT=30
export BETA=0.05
#tried deploying on fewer nodes
export NB_NODES=8
export NB_MALICIOUS=1
export NB_GROUPS=2
export K=1
export NB_BALLOTS=$((2*$K+1))
