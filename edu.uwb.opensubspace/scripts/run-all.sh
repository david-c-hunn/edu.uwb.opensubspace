# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# File Name: run-all.sh
# Author:    Dave Hunn      
# Date:      12/16/2012
# Purpose:   This is a bash shell scrypt to run multiple parameter tuning scripts.
#            This scrypt will try to run each argument passed as a shell script.
#

#sh delete-me.sh
#sh clique-synth.sh

#!/bin/bash

#tot_start=$(date +%s)
for var in "$@"
do
	job_start=$(date +%s)
	# declare your intentions
	#echo ""
	#echo "${var} shell script started."
	#echo ""
	
	# run the script
    sh "$var"
	
	#job_end=$(date +%s)   
	#job_diff=$(( $job-end )) 
    # let them know it's over.
    #echo ""
    #echo "${var} shell script finished. Elapse time = $job_diff."
	#echo ""
done

#tot_end=$(date +%s)
#tot_diff=$(( $tot_end - $tot_start ))
# let them know all scripts have been executed
#echo ""
#echo "All jobs finished. Elapse time = $tot_diff."
#echo ""


