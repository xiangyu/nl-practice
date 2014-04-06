package clustoring;

import java.util.*;


public class Training {

    public static Model train(List<List<Integer> weights, int c_N1, int c_N2) {
	Model model = new Model(weights,
				weights.size(), weights.get(0).size(), c_N1, c_N2);
	Posterior poster = new Posterior(model);

	for (int i = 0; i < 100; ++i) {
	    poster.update_model(model);
	}

	return model;
    }
}
