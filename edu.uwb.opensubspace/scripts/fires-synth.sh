# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# File Name: clique-synth.sh
# Author:    Dave Hunn      
# Date:      1/21/2013
# Purpose:   This is a bash shell script to run parameter tuning on FIRES.
#

# non-algorithm specific settings
class_path="i9-weka.jar:weka.jar:i9-subspace.jar:Jama.jar:jsc.jar:commons-math-1.1.jar:vecmath.jar:j3dcore.jar:j3dutils.jar weka.subspaceClusterer.SubspaceClustererEvaluator"
dbs="Databases/synth_dbsizescale/*.arff Databases/synth_dimscale/*.arff Databases/synth_noisescale/*.arff"
metrics="Accuracy:CE:ClusterDistribution:Coverage:Entropy:F1Measure:RNIA"

# the algorithm
clusterer="Fires"

# algorithm arguments
BASE_DBSCAN_EPSILON=1
BASE_DBSCAN_MINPTS=6
GRAPH_K=1
GRAPH_MINCLU=1
GRAPH_MU=1
GRAPH_SPLIT=0.66
POST_DBSCAN_EPSILON=3
POST_DBSCAN_MINPTS=6
PRE_MINIMUMPERCENT=25

GRAPH_K_OFFSET=3
GRAPH_MINCLU_OFFSET=1
GRAPH_MU_OFFSET=3

echo "Running evaluations for FIRES..."

for db in $dbs
do
	for in_file in $db
	do
		true_file=${in_file/arff/true}
		outfile="output/fires-synth"
		
		echo "Starting evaluation of ${in_file}..."
		
		GRAPH_K=1  # initialize
		for i in {1..4}
		do
			GRAPH_MINCLU=1 # initialize		
			for j in {1..4}
			do
				GRAPH_MU=1  # initialize
				for k in {1..4}
				do
					echo "Running ${clusterer} with GRAPH_K=${GRAPH_K}, GRAPH_MINCLU=${GRAPH_MINCLU}, and GRAPH_MU=${GRAPH_MU}."
					java -Xmx1024m -cp $class_path -sc $clusterer -t $in_file -T $true_file -c last -M $metrics -timelimit 30 -BASE_DBSCAN_EPSILON $BASE_DBSCAN_EPSILON -BASE_DBSCAN_MINPTS $BASE_DBSCAN_MINPTS -PRE_MINIMUMPERCENT $PRE_MINIMUMPERCENT -GRAPH_K $GRAPH_K -GRAPH_MU $GRAPH_MU -GRAPH_MINCLU $GRAPH_MINCLU -GRAPH_SPLIT $GRAPH_SPLIT -POST_DBSCAN_EPSILON $POST_DBSCAN_EPSILON -POST_DBSCAN_MINPTS $POST_DBSCAN_MINPTS >> $outfile
					
					GRAPH_MU="$(echo "$GRAPH_MU + $GRAPH_MU_OFFSET" | bc)"
				done
				GRAPH_MINCLU="$(echo "$GRAPH_MINCLU + $GRAPH_MINCLU_OFFSET" | bc)"
			done
			GRAPH_K="$(echo "$GRAPH_K + $GRAPH_K_OFFSET" | bc)"
		done
		echo "Finished evaluation of ${in_file}..." 
	done
done


