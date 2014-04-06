package clustoring;

import java.util.*;
import clustoring.Model;
import clustoring.Posterior;



public class Training {

    public Model train(List<List<Double>> weights, int c_N1, int c_N2) {
	Model model = new Model(weights,
				weights.size(), weights.get(0).size(), c_N1, c_N2);
	Posterior poster = new Posterior(model);
	for (int i = 0; i < 20; ++i) {
	    poster.update_model(model);
	    System.out.println(i);
	}
	return model;
    }
}
