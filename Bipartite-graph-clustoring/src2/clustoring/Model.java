package clustoring;

import java.util.*;



public class Model {
    public List<List<Double>> weights;

    public List<List<Double>> variances;
    public List<List<Double>> means;
    public List<Double>       priors1;
    public List<Double>       priors2;

    public Model(List<List<Double>> weights, int node_N1, int node_N2, int c_N1, int c_N2) {
	this.weights   = weights;

	this.variances = new ArrayList<List<Double>>();
	this.means     = new ArrayList<List<Double>>();
	this.priors1   = new ArrayList<Double>();
	this.priors2   = new ArrayList<Double>();
    }

    public double calculate_posterior(int node_n1, int node_n2, int c_n1, int c_n2) {
	double prob;

	prob = 
    }
}
