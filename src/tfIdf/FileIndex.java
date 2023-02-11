package tfIdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import histogram.HashMapHistogram;
import histogram.IHistogram;


public class FileIndex {
	
	private boolean isInitialized = false;
	private boolean sortByTfidf = false;
	private boolean cosineSimilaritybool = false;
	private Map<String,IHistogram<String>> mapDirectory = new HashMap<String,IHistogram<String>>();
	private List<String> fileNames = new ArrayList<String>();
	private List<Integer> numberOfWords = new ArrayList<>(); //number of words with repetitions!!
    private List<List<Map.Entry<String, Double>>> tfidfLst= new ArrayList<List<Map.Entry<String, Double>>>();
    private List<Map<String, Double>> cosineSimilarityList= new ArrayList<Map<String, Double>>();
	

	/*
	 * @pre: the directory is no empty, and contains only readable text files
	 * @pre: isInitialized() == false;
	 */
  	public void indexDirectory(String folderPath) {
		File folder = new File(folderPath);
		File[] listFiles = folder.listFiles();
		//iterates over all the files in the folder.
		for (File file : listFiles) {
			// for every file in the folder
			if (file.isFile()) {
				try {
					List<String> readAllTokensList = FileUtils.readAllTokens(file); //return a list of strings with all the words in the file (in lower case)
					IHistogram<String> currentHist = new HashMapHistogram<>(readAllTokensList); // create IHistogram with all the words in file
					String fileName = file.getName(); // get current file name
					fileNames.add(fileName); // fileNames is a list of string storing filename of every file
					numberOfWords.add(readAllTokensList.size()); // numberOfWords list of int storing the number of words in evert doc
					mapDirectory.put(fileName, currentHist);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		isInitialized = true;
	}
  	
  	// This function creates a list of lists,
  	//each containing Entry objects with String keys and double values.
  	//The entries in each list are ordered by their tf-idf values from highest to lowest
  	private void sortIndexDirectoryByTfidf() throws FileIndexException {
  		for(String fileName : fileNames) {
  			//Declare and initialize local variables
  			IHistogram<String> currentFileIHistogram = mapDirectory.get(fileName);
  			Map<String,Double> currentFileTfidfMap = new HashMap<>();
  			List<Map.Entry<String, Double>> currentWordTFIDFLst = new ArrayList<>();
  			double currentTFIDF;
  			// add every String key from IHistogram HashMap to new HashMap of Map.Entry<String, Double>
  			// storing word keys with thier tf-idf values
  			for(Map.Entry<String, Integer> wordHistogram : currentFileIHistogram) {
  				currentTFIDF = getTFIDF(wordHistogram.getKey(),fileName); // Calculates tf-idf 
  				currentFileTfidfMap.put(wordHistogram.getKey(),currentTFIDF); //update tf-idf value of every String key
			}
  			// Create a list of Map.Entry<String, Double> objects from currentFileTfidfMap
  			for(Map.Entry<String, Double> wordTFIDF : currentFileTfidfMap.entrySet()) {
  				currentWordTFIDFLst.add(wordTFIDF);
			}
			Collections.sort(currentWordTFIDFLst, new Comparator <Map.Entry<String, Double>>(){
				// comparator function. Use for Collections list sort operation

				@Override
				public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
					if(o1.getValue().equals(o2.getValue()))
						return o1.getKey().compareTo(o2.getKey()); // tf-idf (value) is equal-> string lexicographic order
					return o2.getValue().compareTo(o1.getValue()); // Default -> higher tf-idf value comes first
				}
			});
			tfidfLst.add(currentWordTFIDFLst);  			
  		}
  		
  	}
	
	
  	
	/* @pre: isInitialized() */
  	// Return the number of times that word appears in 'fileName'.
	public int getCountInFile(String word, String fileName) throws FileIndexException{
		String wordLowerCase = word.toLowerCase();
		IHistogram<String> currentIHistogram = mapDirectory.get(fileName);
		if(currentIHistogram == null)
			throw new FileIndexException(fileName);
		return currentIHistogram.getCountForItem(wordLowerCase);
	}
	
	/* @pre: isInitialized() */
	public int getNumOfUniqueWordsInFile(String fileName) throws FileIndexException{ 
		IHistogram<String> currentIHistogram = mapDirectory.get(fileName);
		if(currentIHistogram == null)
			throw new FileIndexException(fileName);
		return currentIHistogram.getItemsSet().size();
	}
	
	/* @pre: isInitialized() */
	public int getNumOfFilesInIndex(){
		return mapDirectory.size();
	}

	
	/* @pre: isInitialized() */
	public double getTF(String word, String fileName) throws FileIndexException{
		int fileIndex = fileNames.indexOf(fileName);
		return calcTF(getCountInFile(word, fileName),numberOfWords.get(fileIndex));
	}
	
	/* @pre: isInitialized() 
	 * @pre: exist fileName such that getCountInFile(word) > 0*/
	public double getIDF(String word){ //Q4
		return calcIDF(getNumOfFilesInIndex(),getNumOfFilesCont(word)); //replace this with the correct value
	}
	
	
	// return the number of files in mapDirectory that contains word
	private int getNumOfFilesCont(String word){
		int count = 0;
		for(String fileName : fileNames) {
			
			try {
				if(getCountInFile(word,fileName)>0)
					count++;
			} catch (FileIndexException e) {}
		}
		return count;
	}



	/*
	 * @pre: isInitialized()
	 * @pre: 0 < k <= getNumOfUniqueWordsInFile(fileName)
	 * @post: $ret.size() = k
	 * @post for i in (0,k-2):
	 * 		$ret[i].value >= $ret[i+1].value
	 */
	public List<Map.Entry<String, Double>> getTopKMostSignificantWords(String fileName, int k) 
													throws FileIndexException{
		if(!sortByTfidf) {
			sortIndexDirectoryByTfidf();
			sortByTfidf = true;
		}
			
		int fileIndex = fileNames.indexOf(fileName);
		if(fileIndex == -1)
			throw new FileIndexException(fileName);
		return tfidfLst.get(fileIndex).subList(0, k);
	}
	
	/* @pre: isInitialized() */
	// this function calculates cosine similarity of 2 files in the mapDirectory. using the formula:
	//Sig(Ai*Bi)/sqrt(Sig(Ai^2)*Sig(Bi^2)) when Ai is the tf-idf value of word i in the vocabulary.
	//The vocabulary is all the words in the language. Only words that appeared in one of the files is relevant to the calculation. 
	public double getCosineSimilarity(String fileName1, String fileName2) throws FileIndexException{
		// Declare and initialize local variables
		double sumOfASquared = 0.;
		double sumOfBSquared = 0.;
		double sumOfATimesB = 0.;
		double currentAValue;
		double currentBValue;
		 // Obtain an iterator for the elements in each file, sorted alphabetically
		Iterator<Map.Entry<String, Integer>> itFile1 = mapDirectory.get(fileName1).iterator();
		Iterator<Map.Entry<String, Integer>> itFile2 = mapDirectory.get(fileName2).iterator();
		//Store the current word for file1 and file2
		String currentWordFile1 = itFile1.next().getKey();
		String currentWordFile2 = itFile2.next().getKey();
		int currentCompareValue = currentWordFile1.compareTo(currentWordFile2);
		// Iterate over the words in both file1 and file2 in a way that produces a merged, sorted list.
		// This is similar to the process of merging two sorted lists into a single, sorted list.
		while(currentWordFile1 != null || currentWordFile2!= null) {
			if(currentWordFile1 != null && currentWordFile2!= null && currentCompareValue == 0) { //currentWordFile1 == currentWordFile2
				currentAValue = getTFIDF(currentWordFile1, fileName1);
				currentBValue = getTFIDF(currentWordFile2, fileName2);
				sumOfASquared += Math.pow(currentAValue, 2);
				sumOfBSquared += Math.pow(currentBValue, 2);
				sumOfATimesB += currentAValue*currentBValue;
				if(itFile1.hasNext())
					currentWordFile1 = itFile1.next().getKey();
				else
					currentWordFile1 = null;
				if(itFile2.hasNext())
					currentWordFile2 = itFile2.next().getKey();
				else
					currentWordFile2 = null;
				if(currentWordFile1 != null && currentWordFile2!= null)
					currentCompareValue = currentWordFile1.compareTo(currentWordFile2);
				
			}
			if(currentWordFile1 != null && currentWordFile2!= null && currentCompareValue < 0) { //currentWordFile1 < currentWordFile2
				currentAValue = getTFIDF(currentWordFile1, fileName1);
				sumOfASquared += Math.pow(currentAValue, 2);
				if(itFile1.hasNext()) {
					currentWordFile1 = itFile1.next().getKey();
					currentCompareValue = currentWordFile1.compareTo(currentWordFile2);
				}				
				else
					currentWordFile1 = null;
			}
			if(currentWordFile1 != null && currentWordFile2!= null && currentCompareValue > 0) { //currentWordFile1 > currentWordFile2
				currentBValue = getTFIDF(currentWordFile2, fileName2);
				sumOfBSquared += Math.pow(currentBValue, 2);
				if(itFile2.hasNext()) {
					currentWordFile2 = itFile2.next().getKey();
					currentCompareValue = currentWordFile1.compareTo(currentWordFile2);
				}
				else
					currentWordFile2 = null;
			}
			if(currentWordFile1 != null && currentWordFile2== null) { //File2 done iteration
				currentAValue = getTFIDF(currentWordFile1, fileName1);
				sumOfASquared += Math.pow(currentAValue, 2);
				if(itFile1.hasNext())
					currentWordFile1 = itFile1.next().getKey();		
				else
					currentWordFile1 = null;
			}
			if(currentWordFile1 == null && currentWordFile2!= null) { //File1 done iteration
				currentBValue = getTFIDF(currentWordFile2, fileName2);
				sumOfBSquared += Math.pow(currentBValue, 2);
				if(itFile2.hasNext())
					currentWordFile2 = itFile2.next().getKey();		
				else
					currentWordFile2 = null;
			}			
		}
		double cosSim = sumOfATimesB/Math.sqrt(sumOfASquared*sumOfBSquared); //calc cosSim value
		return cosSim;
	}
	
	/*
	 * @pre: isInitialized()
	 * @pre: 0 < k <= getNumOfFilesInIndex()-1
	 * @post: $ret.size() = k
	 * @post for i in (0,k-2):
	 * 		$ret[i].value >= $ret[i+1].value
	 */
	public List<Map.Entry<String, Double>> getTopKClosestDocuments(String fileName, int k) 
			throws FileIndexException{ //Q6
		List<Map.Entry<String, Double>> documentsSimilarityList = new ArrayList<Map.Entry<String, Double>>();
		int fileIndex = fileNames.indexOf(fileName);
		if(!cosineSimilaritybool) {
			for(int i = 0; i<fileNames.size(); i++) {
				Map<String, Double> currentCosineSimilarity = new HashMap<String, Double>();
				for(int j=0; j<fileNames.size(); j++) {
					if(i!=j) {
						currentCosineSimilarity.put(fileNames.get(j), getCosineSimilarity(fileNames.get(i),fileNames.get(j)));
				
					}
				}
				cosineSimilarityList.add(currentCosineSimilarity);
			}
			cosineSimilaritybool = true;
		}
		for(Map.Entry<String, Double> fileSimilarity : cosineSimilarityList.get(fileIndex).entrySet()) {
			documentsSimilarityList.add(fileSimilarity);
		}
		Collections.sort(documentsSimilarityList,(j, i) -> i.getValue().compareTo(j.getValue()));
		return documentsSimilarityList.subList(0, k);
	}
	
	public boolean isInitialized(){
		return this.isInitialized;
	}
	
	/* @pre: exist fileName such that getCountInFile(word) > 0*/
	public double getTFIDF(String word, String fileName) throws FileIndexException{
		return this.getTF(word, fileName)*this.getIDF(word);
	}
	
	private static double calcTF(int repetitionsForWord, int numOfWordsInDoc){
		return (double)repetitionsForWord/numOfWordsInDoc;
	}
	
	private static double calcIDF(int numOfDocs, int numOfDocsContainingWord){
		return Math.log((double)numOfDocs/numOfDocsContainingWord);
	}
	
}
