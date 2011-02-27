#!/bin/bash
source ../configure.sh


cd ../../$PROJECT_NAME/script/executor/;
./compJava.sh
cd -;


START=$(date +%s)
./startTrustedThirdParty.sh
AFTERTRUSTED=$(date +%s)
DIFF1=$(( $AFTERTRUSTED - $START ))
echo "time for trusted 3rd party $DIFF1"

rsync -p -e "ssh -c arcfour -l $LOGIN_NAME -i $SSHHOME_pre -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete keys  $LOGIN_NAME@$proxy_node:$EXECUTE_PATH/keys

cd ../../;
rsync -p -e "ssh -c arcfour -l $LOGIN_NAME -i $SSHHOME_pre -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete $PROJECT_NAME/bin/ $LOGIN_NAME@$home_node:$BINHOME
cd -;

ssh -i $SSHHOME -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@$proxy_node "cd $EXECUTE_PATH; ./start.sh 1 2>&1 |tee results.txt"

