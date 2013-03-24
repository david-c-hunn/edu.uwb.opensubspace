package weka.subspaceClusterer;

import i9.subspace.base.Cluster;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class SubspaceClusterTools {

	public static ArrayList<Cluster> getClusterList(File file, int numdims) {
		ArrayList<Cluster> result = new ArrayList<Cluster>();
		int file_dims = 0;
		
		try {
			FileReader fileReader = new FileReader(file);
			LineNumberReader reader = new LineNumberReader(fileReader);
			String line = reader.readLine(); // drop header line
			
			// Code added by dave to capture dim info in header of file
			// only the synthetic data files list the number of dims
			StringTokenizer toker = new StringTokenizer(line, "=;");
			toker.nextToken(); //Discard the "DIM"
			try {
			  file_dims = Integer.valueOf(toker.nextToken());
			} catch (Exception e) {
			  file_dims = 0;
			}
			// end new code
			
			line = reader.readLine();
			while (line != null) {
				StringTokenizer token = new StringTokenizer(line);
				int value;
				
				// more new code to go with the above
				if (numdims < 0) {
					numdims = file_dims;
				}
				// end code insertion
				
				boolean[] subspace = new boolean[numdims];
				List<Integer> objects = new LinkedList<Integer>();

				for (int i = 0; i < numdims; i++) {
					value = Integer.valueOf(token.nextToken(" ")).intValue();
					if (value == 1)
						subspace[i] = true;
				}
				int size = Integer.valueOf(token.nextToken(" ")).intValue();
				for (int i = 0; i < size; i++) {
					value = Integer.valueOf(token.nextToken(" ")).intValue();
					objects.add(value);
				}
				result.add(new Cluster(subspace, objects));
				line = reader.readLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}
	
}
