#!/bin/bash

for ((i =1;i<=9;i++)) do
rm -Rf /home/$LOGIN_NAME/myfiles/tmp/icbc07pc0$i.epfl.ch/package/p2pvoting/bin
done
for ((i =10;i<=80;i++)) do
rm -Rf /home/$LOGIN_NAME/myfiles/tmp/icbc07pc$i.epfl.ch/package/p2pvoting/bin
done
