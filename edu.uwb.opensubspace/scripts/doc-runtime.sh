#!/bin/sh

# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# File Name: bench-doc-synth.sh
# Author:    Dave Hunn      
# Date:      3/11/2013
# Purpose:   This is a bash shell script to run parameter tuning on DOC.
#

# non-algorithm specific settings
class_path="i9-weka.jar:weka.jar:i9-subspace.jar:Jama.jar:jsc.jar:commons-math-1.1.jar:vecmath.jar:j3dcore.jar:j3dutils.jar weka.subspaceClusterer.SubspaceClustererEvaluator"
dbs="Databases/synth_dbsizescale/*.arff Databases/synth_dimscale/*.arff Databases/synth_noisescale/*.arff"
metrics="Accuracy:CE:ClusterDistribution:Coverage:Entropy:F1Measure:RNIA"

# the algorithm
clusterer="Doc"

# algorithm arguments
ALPHA=0.01
BETA=0.2
MAXITER=1024
K=32
W=100

echo "Running evaluations for DOC..."
outfile="output/doc-runtime-synth"

for db in $dbs; do
	for in_file in $db; do
		true_file=${in_file/arff/true}		
		for i in {1..10}; do
			echo "Running ${clusterer} with ALPHA=${ALPHA}, BETA=${BETA}, K=${K}, and W=${W}."
			java -Xmx1024m -cp $class_path -sc $clusterer -t $in_file -T $true_file -c last -M $metrics -timelimit 30 -APHA $ALPHA -BETA $BETA -MAXITER $MAXITER -k $K -w $W >> $outfile
		done
		echo "Finished evaluation of ${in_file}..." 
	done
done


