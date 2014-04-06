package clustoring;

import java.util.*;



public class Model {
    public List<List<Double>> weights;
    public int node_N1;
    public int node_N2;
    public int c_N1;
    public int c_N2;

    public List<List<Double>> variances;
    public List<List<Double>> means;
    public List<Double>       priors1;
    public List<Double>       priors2;

    public Model(List<List<Double>> weights, int node_N1, int node_N2, int c_N1, int c_N2) {
	this.weights = weights;
	this.node_N1 = node_N1;
	this.node_N2 = node_N2;
	this.c_N1    = c_N1;
	this.c_N2    = c_N2;

	this.variances = new ArrayList<List<Double>>();
	this.means     = new ArrayList<List<Double>>();
	this.priors1   = new ArrayList<Double>();
	this.priors2   = new ArrayList<Double>();
    }

    public double calc_joint_prob(int node_n1, int node_n2, int c_n1, int c_n2) {
	double prob, memo;
	double var_pow = this.variances.get(c_n1).get(c_n2);

	prob = 1 / Math.sqrt(var_pow * 2.0);
	memo = this.weights.get(node_n1).get(node_n2) - this.means.get(c_n1).get(c_n2);
	prob *= Math.exp(-(memo * memo * 0.5 / (var_pow * var_pow)));
	prob *= this.priors1.get(c_n1) * this.priors2.get(c_n2);

	return prob;
    }
}
