import java.util.*;


public class Classifier {
    private static Map<String, Map<String, Double>> model;

    Classifier(Map<String, Map<String, Double>> model) {
	this.model = model;
    }

    public String d_classify(List<String> dcmt) {
	Map<String, Double> result = p_classify(dcmt);
	double max_log_prob = Double.NEGATIVE_INFINITY;
	String result_class = "";

	for (Map.Entry<String, Double> e : result.entrySet()) {
	    if (e.getValue() > max_log_prob) {
		max_log_prob = e.getValue();
		result_class = e.getKey();
	    }
	}
	return result_class;
    }

    public Map<String, Double> p_classify(List<String> dcmt) {
	Map<String, Double> result = new HashMap<String, Double>();

	for (Map.Entry<String, Map<String, Double>> e : model.entrySet()) {
	    double log_prob = 0.0;
	    for (String s : dcmt) {
		log_prob += e.getValue().get(s);
	    }
	    result.put(e.getKey(), log_prob);
	}

	return result;
    }
}
