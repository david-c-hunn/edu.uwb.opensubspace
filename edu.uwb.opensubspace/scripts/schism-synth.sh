# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# File Name: schism-synth.sh
# Author:    Dave Hunn      
# Date:      12/16/2012
# Purpose:   This is a bash shell script to run parameter tuning on SCHISM.
#

# non-algorithm specific settings
class_path="i9-weka.jar:weka.jar:i9-subspace.jar:Jama.jar:jsc.jar:commons-math-1.1.jar:vecmath.jar:j3dcore.jar:j3dutils.jar weka.subspaceClusterer.SubspaceClustererEvaluator"
dbs="Databases/synth_dbsizescale/*.arff Databases/synth_dimscale/*.arff Databases/synth_noisescale/*.arff"
metrics="Accuracy:CE:ClusterDistribution:Coverage:Entropy:F1Measure:RNIA"

# the algorithm
clusterer="Schism"

# algorithm arguments
TAU_OFFSET=1000
XI_OFFSET=5

echo "Running evaluations for ${clusterer}..."

for db in $dbs
do
	for in_file in $db
	do
		true_file=${in_file/arff/true}
		outfile="output/${clusterer}-synth"

		echo "Starting evaluation of ${in_file}..."
		TAU="0.000000000001"  # initialize
		for i in {1..5}
		do
			let XI=5  # initialize
			for j in {1..6}
			do
				echo "Running ${clusterer} with XI=${XI} and TAU=${TAU}..."
				java -Xmx1024m -cp $class_path -sc $clusterer -t $in_file -T $true_file -c last -M $metrics -XI $XI -TAU $TAU -timelimit 30 >> $outfile
				XI="$(echo "$XI + $XI_OFFSET" | bc)"
			done
			TAU="$(echo "($TAU) * $TAU_OFFSET" | bc)"
		done
		echo "Finished evaluation of ${in_file}..." 
	done
done



