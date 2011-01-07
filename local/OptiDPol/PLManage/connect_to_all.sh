#!/bin/bash

for ((i =2;i<=73;i++)) do
sshpass -e ssh $LOGIN_NAME@icbc07pc$i.epfl.ch
echo $i
done
