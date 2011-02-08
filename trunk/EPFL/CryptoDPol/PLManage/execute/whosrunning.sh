#!/bin/bash

source ../configure.sh
ps aux | grep ssh | grep "irisa_$PROJECT_NAME@" | cut -d'@' -f2 | cut -d':' -f1 | cut -d' ' -f1
ps aux | grep scp | grep "irisa_$PROJECT_NAME@" | cut -d'@' -f2 | cut -d':' -f1 | cut -d' ' -f1