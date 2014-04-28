package npylm;

import java.io.*;
import java.util.*;
import npylm.NPYLM_train;


public class NPYLM {

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
	    t.train(sentences, args[1]);
	} catch (FileNotFoundException e) {
	    System.out.println("not find");
	} catch (IOException e) {
	    System.out.println("not read");
	}
    }
}
