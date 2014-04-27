package npylm;

import java.util.*;
import npylm.Slice_Sampler;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.BetaDistribution;


public class Restaurant_Franchise {

    public Restaurant zerogram_restaurant;
    public Map<String, Restaurant> unigram_restaurants;
    public Map<Bigram, Restaurant> bigram_restaurants;
    public Base_distribution base_dist;

    public Slice_Sampler s_sampler; 

    public BetaDistribution  beta_dist;
    public GammaDistribution gamma_dist;

    public double bi_discount;
    public double bi_strength;

    public double uni_discount;
    public double uni_strength;

    public double zero_discount;
    public double zero_strength;


    public Restaurant_Franchise(Base_distribution base_dist,
				Map<Bigram, Map<String, Integer>> counter,
				double discount, double strength) {
	this.base_dist = base_dist;

	this.s_sampler = new Slice_Sampler();

	this.bigram_restaurants  = new HashMap<Bigram, Restaurant>(5000000);
	this.unigram_restaurants = new HashMap<String, Restaurant>(100000);

	this.beta_dist  = new BetaDistribution(1.0, 1.0);
	this.gamma_dist = new GammaDistribution(1.0, 1.0);

	this.bi_discount = this.uni_discount = this.zero_discount = discount;
	this.bi_strength = this.uni_strength = this.zero_strength = strength;

	this.init(counter);
    }

    public Restaurant_Franchise(Base_distribution base_dist,
				Map<Bigram, Map<String, Integer>> counter) {
	this.base_dist = base_dist;

	this.s_sampler = new Slice_Sampler();

	this.bigram_restaurants  = new HashMap<Bigram, Restaurant>(5000000);
	this.unigram_restaurants = new HashMap<String, Restaurant>(100000);

	this.beta_dist  = new BetaDistribution(1.0, 1.0);
	this.gamma_dist = new GammaDistribution(1.0, 1.0);

	this.bi_discount   = beta_dist.sample();
	this.uni_discount  = beta_dist.sample();
	this.zero_discount = beta_dist.sample();

	this.bi_strength   = gamma_dist.sample();
	this.uni_strength  = gamma_dist.sample();
	this.zero_strength = gamma_dist.sample();

	this.init(counter);
    }

    private void init(Map<Bigram, Map<String, Integer>> counter) {
	for (Map.Entry<Bigram, Map<String, Integer>> r_map : counter.entrySet()) {
	    Restaurant bigram_restaurant = new Restaurant();
	    String pre_word = r_map.getKey().second;
	    for (Map.Entry<String, Integer> s_map : r_map.getValue().entrySet()) {
		String current_word = s_map.getKey();
		bigram_restaurant.add_new_table(current_word, s_map.getValue());
		if (unigram_restaurants.containsKey(pre_word)) {
		    unigram_restaurants.get(pre_word).add_new_customer(current_word);
		} else {
		    Restaurant unigram_restaurant = new Restaurant();
		    unigram_restaurant.add_new_customer(current_word);
		    this.unigram_restaurants.put(pre_word, unigram_restaurant);
		}
	    }
	    this.bigram_restaurants.put(r_map.getKey(), bigram_restaurant);
	}

	this.zerogram_restaurant = new Restaurant();
	for (Map.Entry<String, Restaurant> s_map : this.unigram_restaurants.entrySet()) {
	    Restaurant unigram_restaurant = s_map.getValue();
	    for (Map.Entry<String, List<Integer>> dish_table : unigram_restaurant.tables.entrySet()) {
		String dish = dish_table.getKey();
		int customer_n = unigram_restaurant.customer_n_for_dish(dish);
		this.zerogram_restaurant.add_new_customer(dish, customer_n);
	    }
	}
    }

    // Each sentence in sentences already contains BOS and EOS.
    public void gibbs_sampling(List<List<String>> sentences, int iter_num) {
	for (int i = 0; i < iter_num; ++i) {
	    this._gibbs_sampling(sentences);
	    System.out.println(this.log_like());
	}
    }

    public void gibbs_sampling_with_hyper(List<List<String>> sentences, int iter_num) {
	for (int i = 0; i < iter_num; ++i) {
	    this._gibbs_sampling(sentences);
	    this.update_hyper();
	    System.out.println(this.log_like());
	}
    }

    private void _gibbs_sampling(List<List<String>> sentences) {
	Bigram bigram = new Bigram();
	String current_word;
	for (List<String> sentence : sentences) {
	    int upper = sentence.size() - 2;
	    for (int i = 0; i < upper; ++i) {
		bigram.first  = sentence.get(i);
		bigram.second = sentence.get(i + 1);
		current_word  = sentence.get(i + 2);
		this.remove(bigram, current_word);
		this.add(bigram, current_word);
	    }
	}
    }

    private void add(Bigram context, String current_word) {
	boolean is_recurve;
	Restaurant restaurant;
	double new_prob;

	restaurant = this.bigram_restaurants.get(context);
	new_prob = (this.bi_strength + this.bi_discount * restaurant.total_table_n) *
	    this.prob(context.second, current_word);
	is_recurve = restaurant.add(current_word, this.bi_discount, new_prob);
	if (!is_recurve) {
	    return;
	}

	restaurant = this.unigram_restaurants.get(context.second);
	new_prob = (this.uni_strength + this.uni_discount * restaurant.total_table_n) *
	    this.prob(current_word);
	is_recurve = restaurant.add(current_word, this.uni_discount, new_prob);
	if (!is_recurve) {
	    return;
	}

	restaurant = this.zerogram_restaurant;
	new_prob = (this.zero_strength + this.zero_discount * restaurant.total_table_n) *
	    this.base_dist.get(current_word);
	restaurant.add(current_word, this.zero_discount, new_prob);
    }

    private void remove(Bigram context, String current_word) {
	boolean is_recurve;
	Restaurant restaurant;

	restaurant = this.bigram_restaurants.get(context);
	is_recurve = restaurant.remove(current_word);
	if (!is_recurve) {
	    return;
	}

	restaurant = this.unigram_restaurants.get(context.second);
	is_recurve = restaurant.remove(current_word);
	if (!is_recurve) {
	    return;
	}

	restaurant = this.zerogram_restaurant;
	restaurant.remove(current_word);
    }

    private void update_hyper() {
	double next_discount, next_strength;

	//System.out.println("a");
	next_discount = this.s_sampler.next_discount(2, this.bi_discount, this);
	this.bi_discount = next_discount;
	//System.out.println("b");
	next_strength = this.s_sampler.next_strength(2, this.bi_strength, this);
	this.bi_strength = next_strength;

	//System.out.println("c");
	next_discount = this.s_sampler.next_discount(1, this.uni_discount, this);
	this.uni_discount = next_discount;
	//System.out.println("d");
	next_strength = this.s_sampler.next_strength(1, this.uni_strength, this);
	this.uni_strength = next_strength;

	//System.out.println("e");
	next_discount = this.s_sampler.next_discount(0, this.zero_discount, this);
	this.zero_discount = next_discount;
	//System.out.println("f");
	next_strength = this.s_sampler.next_strength(0, this.zero_strength, this);
	this.zero_strength = next_strength;
    }

    public double prob(Bigram context, String current_word) {
	Restaurant restaurant = this.bigram_restaurants.get(context);
	double first  = this.prob_first(restaurant, current_word, this.bi_discount, this.bi_strength);
	double second = this.prob_second(restaurant, this.bi_discount, this.bi_strength);

	return first + second * this.prob(context.second, current_word);
    }

    public double prob(String context, String current_word) {
	Restaurant restaurant = this.unigram_restaurants.get(context);
	double first  = this.prob_first(restaurant, current_word, this.uni_discount, this.uni_discount);
	double second = this.prob_second(restaurant, this.uni_discount, this.uni_strength);

	return first + second * this.prob(current_word);
    }

    public double prob(String current_word) {
	Restaurant restaurant = this.zerogram_restaurant;
	double first  = this.prob_first(restaurant, current_word, this.zero_discount, this.zero_strength);
	double second = this.prob_second(restaurant, this.zero_discount, this.zero_strength);

	return first + second * base_dist.get(current_word);
    }

    private double prob_first(Restaurant restaurant, String current_word,
			      double _discount, double _strength) {
	int dish_table_n;
	if (!restaurant.tables.containsKey(current_word)) {
	    dish_table_n = 0;
	} else {
	    dish_table_n = restaurant.tables.get(current_word).size();
	}
	double first = (restaurant.customer_n_for_dish(current_word) - _discount * dish_table_n) / (_strength + restaurant.total_customer_n);

	return first;
    }

    private double prob_second(Restaurant restaurant, double _discount, double _strength) {
	return (_strength + _discount * restaurant.total_table_n) / (_strength + restaurant.total_customer_n);
    }

    public double log_like() {
	double _log_like = 0.0;

	for (Map.Entry<Bigram, Restaurant> b_r : this.bigram_restaurants.entrySet()) {
	    _log_like += b_r.getValue().log_like(this.bi_discount, this.bi_strength);
	}
	for (Map.Entry<String, Restaurant> s_r : this.unigram_restaurants.entrySet()) {
	    _log_like += s_r.getValue().log_like(this.uni_discount, this.uni_strength);
	}
	_log_like += this.zerogram_restaurant.log_like(this.zero_discount, this.zero_strength);

	int c_n;
	for (String word : this.zerogram_restaurant.tables.keySet()) {
	    c_n = this.zerogram_restaurant.customer_n_for_dish(word);
	    _log_like += c_n * Math.log(this.base_dist.get(word));
	}

	return _log_like;
    }

    public double log_like(double bi_dis, double uni_dis, double zero_dis,
			   double bi_str, double uni_str, double zero_str) {
	double _log_like = 0.0;

	for (Map.Entry<Bigram, Restaurant> b_r : this.bigram_restaurants.entrySet()) {
	    _log_like += b_r.getValue().log_like(bi_dis, bi_str);
	}
	for (Map.Entry<String, Restaurant> s_r : this.unigram_restaurants.entrySet()) {
	    _log_like += s_r.getValue().log_like(uni_dis, uni_str);
	}
	_log_like += this.zerogram_restaurant.log_like(zero_dis, zero_str);

	int c_n;
	for (String word : this.zerogram_restaurant.tables.keySet()) {
	    c_n = this.zerogram_restaurant.customer_n_for_dish(word);
	    _log_like += c_n * Math.log(this.base_dist.get(word));
	}

	return _log_like;
    }
}
