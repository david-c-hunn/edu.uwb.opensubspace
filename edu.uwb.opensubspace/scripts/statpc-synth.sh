# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# File Name: statpc-synth.sh
# Author:    Dave Hunn      
# Date:      2/10/2013
# Purpose:   This is a bash shell script to run parameter tuning on STATPC.
#            Note, that STATPC uses the same range of arguments for synthetic and 
#            real world data sets. So, a single script is sufficient to perform
#            parameter tuning.

# non-algorithm specific settings
class_path="i9-weka.jar:weka.jar:i9-subspace.jar:Jama.jar:jsc.jar:commons-math-1.1.jar:vecmath.jar:j3dcore.jar:j3dutils.jar weka.subspaceClusterer.SubspaceClustererEvaluator"
dbs="Databases/synth_dbsizescale/*.arff Databases/synth_dimscale/*.arff Databases/synth_noisescale/*.arff Databases/real_world_data/*.arff"
metrics="Accuracy:CE:ClusterDistribution:Coverage:Entropy:F1Measure:RNIA"

# the algorithm
clusterer="Statpc"

alpha0_offset=10000
alphah_offset=10000
alphak_offset=10000

echo "Running evaluations for ${clusterer}..."

for db in $dbs; do
	for in_file in $db; do
		true_file=${in_file/arff/true}
		outfile="output/${clusterer}-real"
		
		echo "Starting evaluation of ${in_file}..."
		
		alpha0="0.00000000000000000001"  # initialize
		for i in {1..6}; do
			alphah="0.00000000000000000001" # initialize		
			for j in {1..6}; do
				alphak="0.00000000000000000001"  # initialize
				for k in {1..6}; do
					echo "Running ${clusterer} with alpha0=${alpha0}, alphah=${alphah}, and alphak=${alphak}."
					java -Xmx1024m -cp $class_path -sc $clusterer -t $in_file -T $true_file -c last -M $metrics -timelimit 30 -alpha0 $alpha0 -alphah $alphah -alphak $alphak >> $outfile
					
					alphak="$(echo "$alphak * $alphak_offset" | bc)"
				done
				alphah="$(echo "$alphah * $alphah_offset" | bc)"
			done
			alpha0="$(echo "$alpha0 * $alpha0_offset" | bc)"
		done
		echo "Finished evaluation of ${in_file}..." 
	done
done

echo "$(date): Finished evaluation of all data sets for ${clusterer}"

