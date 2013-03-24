# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# File Name: proclus-synth.sh
# Author:    Dave Hunn      
# Date:      12/16/2012
# Purpose:   This is a bash shell script to run parameter tuning on SCHISM.
#

# non-algorithm specific settings
class_path="i9-weka.jar:weka.jar:i9-subspace.jar:Jama.jar:jsc.jar:commons-math-1.1.jar:vecmath.jar:j3dcore.jar:j3dutils.jar weka.subspaceClusterer.SubspaceClustererEvaluator"
dbs="Databases/synth_dbsizescale/*.arff Databases/synth_dimscale/*.arff Databases/synth_noisescale/*.arff"
metrics="Accuracy:CE:ClusterDistribution:Coverage:Entropy:F1Measure:RNIA"

# the algorithm
clusterer="Proclus"

# algorithm arguments
avgerageDimensions_offset=2
numberOfClusters_offset=2

echo "Running evaluations for ${clusterer}..."

for db in $dbs
do
	for in_file in $db
	do
		true_file=${in_file/arff/true}
		outfile="output/${clusterer}-synth"

		echo "Starting evaluation of ${in_file}..."
		avgerageDimensions=1  # initialize
		for i in {1..38}
		do
			numberOfClusters=2  # initialize
			for j in {1..6}
			do
				echo "Running ${clusterer} with avgerageDimensions=${avgerageDimensions} and numberOfClusters=${numberOfClusters}..."
				java -Xmx1024m -cp $class_path -sc $clusterer -t $in_file -T $true_file -c last -M $metrics -timelimit 30 -avgerageDimensions $avgerageDimensions -numberOfClusters $numberOfClusters >> $outfile
				numberOfClusters="$(echo "$numberOfClusters * $numberOfClusters_offset" | bc)"
			done
			avgerageDimensions="$(echo "$avgerageDimensions + $avgerageDimensions_offset" | bc)"
		done
		echo "Finished evaluation of ${in_file}..." 
	done
done



