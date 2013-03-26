#!/bin/sh

# File Name: run
# Author:    Dave Hunn
# Date:      3/24/2013
# Purpose:   A script to run the paramater tuning scripts.

if  [ ! -d log ]; then
	mkdir log
fi
nohup sh $1 1> "log/${1}.out" 2> "log/${1}.err" < /dev/null &

exit 0