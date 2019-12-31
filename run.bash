#!/bin/sh

project=$1
frequency=$2
timeout=$3

mkdir -p ignore/results

command=$(echo java -jar -Xmx56G build/libs/TC2P-alpha.jar mining -p $project -f $frequency)

(
    echo $command
    echo ""
    timeout $timeout $command
) 2>&1 | tee ignore/results/$(echo $project)__$frequency.txt
