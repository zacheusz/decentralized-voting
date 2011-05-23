#!/bin/bash
source ../configure.sh

i=5;
node=0;

for bits in 128 256 512 1024
do
	for servers in 25 50 100 200 400 800 1600
	do
		for threshold in 5 10 20 40 80
		do
		node=$(($node%50+1));
		if [ $threshold -lt $servers ]; then
		./startRemoteTimingStats.sh $bits $servers $threshold $i 2 "node-$node.$experiment.abstracts.emulab.net" 2>&1 |tee "b$bits-s$servers-t$threshold.txt";
		echo "b$bits-s$servers-t$threshold.txt">>done.txt;
		fi
		done
	done
done

