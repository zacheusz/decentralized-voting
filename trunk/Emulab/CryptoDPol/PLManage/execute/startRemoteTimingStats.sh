#!/bin/bash

source ../configure.sh

cd ../../$PROJECT_NAME/script/executor/;
./compJava.sh
cd -;
cd ../../;

rsync -p -e "ssh -c arcfour -l $LOGIN_NAME -i $SSHHOME -o StrictHostKeyChecking=no -o ConnectTimeout=$SSH_TIMEOUT -o Compression=no -x" --timeout=$RSYNC_TIMEOUT -al --force --delete $PROJECT_NAME/bin/ $LOGIN_NAME@$home_node:$BINHOME
cd -;
ssh -i $SSHHOME -o ConnectTimeout=$SSH_TIMEOUT -o StrictHostKeyChecking=no ${LOGIN_NAME}@$home_node "java -classpath $BINHOME paillierp.testingPaillier.Testing -bitsnum 1024 -servers 10 -threshold 5 -rounds 3 -candidatesLength 2"



