#!/bin/sh

# File Name: mineclus-synth.sh
# Author:    Dave Hunn      
# Date:      3/17/2013
# Purpose:   This is a bash shell script to run parameter tuning on MINECLUS.

# non-algorithm specific settings
dbs="Databases/synth_dbsizescale/*.arff Databases/synth_dimscale/*.arff Databases/synth_noisescale/*.arff"
metrics="Accuracy:CE:ClusterDistribution:Coverage:Entropy:F1Measure:RNIA"

# the algorithm		
clusterer="MineClus"

# algorithm arguments
ALPHA=0.001
BETA=0.1
MAXOUT=-1
K=2
W=50
NUM_BINS=1

ALPHA_OFFSET=10  # multiplicative
BETA_OFFSET=0.1  # additive
K_OFFSET=2       # multiplicative
W_OFFSET=2       # multiplicative

# I assume this file resides in the /scripts directory. Moving one directory up
# should take us into the parent directory where the runnable jar resides
# and the relative paths used in this script will be correct.
cd ..

echo "Running evaluations for ${clusterer}"
for db in $dbs; do
	for in_file in $db; do
		true_file=${in_file/arff/true}
		outfile="output/${clusterer}-synth"
		echo "Starting evaluation of ${in_file}..."
		ALPHA=0.001  # initialize
		for i in {1..3}; do
			BETA=0.1 # initialize		
			for j in {1..4}; do
				K=2  # initialize
				for kk in {1..6}; do
					W=50 # initialize
					for l in {1..3}; do
						echo "Running ${clusterer} with ALPHA=${ALPHA}, BETA=${BETA}, K=${K}, and W=${W}"
						java -Xmx1024m -jar evaluator.jar -sc $clusterer -t $in_file -T $true_file -c last -M $metrics -timelimit 30 -a $ALPHA -b $BETA -m $MAXOUT -k $K -n $NUM_BINS -w $W  -outfile $outfile
						W="$(echo "$W * $W_OFFSET" | bc)"
					done
					K="$(echo "$K * $K_OFFSET" | bc)"	
				done
				BETA="$(echo "$BETA + $BETA_OFFSET" | bc)"	
			done
			ALPHA="$(echo "$ALPHA * $ALPHA_OFFSET" | bc)"
		done
		echo "Finished evaluation of ${in_file}..." 
	done
done
echo "$(date): Finished evaluation of all synthetic data sets for ${clusterer}"

exit 0