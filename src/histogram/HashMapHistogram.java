package histogram;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//meaning of: <T extends Comparable<T>> is that HashMapHistogram class can be used for every Comparable variable type
public class HashMapHistogram<T extends Comparable<T>> implements IHistogram<T>{


	Map<T, Integer> mapHistogram = new HashMap<T, Integer>();
	
	//constructor with list of objects
	public HashMapHistogram(Collection<T> lst1){
		for(T item : lst1) {
			if(!this.mapHistogram.containsKey(item))
				this.mapHistogram.put(item, 1);
			else {
				Integer currentItemApperance = this.mapHistogram.get(item);
				this.mapHistogram.replace(item, currentItemApperance + 1);
			}
			
		}
	}
	//constructor empty 
	public HashMapHistogram(){
	}
	
	@Override
	public void addItem(T item) {
		if(!mapHistogram.containsKey(item)) // first item appearance
			mapHistogram.put(item, 1);
		else { // update item appearance counter +1
			Integer currentItemApperance = mapHistogram.get(item);
			mapHistogram.replace(item, currentItemApperance + 1);
		}
	}
	
	@Override
	// update item appearance counter -1 and delete if needed
	public boolean removeItem(T item)  {
		if(!mapHistogram.containsKey(item)) // item is not in the histogram map
			return false;
		if(mapHistogram.get(item) == 1)
			mapHistogram.remove(item);
		else {
			Integer currentItemApperance = mapHistogram.get(item);
			mapHistogram.replace(item, currentItemApperance - 1);
		}
		return true;
	}
	
	@Override
	//add list of objects to the histogram map
	public void addAll(Collection<T> items) {
		for(T item : items) {
			if(!mapHistogram.containsKey(item))
				mapHistogram.put(item, 1);
			else {
				Integer currentItemApperance = mapHistogram.get(item);
				mapHistogram.replace(item, currentItemApperance + 1);
			}
			
		}
	}

	@Override
	public int getCountForItem(T item) {
		if(!mapHistogram.containsKey(item))
			return 0;
		return mapHistogram.get(item);
	}

	@Override
	public void clear() {
		mapHistogram.clear();
	}

	@Override
	public Set<T> getItemsSet() {
		return mapHistogram.keySet();
	}
	
	@Override
	// get total appearance in the histogram
	public int getCountsSum() {
		int sum = 0;
		for(Integer itemAppearance : mapHistogram.values())
			sum += itemAppearance;
		return sum;
	}

	@Override
	public Iterator<Map.Entry<T, Integer>> iterator() {
		// generate iterator using the HashMapHistogramIterator class
    	Iterator<Map.Entry<T, Integer>> it = new HashMapHistogramIterator<T>(this.mapHistogram);
        return it;
	}
}
