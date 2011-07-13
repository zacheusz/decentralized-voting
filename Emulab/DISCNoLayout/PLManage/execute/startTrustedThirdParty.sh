#!/bin/bash
source ../configure.sh

cd ../../$PROJECT_NAME/script/executor/;
./compJava.sh
cd -;


for (( k=200 ; k<1600 ;  k=$(($k+200)) )) do
echo keys$k
mkdir -p keys$k
java -classpath ../../$PROJECT_NAME/bin $TRUSTEDTHIRDPARTYCLASS -votercount $k -votecount $VOTECOUNT -bits $BITS -kvalue $KVALUE
done

