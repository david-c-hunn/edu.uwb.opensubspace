#
# File Name: evsl.sh
# Author:    Dave Hunn      
# Date:      8/14/2016
# Purpose:   Bash shell script to drive Evaluator.jar
#

# non-algorithm specific settings
jar="build/jar/evaluator.jar"
dbs="Databases/real*/*.arff"
metrics="F1Measure"
# Accuracy:CE:ClusterDistribution:Coverage:Entropy:RNIA
# the algorithm
clusterer="Clon"

# algorithm arguments
kOffset=10
minLenOffset=5

echo "Running evaluations for ${clusterer}..."

for db in $dbs
do
	for in_file in $db
	do
		true_file=${in_file/arff/true}
		outfile="output/clon-synth"

		echo "Starting evaluation of ${in_file}..."
		let k=100  # initialize
		for i in {1..5}
		do
			let minLen=5  # initialize
			for j in {1..5}
			do
				# echo "java -Xmx8192m -jar $jar -sc $clusterer -t $in_file -T $true_file -c last -M $metrics -k $k -minLen $minLen -timelimit 30 >> $outfile"
				java -Xmx8192m -jar $jar -sc $clusterer -t $in_file -T $true_file -c last -M $metrics -k $k -minLen $minLen -timelimit 30 >> $outfile
				minLen="$(echo "${minLen} + ${minLenOffset}" | bc)"
			done
			k="$(echo "${k} + ${kOffset}" | bc)"
		done
		echo "Finished evaluation of ${in_file}..." 
	done
done
