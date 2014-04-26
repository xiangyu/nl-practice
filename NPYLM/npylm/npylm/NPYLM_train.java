package npylm;

import java.util.*;
//import npylm.Model;
import npylm.Base_distribution;
import npylm.Restaurant_Franchise;


public class NPYLM_train {
    //private static double[] param_d = {0.2, 0.2, 0.2};
    //private static double[] param_theta = {0.2, 0.2, 0.2};

    private static String BOS2 = "##<BOS2>##";
    private static String BOS1 = "##<BOS1>##";
    private static String EOS1 = "##<EOS1>##";
    private static String EOS2 = "##<EOS2>##";

    public void train(List<List<String>> sentences) {
	List<List<String>> converted_sentences = this.convert_sentences(sentences);
	Map<Bigram, Map<String, Integer>> counter = this.count_trigram(converted_sentences);
	Base_distribution base_dist = new Base_distribution();
	Restaurant_Franchise rf = new Restaurant_Franchise(base_dist, counter);
	rf.gibbs_sampling(converted_sentences, 1000);// 1000 * 1000 * 500
	//Model model = new Model(rf);

	//return model;
    }

    public Map<Bigram, Map<String, Integer>>
	count_trigram(List<List<String>> c_sentences) {
	Map<Bigram, Map<String, Integer>> counter = new HashMap<Bigram, Map<String, Integer>>();
	Map<String, Integer> local_counter;
	String word;
	for (List<String> c_sentence : c_sentences) {
	    for (int i = 0, upper = c_sentence.size() - 2; i < upper; ++i) {
		Bigram bigram = new Bigram(c_sentence.get(i), c_sentence.get(i + 1));
		word = c_sentence.get(i + 2);
		if (!counter.containsKey(bigram)) {
		    local_counter = new HashMap<String, Integer>();
		    counter.put(bigram, local_counter);
		} else {
		    local_counter = counter.get(bigram);
		}
		if (local_counter.containsKey(word)) {
		    local_counter.put(word, local_counter.get(word) + 1);
		} else {
		    local_counter.put(word, 1);
		}
	    }
	}

	return counter;
    }

    public List<List<String>> convert_sentences(List<List<String>> sentences) {
	List<List<String>> converted_sentences = new ArrayList<List<String>>();
	for (List<String> sentence : sentences) {
	    List<String> converted_sentence = new ArrayList<String>();
	    converted_sentence.add(BOS2);
	    converted_sentence.add(BOS1);
	    for (String word : sentence) {
		converted_sentence.add(word);
	    }
	    converted_sentence.add(EOS1);
	    converted_sentence.add(EOS2);

	    converted_sentences.add(converted_sentence);
	}

	return converted_sentences;
    }
}


