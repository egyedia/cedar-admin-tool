#!/bin/sh
clear
echo ----------------------------------------------
echo Launching CEDAR Admin tools 0.1.0
echo ----------------------------------------------
echo
DIRNAME=`dirname "$0"`

java -jar $DIRNAME/cedar-admin-tools-0.1.0.jar "$@"