export PROJECT_NAME="cryptop2pvoting"
#export LOGIN_NAME="irisa_$PROJECT_NAME"
#changed login name
export LOGIN_NAME="harkous"
export HOME=/users/$LOGIN_NAME
export PROJECT_HOME=$HOME/CryptoDPol
export BINHOME=$PROJECT_HOME/$PROJECT_NAME/bin
export BOOTSTRAPCLASSNAME="launchers.executor.CryptoBootstrapLauncher"
export NODELAUNCHERCLASSNAME="launchers.executor.CryptoGossipLauncher"
export TRUSTEDTHIRDPARTYCLASS="launchers.executor.CryptoPrepareTrusted"
export GET_VIEW_FROM_BOOTSTRAP_DELAY="50000"
export DEFAULT_NODEFILE="../deploy/nodesPLAllNew"
#export DEFAULT_BOOTSTRAP="peeramidion.irisa.fr"
#changed bootstrap to localhost
export DEFAULT_BOOTSTRAP="localhost"
export SSH_TIMEOUT=30
export BETA=0.05
#tried deploying on fewer nodes
export NB_NODES=30
export NB_MALICIOUS=0
export NB_GROUPS=5
export K=1
#export NB_BALLOTS=$((2*$K+1))
export NB_BALLOTS=1
export VOTECOUNT=2
export TALLYCOUNT=$NB_NODES
export VOTERCOUNT=$(($NB_NODES))
export MINTALLIES=$(((($VOTERCOUNT/$NB_GROUPS))))
export CERTAINTY=64
export BITS=128
export bname=node-1
export bport=12346
export pport=22222
export BOOTSTRAP=$bname
export BOOTSTRAP_PORT=$bport
