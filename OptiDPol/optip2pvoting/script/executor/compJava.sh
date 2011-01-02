#!/bin/bash

if [ -e ../../bin ]
then
   rm -rf ../../bin
fi

mkdir ../../bin

find ../../src/ -iname "*.java" | xargs javac -source 1.6 -classpath ../../src -d ../../bin
