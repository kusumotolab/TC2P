#!/bin/sh

project=$1
frequency=$2
timeout=$3

mkdir -p ignore/results

command=$(echo java -jar build/libs/TC2P-alpha.jar mining -p $project -f $frequency | tee ignore/results/$project__$frequency.txt)

echo $command

$command
