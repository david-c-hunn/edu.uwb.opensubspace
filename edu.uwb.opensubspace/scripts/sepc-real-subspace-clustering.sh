# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# File Name: sepc-real-subspace.sh
# Author:    Dave Hunn      
# Date:      3/11/2013
# Purpose:   This is a bash shell script to run parameter tuning on SEPC.
#

# non-algorithm specific settings
class_path="i9-weka.jar:weka.jar:i9-subspace.jar:Jama.jar:jsc.jar:commons-math-1.1.jar:vecmath.jar:j3dcore.jar:j3dutils.jar weka.subspaceClusterer.SubspaceClustererEvaluator"
dbs="Databases/real_world_data/*.arff"
metrics="Accuracy:CE:ClusterDistribution:Coverage:Entropy:F1Measure:RNIA"

# the algorithm
clusterer="Sepc"

# algorithm arguments
ALPHA=0.001 
BETA=0.1 # to 0.4 step 0.1
EPSILON=0.001
DIM_OVERLAP=0.1 # to 0.5 step 0.1
MAX_OVERLAP=0.5 # to 0.9 step 0.1
MIN_SUBSPACE=0.5
MU_0=1
W=5 # 

BETA_OFFSET=0.1        # additive
DIM_OVERLAP_OFFSET=0.1 # additive
MAX_OVERLAP_OFFSET=0.1 # additive
W_OFFSET=2             # multiplicative

echo "Running evaluations for ${clusterer}..."

for db in $dbs; do
	for in_file in $db; do
		true_file=${in_file/arff/true}
		outfile="output/${clusterer}-real-subspace"
		
		echo "Starting evaluation of ${in_file}..."
		
		BETA=0.1 # initialize		
		for i in {1..4}; do
			W=5 # initialize
			for j in {1..3}; do
				DIM_OVERLAP=0.1  # initialize
				for k in {1..5}; do
					MAX_OVERLAP=0.5 # initialize
					for l in {1..5}; do
						echo "$(date): Running ${clusterer} with BETA=${BETA}, W=${W}, DIM_OVERLAP=${DIM_OVERLAP}, and MAX_OVERLAP=${MAX_OVERLAP}"
						java -Xmx1024m -cp $class_path -sc $clusterer -t $in_file -T $true_file -c last -M $metrics -timelimit 30 -a $ALPHA -b $BETA -w $W -e $EPSILON -m $MU_0 -n 0 -o $MAX_OVERLAP -d $DIM_OVERLAP -s $MIN_SUBSPACE -x false >> $outfile
					
						MAX_OVERLAP="$(echo "$MAX_OVERLAP + $MAX_OVERLAP_OFFSET" | bc)"		
					done
					DIM_OVERLAP="$(echo "$DIM_OVERLAP + $DIM_OVERLAP_OFFSET" | bc)"	
				done
				W="$(echo "$W * $W_OFFSET" | bc)"
			done
			BETA="$(echo "$BETA + $BETA_OFFSET" | bc)"	
		done
		echo "Finished evaluation of ${in_file}..." 
	done
done

echo "$(date): Finished evaluation of all data sets for ${clusterer}"
