package npylm;

import java.util.*;
import npylm.Base_distribution;


public class Model {
    private static String BOS2 = "##<BOS2>##";
    private static String BOS1 = "##<BOS1>##";
    private static String EOS1 = "##<EOS1>##";
    private static String EOS2 = "##<EOS2>##";


    public Map<Bigram, Map<String, double[]>> trigram_params;
    public Map<Bigram, double[]> unknown_trigram_params;
    //public double unknown_rt_trigram_param;

    public Map<String, Map<String, double[]>> bigram_params;
    public Map<String, double[]> unknown_bigram_params;
    //public double unknown_rt_bigram_param;

    public Map<String, double[]> unigram_params;
    public double[] unknown_unigram_param;

    public Base_distribution base_distribution;


    public Model(Restaurant_Franchise rf) {
	this.trigram_params = new HashMap<Bigram, Map<String, double[]>>();
	this.bigram_params  = new HashMap<String, Map<String, double[]>>();
	this.unknown_trigram_params = new HashMap<Bigram, double[]>();
	this.unknown_bigram_params  = new HashMap<String, double[]>();
	this.unknown_unigram_param  = new double[2];
	this.base_distribution = new Base_distribution();

	for (Map.Entry<Bigram, Restaurant> b_map : rf.bigram_restaurants.entrySet()) {
	    Restaurant restaurant = b_map.getValue();
	    Map<String, List<Integer>> customer_list = restaurant.get_customer_list();
	    int total_customer_n = restaurant.total_customer_n;
	    int table_n          = restaurant.tables.size();

	    Map<String, double[]> params = new HashMap<String, double[]>();
	    for (Map.Entry<String, List<Integer>> s_map : customer_list.entrySet()) {
		String dish = s_map.getKey();
		double[] dish_params = new double[2];
		int table_n_dish = s_map.getValue().size();
		int total_cusomer_n_dish = 0;
		for (int i = 0; i < table_n_dish; ++i) {
		    total_cusomer_n_dish += s_map.getValue().get(i);
		}
		dish_params[0] =
		    Math.log(total_cusomer_n_dish -
			     Restaurant.param_d[2] * table_n_dish) -
		    Math.log(Restaurant.param_theta[2] +total_customer_n);
		dish_params[1] =
		    Math.log(Restaurant.param_theta[2] +
			     Restaurant.param_d[2] * table_n) -
		    Math.log(Restaurant.param_theta[2] + total_customer_n);
		params.put(dish, dish_params);
	    }
	    this.trigram_params.put(b_map.getKey(), params);
	    double[] unknown_param = new double[2];

	    unknown_param[0] = Math.log(0.0);
	    unknown_param[1] = 
		Math.log(Restaurant.param_theta[2] +
			 Restaurant.param_d[2] * total_customer_n) -
		Math.log(Restaurant.param_theta[2] + total_customer_n);
	    this.unknown_trigram_params.put(b_map.getKey(), unknown_param);
	}

	for (Map.Entry<String, Restaurant> s_map : rf.unigram_restaurants.entrySet()) {
	    Restaurant restaurant = s_map.getValue();
	    Map<String, List<Integer>> customer_list = restaurant.get_customer_list();
	    int total_customer_n = restaurant.total_customer_n;
	    int table_n          = restaurant.tables.size();

	    Map<String, double[]> params = new HashMap<String, double[]>();
	    for (Map.Entry<String, List<Integer>> s_map2 : customer_list.entrySet()) {
		String dish = s_map2.getKey();
		double[] dish_params = new double[2];
		int table_n_dish = s_map2.getValue().size();
		int total_customer_n_dish = 0;
		for (int i = 0; i < table_n_dish; ++i) {
		    total_customer_n_dish += s_map2.getValue().get(i);
		}
		dish_params[0] =
		    Math.log(total_customer_n_dish -
			     Restaurant.param_d[1] * table_n_dish) -
		    Math.log(Restaurant.param_theta[1] + total_customer_n);
		dish_params[1] =
		    Math.log(Restaurant.param_theta[1] +
			     Restaurant.param_d[1] * table_n) -
		    Math.log(Restaurant.param_theta[1] + total_customer_n);
		params.put(dish, dish_params);
	    }
	    this.bigram_params.put(s_map.getKey(), params);
	    double[] unknown_param = new double[2];

	    unknown_param[0] = Math.log(0.0);
	    unknown_param[1] = 
		Math.log(Restaurant.param_theta[1] +
			 Restaurant.param_d[1] * total_customer_n) -
		Math.log(Restaurant.param_theta[1] + total_customer_n);
	    this.unknown_bigram_params.put(s_map.getKey(), unknown_param);
	}

	Restaurant restaurant = rf.zerogram_restaurant;
	Map<String, List<Integer>> customer_list = restaurant.get_customer_list();
	int total_customer_n = restaurant.total_customer_n;
	int table_n          = restaurant.tables.size();

	Map<String, double[]> params = new HashMap<String, double[]>();
	for (Map.Entry<String, List<Integer>> s_map : customer_list.entrySet()) {
	    String dish = s_map.getKey();
	    double[] dish_params = new double[2];
	    int table_n_dish = s_map.getValue().size();
	    int total_customer_n_dish = 0;
	    for (int i = 0; i < table_n_dish; ++i) {
		total_customer_n_dish += s_map.getValue().get(i);
	    }
	    dish_params[0] =
		Math.log(total_customer_n_dish -
			 Restaurant.param_d[0] * table_n_dish) -
		Math.log(Restaurant.param_theta[0] +total_customer_n);
	    dish_params[1] =
		Math.log(Restaurant.param_theta[0] +
			 Restaurant.param_d[0] * table_n) -
		Math.log(Restaurant.param_theta[0] + total_customer_n);
	    params.put(dish, dish_params);
	}
	this.unigram_params = params;
	this.unknown_unigram_param = new double[2];
	this.unknown_unigram_param[0] = Math.log(0.0);
	this.unknown_unigram_param[1] = 
	    Math.log(Restaurant.param_theta[0] +
		     Restaurant.param_d[0] * total_customer_n) -
	    Math.log(Restaurant.param_theta[0] + total_customer_n);
	
    }
    public double calc_sentence_log_prob(List<String> sentence) {
	List<String> sentence_cp = new ArrayList<String>(sentence.size() + 4);
	sentence_cp.add(BOS2);
	sentence_cp.add(BOS1);

	int sent_len = sentence.size();
	for (int i = 0; i < sent_len; ++i) {
	    sentence_cp.add(sentence.get(i));
	}
	sentence_cp.add(EOS1);
	sentence_cp.add(EOS2);

	double sent_log_prob = 0.0;
	for (int i = 0; i < sent_len + 2; ++i) {
	    sent_log_prob += calc_word_log_prob(sentence_cp.get(i),
						sentence_cp.get(i + 1),
						sentence_cp.get(i + 2)
						);
	}

	return sent_log_prob;
    }

    public double calc_word_log_prob(String first, String second, String third) {
	double[] log_probs = new double[4];
	double former, latter;

	Bigram bigram = new Bigram(first, second);
	if (this.trigram_params.containsKey(bigram)) {
	    Map<String, double[]> trigram_param = this.trigram_params.get(bigram);
	    if (trigram_param.containsKey(third)) {
		log_probs[0] = trigram_param.get(third)[0];
		log_probs[1] = trigram_param.get(third)[1];
	    } else {
		log_probs[0] = this.unknown_trigram_params.get(bigram)[0];
		log_probs[1] = this.unknown_trigram_params.get(bigram)[1];
	    }
	} else {
	    log_probs[0] = Math.log(0.0);
	    log_probs[1] = Math.log(1.0);
	}
	log_probs[2] = log_probs[1];
	log_probs[3] = log_probs[2];

	if (this.bigram_params.containsKey(second)) {
	    Map<String, double[]> bigram_param = this.bigram_params.get(second);
	    if (bigram_param.containsKey(third)) {
		log_probs[1] += bigram_param.get(third)[0];
		log_probs[2] += bigram_param.get(third)[1];
		log_probs[3] += bigram_param.get(third)[1];
	    } else {
		log_probs[1] += this.unknown_bigram_params.get(second)[0];
		log_probs[2] += this.unknown_bigram_params.get(second)[1];
		log_probs[3] += this.unknown_bigram_params.get(second)[1];
	    }
	} else {
	    log_probs[1] += Math.log(0.0);
	    log_probs[2] += Math.log(1.0);
	    log_probs[3] += Math.log(1.0);
	}

	if (this.unigram_params.containsKey(third)) {
	    log_probs[2] += this.unigram_params.get(third)[0];
	    log_probs[3] += this.unigram_params.get(third)[1];
	} else {
	    log_probs[2] += this.unknown_unigram_param[0];
	    log_probs[3] += this.unknown_unigram_param[1];
	}

	log_probs[3] += this.base_distribution.get(third);

	return log_sum_exp(log_probs);
    }

    private double log_sum_exp(double[] log_probs) {
	double max_log_prob = log_probs[0];

	for (int i = 0; i < 4; ++i) {
	    if (log_probs[i] > max_log_prob) {
		max_log_prob = log_probs[i];
	    }
	}

	double sum_exp_prob = 0.0;
	for (int i = 0; i < 4; ++i) {
	    sum_exp_prob += Math.exp(log_probs[i] - sum_exp_prob);
	}

	return max_log_prob + Math.log(sum_exp_prob);
    }

    public void print_model() {
	for (Map.Entry<Bigram, Map<String, double[]>> b_map : this.trigram_params.entrySet()) {
	    Bigram bigram = b_map.getKey();
	    System.out.println(bigram.first + " " + bigram.second);
	    for (Map.Entry<String, double[]> s_map : b_map.getValue().entrySet()) {
		System.out.println("\t" + s_map.getKey() + " " + s_map.getValue()[0] + " " + s_map.getValue()[1]);
	    }
	}
	System.out.println("=========");
	for (Map.Entry<String, Map<String, double[]>> s_map : this.bigram_params.entrySet()) {
	    String unigram = s_map.getKey();
	    System.out.println(unigram);
	    for (Map.Entry<String, double[]> s_map2 : s_map.getValue().entrySet()) {
		System.out.println("\t" + s_map2.getKey() + " " + s_map2.getValue()[0] + " " + s_map2.getValue()[1]);
	    }
	}
	System.out.println("==========");
	for (Map.Entry<String, double[]> s_map : this.unigram_params.entrySet()) {
	    System.out.println(s_map.getKey() + " " + s_map.getValue()[0] + " " + s_map.getValue()[1]);
	}
    }
}
