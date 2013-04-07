#!/bin/sh

# File Name: doc-synth.sh
# Author:    Dave Hunn      
# Date:      3/17/2013
# Purpose:   This is a bash shell script to run parameter tuning on DOC.
#

# non-algorithm specific settings
dbs="Databases/synth_dbsizescale/*.arff Databases/synth_dimscale/*.arff Databases/synth_noisescale/*.arff"
# dbs="Databases/synth_dimscale/*.arff Databases/synth_noisescale/*.arff"
metrics="Accuracy:CE:ClusterDistribution:Coverage:Entropy:F1Measure:RNIA"

# the algorithm
clusterer="Doc"

# algorithm arguments
ALPHA=0.01
BETA=0.2
MAXITER=1024
K=16
W=100

# I assume this file resides in the /scripts directory. Moving one directory up
# should take us into the parent directory where the runnable jar resides
# and the relative paths used in this script will be correct.
cd ..

echo "Running evaluations for ${clusterer}"
for db in $dbs; do
	for in_file in $db;	do
		true_file=${in_file/arff/true}
		outfile="output/${clusterer}-runtime"
		echo "Starting evaluation of ${in_file}"
		echo "$(date): Running ${clusterer} with ALPHA=${ALPHA}, BETA=${BETA}, K=${K}, and W=${W}"
		java -Xmx1024m -jar evaluator.jar -sc $clusterer -t $in_file -T $true_file -c last -M $metrics -timelimit 30 -a $ALPHA -b $BETA -m $MAXITER -k $K -w $W -outfile $outfile
	done
done
echo "$(date): Finished evaluation of all data sets for ${clusterer}"

exit 0