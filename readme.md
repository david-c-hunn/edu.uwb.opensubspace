

To compile and create a runnable jar:
```
$> cd edu.uwb.opensubspace
$> ant compile jar
```
To run the jar:
```
$> java -jar build/jar/evaluator.jar -sc Clon -k 150 -minLen 90 Databases/synth_dbsizescale/S1500.arff -T Databases/synth_dbsizescale/S1500.true -c last -M F1Measure -timelimit 30
```
The following is an example of the output to StdOut:
```
$> 2016-08-14 08:16	Carti-Clon	k=150; minlen=90	db_C10D20S150	1387	F1Measure=	0.40814841754289816
```


This is the code for my capstone project at UW Bothell.

This is a slightly modifed version of OpenSubspace. I am using it in my research
on subspace clustering.

Here's a reference to OpenSubspace:

Müller E., Günnemann S., Assent I., Seidl T.:
Evaluating Clustering in Subspace Projections of High Dimensional Data
http://dme.rwth-aachen.de/OpenSubspace/
In Proc. 35th International Conference on Very Large Data Bases (VLDB 2009), Lyon, France. (2009) 

