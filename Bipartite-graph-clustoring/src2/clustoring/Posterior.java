package clustoring;

import java.util.*;
import clustoring.Model;


public class Posterior {

    // marginaling with k,l
    public List<List<Double>> semi_marginals1;
    // marginaling with m,n
    public List<List<Double>> semi_marginals2;

    public Posterior(Model model) {
	this.semi_marginals1 = new ArrayList<List<Double>>(model.node_N1);
	this.semi_marginals2 = new ArrayList<List<Double>>(model.c_N1);

	List<Double> semi_marginal;
	for (int m = 0; m < model.node_N1; ++m) {
	    semi_marginal = new ArrayList<Double>(model.node_N2);
	    for (int n = 0; n < model.node_N2; ++n) {
		semi_marginal.add(0.0);
	    }
	    this.semi_marginals1.add(semi_marginal);
	}

	for (int k = 0; k < model.c_N1; ++k) {
	    semi_marginal = new ArrayList<Double>(model.c_N2);
	    for (int l = 0; l < model.c_N2; ++l) {
		semi_marginal.add(0.0);
	    }
	    this.semi_marginals2.add(semi_marginal);
	}
	this.update_model(model);
    }

    public void update_model(Model model) {
	this.prepare_marginals(model);
	this.update_priors(model);
	this.update_means(model);
	this.update_variances(model);
    }

    private void prepare_marginals(Model model) {
	List<Double> prob_sums;
	double outer_sum, inner_sum;
	for (int m = 0; m < model.node_N1; ++m) {
	    for (int n = 0; n < model.node_N2; ++n) {
		outer_sum = 0.0;
		for (int k = 0; k < model.c_N1; ++k) {
		    inner_sum = 0.0;
		    for (int l = 0; l < model.c_N2; ++l) {
			inner_sum += model.calc_joint_prob(m, n, k, l);
		    }
		    outer_sum += inner_sum;
		}
		this.semi_marginals1.get(m).set(n, outer_sum);
	    }
	}

	double prob;
	for (int k = 0; k < model.c_N1; ++k) {
	    for (int l = 0; l < model.c_N2; ++l) {
		outer_sum = 0.0;
		for (int m = 0; m < model.node_N1; ++m) {
		    inner_sum = 0.0;
		    for (int n = 0; n < model.node_N2; ++n) {
			prob = model.calc_joint_prob(m, n, k, l) / this.semi_marginals1.get(m).get(n);
			inner_sum += prob;
		    }
		    outer_sum += inner_sum;
		}
		this.semi_marginals2.get(k).set(l, outer_sum);
	    }
	}
    }

    private void update_priors(Model model) {
	double nmlz = 1.0 / model.node_N1 * model.node_N2;
	double sum;
	for (int k = 0; k < model.c_N1; ++k) {
	    sum = 0.0;
	    for (int l = 0; l < model.c_N2; ++l) {
		sum += this.semi_marginals2.get(k).get(l);
	    }
	    model.priors1.set(k, sum * nmlz);
	}
	for (int l = 0; l < model.c_N2; ++l) {
	    sum = 0.0;
	    for (int k = 0; k < model.c_N1; ++k) {
		sum += this.semi_marginals2.get(k).get(l);
	    }
	    model.priors2.set(l, sum * nmlz);
	}
    }

    private void update_means(Model model) {
	double prob;
	double inner_sum, outer_sum;
	for (int k = 0; k < model.c_N1; ++k) {
	    for (int l = 0; l < model.c_N2; ++l) {
		outer_sum = 0.0;
		for (int m = 0; m < model.node_N1; ++m) {
		    inner_sum = 0.0;
		    for (int n = 0; n < model.node_N2; ++n) {
			prob = model.calc_joint_prob(m, n, k, l) / this.semi_marginals1.get(m).get(n);
			inner_sum += prob * model.weights.get(m).get(n);
		    }
		    outer_sum += inner_sum;
		}
		model.means.get(k).set(l, outer_sum / this.semi_marginals2.get(k).get(l));
	    }
	}
    }

    private void update_variances(Model model) {
	double prob;
	double diff;
	double inner_sum, outer_sum;

	for (int k = 0; k < model.c_N1; ++k) {
	    for (int l = 0; l < model.c_N2; ++l) {
		outer_sum = 0.0;
		for (int m = 0; m < model.node_N1; ++m) {
		    inner_sum = 0.0;
		    for (int n = 0; n < model.node_N2; ++n) {
			prob = model.calc_joint_prob(m, n, k, l) / this.semi_marginals1.get(m).get(n);
			diff = model.weights.get(m).get(n) - model.means.get(k).get(l);
			inner_sum += prob * diff * diff;
		    }
		    outer_sum += inner_sum;
		}
		model.variances.get(k).set(l, outer_sum / this.semi_marginals2.get(k).get(l));
	    }
	}
    }
			

    private double sum_List(List<Double> probs) {
	int upper = probs.size();
	double total = 0.0;
	for (int i = 0; i < upper; ++i) {
	    total += probs.get(i);
	}
	return total;
    }
}
