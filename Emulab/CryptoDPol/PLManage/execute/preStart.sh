#!/bin/bash
source ../configure.sh


START=$(date +%s)
./startTrustedThirdParty.sh
AFTERTRUSTED=$(date +%s)
DIFF1=$(( $AFTERTRUSTED - $START ))
echo "time for trusted 3rd party $DIFF1"

cd ../../$PROJECT_NAME/script/executor/;
./compJava.sh
cd -;


rsync -p -e "ssh -c arcfour -l $LOGIN_NAME -i $SSHHOME -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete keys  $LOGIN_NAME@$proxy_node:$EXECUTE_PATH/keys

cd ../../;
rsync -p -e "ssh -c arcfour -l $LOGIN_NAME -i $SSHHOME -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete $PROJECT_NAME/bin/ $LOGIN_NAME@node-1.Polling.abstracts.emulab.net:$BINHOME
cd -;

ssh -i $SSHHOME -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@ops.emulab.net "$EXECUTE_PATH/start.sh 1 > results.txt"

