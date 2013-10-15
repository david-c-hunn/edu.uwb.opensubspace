#!/bin/sh

# File Name: sepc-synth.sh
# Author:    Dave Hunn      
# Date:      3/24/2013
# Purpose:   This is a bash shell script to run parameter tuning on SEPC.

# non-algorithm specific settings
dbs="Databases/synth-normal/dims/*.arff Databases/synth-normal/points/*.arff"
metrics="Accuracy:Coverage:Entropy:F1Measure:ClusterDistribution" #:ConfusionMatrix

# the algorithm
clusterer="Sepc"

# algorithm arguments
ALPHA=0.05
BETA=0.2 
EPSILON=0.001
MIN_SUBSPACE=0.5
W="0.1"

W_OFFSET="0.1"            # additive

# I assume this file resides in the /scripts directory. Moving one directory up
# should take us into the parent directory where the runnable jar resides
# and the relative paths used in this script will be correct.
cd ..

echo "Running evaluations for ${clusterer}"

for db in $dbs; do
	for in_file in $db; do
		outfile="output/${clusterer}-synth-norm-3"
		
		echo "Starting evaluation of ${in_file}"
			W="0.1" # initialize
			for j in {1..3}; do
				echo "$(date): Running ${clusterer} W=${W}"
				java -Xmx1024m -jar evaluator.jar -sc $clusterer -t $in_file -outfile $outfile -c last -M $metrics -timelimit 30 -a $ALPHA -b $BETA -w $W -e $EPSILON -n 10 -s $MIN_SUBSPACE -x true 
				W="$(echo "$W + $W_OFFSET" | bc)"
			done
		echo "Finished evaluation of ${in_file}..." 
	done
done
echo "$(date): Finished evaluation of all synthetic data sets for ${clusterer}"

exit 0









