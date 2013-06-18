#!/bin/sh

# File Name: sepc-real.sh
# Author:    Dave Hunn      
# Date:      3/11/2013
# Purpose:   This is a bash shell script to run parameter tuning on SEPC.

# non-algorithm specific settings
db=Databases/real_world_data/*.arff
metrics="Accuracy:CE:ClusterDistribution:Coverage:Entropy:F1Measure:RNIA"

# the algorithm
clusterer="Sepc"

# algorithm arguments
ALPHA=0.001
BETA=0.2
EPSILON=0.001
MIN_SUBSPACE=0.5
MU_0=1
W=5 

ALPHA_OFFSET=10        # multiplicative
BETA_OFFSET=0.05       # additive
W_OFFSET=2             # multiplicative

# I assume this file resides in the /scripts directory. Moving one directory up
# should take us into the parent directory where the runnable jar resides
# and the relative paths used in this script will be correct.
cd ..

echo "Running evaluations for ${clusterer}..."

for in_file in $db; do
	true_file=${in_file/arff/true}
	outfile="output/${clusterer}-real-disjoint"
	
	echo "Starting evaluation of ${in_file}..."
	
	ALPHA=0.001 # initialize		
	for i in {1..3}; do
		BETA=0.2 # initialize
		for j in {1..4}; do
			W=30  # initialize, 5 is the original setting here
			for l in {1..1}; do # originally 1..3, I changed it to check a larger w
				echo "$(date): Running ${clusterer} with ALPHA=${ALPHA} BETA=${BETA}, W=${W}"
				echo java -Xmx1024m -jar evaluator.jar -sc $clusterer -t $in_file -T $true_file -c last -M $metrics -timelimit 30 -a $ALPHA -b $BETA -w $W -e $EPSILON -m $MU_0 -n 0 -s $MIN_SUBSPACE -x true #>> $outfile
				
				W="$(echo "$W * $W_OFFSET" | bc)"
			done
			BETA="$(echo "$BETA + $BETA_OFFSET" | bc)"
		done
		ALPHA="$(echo "$ALPHA * $ALPHA_OFFSET" | bc)"	
	done
	echo "Finished evaluation of ${in_file}..." 
done
echo "$(date): Finished evaluation of all data sets for ${clusterer}"

exit 0