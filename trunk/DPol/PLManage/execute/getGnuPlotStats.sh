#!/bin/bash

std=`for i in 10*; do cd $i; tail -1 stats; cd ..; done | cut -d' ' -f5 | myaverage`
ignore=`for i in 10*; do cd $i; tail -1 stats; cd ..; done | cut -d' ' -f6 | myaverage`
propright=`for i in 10*; do cd $i; tail -1 stats; cd ..; done | cut -d' ' -f7 | myaverage`

echo "$std $ignore $propright"
