#!/bin/bash

cd `dirname $0`

set PWD=$(echo `pwd`)

gvim -c "set runtimepath+=$PWD" &>/dev/null &

exit
