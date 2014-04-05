import java.io.*;
import java.util.*;



public class Trainer {
    final static double smooth_d = 0.2;

    public static Map<String, Map<String, Double>> train(String dir_path) throws IOException {
	File dir = new File(dir_path);
	String[] class_names = dir.list();

	Map<String, Map<String, Integer>> docs = read_files(dir_path, class_names);
	Set<String> vocaburary = determin_uniq_word(docs);
	Map<String, Map<String, Double>> model = build_model(docs, vocaburary);

	return model;
    }

    private static Map<String, Map<String, Integer>> read_files(String dir_path, String[] class_names) throws IOException {
	Map<String, Map<String, Integer>> docs = new HashMap<String, Map<String, Integer>>();
	String line;
	for (String class_name : class_names) {
	    FileReader     in = new FileReader(dir_path + "/" + class_name);
	    BufferedReader br = new BufferedReader(in);
	    Map<String, Integer> words_count = new HashMap<String, Integer>();
	    String[] words;
	    while ((line = br.readLine()) != null) {
		words = line.split(" ", -1);
		for (int i = 0; i < words.length; ++i) {
		    if (words_count.containsKey(words[i])) {
			words_count.put(words[i], words_count.get(words[i]) + 1);
		    } else {
			words_count.put(words[i], 0);

		    }
		}
	    }
	    br.close();
	    in.close();
	    docs.put(class_name, words_count);
	}

	return docs;
    }

    private static Set<String> determin_uniq_word(Map<String, Map<String, Integer>> docs) {
	Set<String> vocaburary = new HashSet<String>();
	for (Map.Entry<String, Map<String, Integer>> e : docs.entrySet()) {
	    for (Map.Entry<String, Integer> e2 : e.getValue().entrySet()) {
		vocaburary.add(e2.getKey());
	    }
	}
	return vocaburary;
    }

    private static Map<String, Map<String, Double>> build_model
	(Map<String, Map<String, Integer>> docs, Set<String> vocaburary) {
	Map<String, Map<String, Double>> log_probs = new HashMap<String, Map<String, Double>>();

	double smooth_denom = smooth_d * vocaburary.size();
	for (Map.Entry<String, Map<String, Integer>> e : docs.entrySet()) {
	    String class_name = e.getKey();
	    int total_count = 0;
	    Map<String, Double> log_prob = new HashMap<String, Double>();
	    for (String word : vocaburary) {
		if (e.getValue().containsKey(word)) {
		    int count_num = e.getValue().get(word);
		    total_count += count_num;
		    log_prob.put(word, Math.log(count_num + smooth_d));
		} else {
		    log_prob.put(word, Math.log(smooth_d));
		}
	    }
	    double denom = Math.log(smooth_denom + total_count);
	    for (String word : vocaburary) {
		log_prob.put(word, log_prob.get(word) - denom);
	    }
	    log_probs.put(class_name, log_prob);
	}
	return log_probs;
    }
}
