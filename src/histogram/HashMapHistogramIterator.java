package histogram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// class HashMapHistogramIterator is an iterator class
public class HashMapHistogramIterator<T extends Comparable<T>> 
							implements Iterator<Map.Entry<T, Integer>>{
	// Create list from elements of HashMap
    List<Map.Entry<T, Integer>> lst= new ArrayList<Map.Entry<T, Integer> >();
    int currentIndex = 0;

	// HashMapHistogramIterator contractor
	public HashMapHistogramIterator(Map<T, Integer> mapHistogram) {
		for(Map.Entry<T, Integer> element : mapHistogram.entrySet())
			lst.add(element);
		Collections.sort(lst,(i, j) -> i.getKey().compareTo(j.getKey()));
	}

	@Override
	public boolean hasNext() {
		return currentIndex<lst.size();
	}

	@Override
	public Map.Entry<T, Integer> next() {
		currentIndex++;
		return lst.get(currentIndex-1);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
		
	}
	
}

