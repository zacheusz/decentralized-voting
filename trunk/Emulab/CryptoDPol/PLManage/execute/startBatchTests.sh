#!/bin/bash
source ../configure.sh

i=1;
node=0;

for bits in 128 256 512 
#1024 2048
do
	for servers in 25 50 100 200 400 800 1600
	do
		for threshold in 5 10 20 40 80 160 320
		do
		node=$(($node%50+1));
		if [ $threshold -lt $servers ]; then
		./startRemoteTimingStats.sh $bits $servers $threshold $i 2 "node-$node.$experiment.abstracts.emulab.net" 2>&1 |tee "b$bits-s$servers-t$threshold.txt"
		fi
		done
	done
done
