# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# File Name: p3c-real.sh
# Author:    Dave Hunn      
# Date:      2/10/2013
# Purpose:   This is a bash shell script to run parameter tuning on P3C.
#

# non-algorithm specific settings
class_path="i9-weka.jar:weka.jar:i9-subspace.jar:Jama.jar:jsc.jar:commons-math-1.1.jar:vecmath.jar:j3dcore.jar:j3dutils.jar weka.subspaceClusterer.SubspaceClustererEvaluator"
dbs="Databases/real_world_data/*.arff"
metrics="Accuracy:CE:ClusterDistribution:Coverage:Entropy:F1Measure:RNIA"

# the algorithm
clusterer="P3c"

# algorithm arguments
possion_offset=10

echo "Running evaluations for ${clusterer}..."

for db in $dbs
do
	for in_file in $db
	do
		true_file=${in_file/arff/true}
		outfile="output/${clusterer}-real"

		echo "Starting evaluation of ${in_file}..."
		alpha="0.001"  # initialize
		for i in {1..1}
		do
			possion=10  # initialize
			for j in {1..10}
			do
				echo "$(date): Running ${clusterer} with alpha=${alpha} and possion=${possion}..."
				java -Xmx1024m -cp $class_path -sc $clusterer -t $in_file -T $true_file -c last -M $metrics -timelimit 30 -alpha $alpha -possion $possion >> $outfile
				possion="$(echo "$possion + $possion_offset" | bc)"
			done
		done
		echo "Finished evaluation of ${in_file}..." 
	done
done

echo "$(date): Finished evaluation of all data sets for ${clusterer}"

