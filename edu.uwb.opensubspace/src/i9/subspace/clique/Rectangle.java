/**
 * rectangles used to cover the cluster
 * @author pishchulin
 */
package i9.subspace.clique;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Rectangle {
	

	private ArrayList<Integer> m_minValues;
	
	private ArrayList<Integer> m_maxValues;
	
	List<Set<Integer>> m_units;
	
	public Rectangle(ArrayList<Integer> minValues, ArrayList<Integer> maxValues){
		
		m_minValues = new ArrayList<Integer>();
		m_maxValues = new ArrayList<Integer>();
		m_units = new ArrayList<Set<Integer>>();
		m_minValues = minValues;
		m_maxValues = maxValues;
	}
	
	public Rectangle(){
		
		m_minValues = new ArrayList<Integer>();
		m_maxValues = new ArrayList<Integer>();
		m_units = new ArrayList<Set<Integer>>();
		
	}
	
	public ArrayList<Integer> getMaxValues(){
		return m_maxValues;
	}
	
	public ArrayList<Integer> getMinValues(){
		return m_minValues;
	}
	
	public void addValues(int minValue, int maxValue){
		m_maxValues.add(maxValue);
		m_minValues.add(minValue);
		quickSort(m_maxValues, 0, m_maxValues.size()-1);
		quickSort(m_minValues, 0, m_minValues.size()-1);
	}
	
	public void addUnit(List<Set<Integer>> units){
		m_units = units;
	}
	
	private void quickSort(ArrayList<Integer> array, int start, int end)
    {
            int i = start;                          // index of left-to-right scan
            int k = end;                            // index of right-to-left scan

            if (end - start >= 1)                   // check that there are at least two elements to sort
            {
                    double pivot = array.get(start);       // set the pivot as the first element in the partition

                    while (k > i)                   // while the scan indices from left and right have not met,
                    {
                            while (array.get(i) <= pivot && i <= end && k > i)  // from the left, look for the first
                                    i++;                                    // element greater than the pivot
                            while (array.get(k) > pivot && k >= start && k >= i) // from the right, look for the first
                                k--;                                        // element not greater than the pivot
                            if (k > i)                                       // if the left seekindex is still smaller than
                                    swap(array, i, k);                      // the right index, swap the corresponding elements
                    }
                    swap(array, start, k);          // after the indices have crossed, swap the last element in
                                                    // the left partition with the pivot 
                    quickSort(array, start, k - 1); // quicksort the left partition
                    quickSort(array, k + 1, end);   // quicksort the right partition
            }
            else    // if there is only one element in the partition, do not do any sorting
            {
                    return;                     // the array is sorted, so exit
            }
    }
	
	private void swap(ArrayList<Integer> array, int index1, int index2) 
    // pre: array is full and index1, index2 < array.length
    // post: the values at indices 1 and 2 have been swapped
    {
    	Integer temp = array.get(index1);           // store the first value in a temp
    	array.set(index1, array.get(index2));      	// copy the value of the second into the first
    	array.set(index2, temp); 					// copy the value of the temp into the second
    }
	

}
