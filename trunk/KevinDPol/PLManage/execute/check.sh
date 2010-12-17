#!/bin/bash

for i in 09* ; do
    echo $i
    ls $i/*.out | sed "s/\t/\n/g" | sed "s/$i\///g" | sed "s/-$i.out//g" | sort > $i/list
    cat $i/nodesGoodPLOk | sort > $i/nodesGoodPLOksorted
    pb=`diff $i/list $i/nodesGoodPLOksorted | grep -v "#" | grep -e ">" | sed "s/> //g"`
    for j in $pb; do
	if [[ $((`grep $j $i/*.out | wc -l`)) -gt 0 ]]
	then scp irisa_p2pvoting@$j:/tmp/$j-$i.out $i/
	fi
    done
    ls $i/*.out | sed "s/\t/\n/g" | sed "s/$i\///g" | sed "s/-$i.out//g" | sort > $i/list
done
