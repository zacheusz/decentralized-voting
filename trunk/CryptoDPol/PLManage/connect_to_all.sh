#!/bin/bash

for ((i =2;i<=73;i++)) do
sshpass -e ssh harkous@icbc07pc$i.epfl.ch
echo $i
done
