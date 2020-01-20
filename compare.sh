#!/bin/sh

project=$1

java -jar


mkdir -p ignore/results
mkdir -p ignore/base-results

command=$(echo java -jar -Xmx56G build/libs/TC2P-alpha.jar rx-base -p $project -f $frequency)

(
    echo $command
    echo ""
    timeout -k 10 $timeout $command
) 2>&1 | tee ignore/base-results/$(echo $project)__$frequency.txt
