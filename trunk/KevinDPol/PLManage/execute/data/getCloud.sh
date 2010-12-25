#!/bin/bash

for i in 09*; do
    tail -1 $i/stats
done