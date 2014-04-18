package npylm;

import java.io.*;
import java.util.*;
import npylm.NPYLM_train;


public class NPYLM {
    /*
    public static void main(String[] args) {
	NPYLM_train t = new NPYLM_train();
	List<List<String>> sentences = new ArrayList<List<String>>();

	List<String> s1 = Arrays.asList("I", "am", "a", "student", ".");
	List<String> s2 = Arrays.asList("We", "are", "we");
	List<String> s3 = Arrays.asList("There", "are", "more", "than",
					"one", "way", "to", "do", "it", ".");
	List<String> s4 = Arrays.asList("There", "are", "only", "one", "way",
					"to", "do", "it", ".");
	sentences.add(s1);
	sentences.add(s2);
	sentences.add(s3);
	sentences.add(s4);
	t.train(sentences);
    }
    */
    public static void main(String[] args) {
	try {
	    BufferedReader reader = new BufferedReader(new FileReader(args[0]));
	    String line;
	    String[] words;
	    List<List<String>> sentences = new ArrayList<List<String>>();
	    while ((line = reader.readLine()) != null) {
		words = line.split(" ");
		sentences.add(Arrays.asList(words));
	    }
	    NPYLM_train t = new NPYLM_train();
	    t.train(sentences);
	} catch (FileNotFoundException e) {
	    System.out.println("not find");
	} catch (IOException e) {
	    System.out.println("not read");
	}
    }
}
