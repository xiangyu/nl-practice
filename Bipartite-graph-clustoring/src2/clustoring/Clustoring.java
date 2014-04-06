package clustoring;

import java.util.*;
import clustoring.Training;
import clustoring.Model;


public class Clustoring {
    public static void main(String[] args) {
	int node_N1 = 100;
	int node_N2 = 500;
	List<List<Double>> weights = new ArrayList<List<Double>>(node_N1);
	List<Double> temp;
	for (int i = 0; i < node_N1; ++i) {
	    temp = new ArrayList<Double>(node_N2);
	    for (int j = 0; j < node_N2; ++j) {
		temp.add(0.1 * (j % 3) + 5.0 * (i % 2));
	    }
	    weights.add(temp);
	}
	Training t = new Training();
	Model model = t.train(weights, 5, 10);
    }
}
