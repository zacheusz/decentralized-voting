export PROJECT_NAME="p2pvoting"
#export LOGIN_NAME="irisa_$PROJECT_NAME"
#changed login name
export LOGIN_NAME="harkous"
export BOOTSTRAPCLASSNAME="launchers.executor.BootstrapLauncher"
export NODELAUNCHERCLASSNAME="launchers.executor.SimpleGossipLauncher"
#export TRUSTEDTHIRDPARTYCLASS="launchers.executor.CryptoPrepareTrusted"
export GET_VIEW_FROM_BOOTSTRAP_DELAY="50000"
export DEFAULT_NODEFILE="../deploy/nodesPLAllNew"
#export DEFAULT_BOOTSTRAP="peeramidion.irisa.fr"
#changed bootstrap to localhost
export DEFAULT_BOOTSTRAP="icbc07pc02.epfl.ch"
export SSH_TIMEOUT=30
export BETA=0.05
#tried deploying on fewer nodes
export NB_NODES=25
export NB_MALICIOUS=1
export NB_GROUPS=4
export K=1
export NB_BALLOTS=$((2*$K+1))
#export NB_BALLOTS=1
#export VOTECOUNT=3
#export TALLYCOUNT=$NB_NODES
#export VOTERCOUNT=$(($NB_NODES-1))
#export MINTALLIES=$(($VOTERCOUNT/$NB_GROUPS))
#export CERTAINTY=64
