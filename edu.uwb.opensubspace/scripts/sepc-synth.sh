#!/bin/sh

# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# File Name: sepc-synth.sh
# Author:    Dave Hunn      
# Date:      3/11/2013
# Purpose:   This is a bash shell script to run parameter tuning on SEPC.
#

# non-algorithm specific settings
dbs="../Databases/synth_dbsizescale/*.arff ../Databases/synth_dimscale/*.arff ../Databases/synth_noisescale/*.arff"
metrics="Accuracy:CE:ClusterDistribution:Coverage:Entropy:F1Measure:RNIA"

# the algorithm
clusterer="Sepc"

# algorithm arguments
ALPHA=0.001 
BETA=0.1 
EPSILON=0.001
DIM_OVERLAP=0.1 
MAX_OVERLAP=0.5 
MIN_SUBSPACE=0.5
MU_0=1
W=75 

BETA_OFFSET=0.05       # additive
DIM_OVERLAP_OFFSET=0.1 # additive
MAX_OVERLAP_OFFSET=0.1 # additive
W_OFFSET=25            # additive

echo "Running evaluations for ${clusterer}..."

for db in $dbs; do
	for in_file in $db; do
		true_file=${in_file/arff/true}
		outfile="../output/${clusterer}-synth"
		
		echo "Starting evaluation of ${in_file}..."
		
		BETA=0.2 # initialize		
		for i in {1..4}; do
			W=75 # initialize
			for j in {1..2}; do
				DIM_OVERLAP=0.1  # initialize
				for k in {1..5}; do
					MAX_OVERLAP=0.5 # initialize
					for l in {1..5}; do
						echo "$(date): Running ${clusterer} with BETA=${BETA}, W=${W}, DIM_OVERLAP=${DIM_OVERLAP}, and MAX_OVERLAP=${MAX_OVERLAP}"
						java -Xmx1024m -jar ../evaluator.jar -sc $clusterer -t $in_file -T $true_file -outfile outfile -c last -M $metrics -timelimit 30 -a $ALPHA -b $BETA -w $W -e $EPSILON -m $MU_0 -n 0 -o $MAX_OVERLAP -d $DIM_OVERLAP -s $MIN_SUBSPACE -x false 
					
						MAX_OVERLAP="$(echo "$MAX_OVERLAP + $MAX_OVERLAP_OFFSET" | bc)"		
					done
					DIM_OVERLAP="$(echo "$DIM_OVERLAP + $DIM_OVERLAP_OFFSET" | bc)"	
				done
				W="$(echo "$W + $W_OFFSET" | bc)"
			done
			BETA="$(echo "$BETA + $BETA_OFFSET" | bc)"	
		done
		echo "Finished evaluation of ${in_file}..." 
	done
done

echo "$(date): Finished evaluation of all synthetic data sets for ${clusterer}"
