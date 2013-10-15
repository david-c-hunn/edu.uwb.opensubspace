#!/bin/sh

# File Name: sepc-synth.sh
# Author:    Dave Hunn      
# Date:      3/24/2013
# Purpose:   This is a bash shell script to run parameter tuning on SEPC.

# non-algorithm specific settings
metrics="Accuracy:Entropy:F1Measure"

# the algorithm
clusterer="Sepc"

# algorithm arguments
ALPHA=0.05
BETA=0.2 
EPSILON=0.001
MIN_SUBSPACE=0.5
W="0.1"

# I assume this file resides in the /scripts directory. Moving one directory up
# should take us into the parent directory where the runnable jar resides
# and the relative paths used in this script will be correct.
cd ..

echo "Running evaluations for ${clusterer}"

in_file="Databases/synth-normal/dims/D20.arff"
outfile="output/${clusterer}-synth-variance"

for i in {1..20}; do	
	java -Xmx1024m -jar evaluator.jar -sc $clusterer -t $in_file -outfile $outfile -c last -M $metrics -timelimit 30 -a $ALPHA -b $BETA -w $W -e $EPSILON -n 10 -s $MIN_SUBSPACE -x true 
done
echo "$(date): Finished evaluation of all synthetic data sets for ${clusterer}"

exit 0