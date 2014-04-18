package npylm;

import java.util.*;


public class Restaurant_Franchise {

    public Restaurant zerogram_restaurant;
    public Map<String, Restaurant> unigram_restaurants;
    public Map<Bigram, Restaurant> bigram_restaurants;
    public Base_distribution base_dist;
    public double strength = 0.2;
    public double discount = 0.4;

    public Restaurant_Franchise(Base_distribution base_dist,
				Map<Bigram, Map<String, Integer>> counter) {
	this.base_dist = base_dist;
	this.bigram_restaurants  = new HashMap<Bigram, Restaurant>(1000000);
	this.unigram_restaurants = new HashMap<String, Restaurant>(100000);

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
    public void gibbs_sampling(List<List<String>> sentences, int gibbs_sup) {
	int rest_gibbs = gibbs_sup;
	while (rest_gibbs > 0) {
	    rest_gibbs = _gibbs_sampling(sentences, rest_gibbs);
	}
    }

    private int _gibbs_sampling(List<List<String>> sentences, int gibbs_sup) {
	int gibbs_n = 0;
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
	    //System.out.println(this.log_like());
	    gibbs_n += upper;
	    if (gibbs_n > gibbs_sup) {
		return gibbs_sup - gibbs_n;
	    }
	}
	System.out.println(this.log_like());
	return gibbs_sup - gibbs_n;
    }

    private void add(Bigram context, String current_word) {
	boolean is_recurve;
	Restaurant restaurant;
	double new_prob;

	restaurant = this.bigram_restaurants.get(context);
	new_prob = this.prob(context.second, current_word);
	is_recurve = restaurant.add(current_word, this.discount, new_prob);
	if (!is_recurve) {
	    return;
	}

	restaurant = this.unigram_restaurants.get(context.second);
	new_prob = this.prob(current_word);
	is_recurve = restaurant.add(current_word, this.discount, new_prob);
	if (!is_recurve) {
	    return;
	}

	restaurant = this.zerogram_restaurant;
	new_prob = this.base_dist.get(current_word);
	is_recurve = restaurant.add(current_word, this.discount, new_prob);
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
	is_recurve = restaurant.remove(current_word);
    }

    public double prob(Bigram context, String current_word) {
	Restaurant restaurant = this.bigram_restaurants.get(context);
	double first, second;
	int dish_table_n;
	if (!restaurant.tables.containsKey(current_word)) {
	    dish_table_n = 0;
	} else {
	    dish_table_n = restaurant.tables.get(current_word).size();
	}
	first = (restaurant.customer_n_for_dish(current_word) - this.discount * dish_table_n) / (this.strength + restaurant.total_customer_n);
	second = (this.strength + this.discount * restaurant.total_table_n) / (this.strength + restaurant.total_customer_n);
	second *= this.prob(context.second, current_word);

	return first + second;
    }

    public double prob(String context, String current_word) {
	Restaurant restaurant = this.unigram_restaurants.get(context);
	double first, second;
	int dish_table_n;
	if (!restaurant.tables.containsKey(current_word)) {
	    dish_table_n = 0;
	} else {
	    dish_table_n = restaurant.tables.get(current_word).size();
	}
	first = (restaurant.customer_n_for_dish(current_word) - this.discount * dish_table_n) / (this.strength + restaurant.total_customer_n);
	second = (this.strength + this.discount * restaurant.total_table_n) / (this.strength + restaurant.total_customer_n);
	second *= this.prob(current_word);

	return first + second;
    }

    public double prob(String current_word) {
	Restaurant restaurant = this.zerogram_restaurant;
	double first, second;
	int dish_table_n;
	if (!restaurant.tables.containsKey(current_word)) {
	    dish_table_n = 0;
	} else {
	    dish_table_n = restaurant.tables.get(current_word).size();
	}
	first = (restaurant.customer_n_for_dish(current_word) - this.discount * dish_table_n) / (this.strength + restaurant.total_customer_n);
	second = (this.strength + this.discount * restaurant.total_table_n) / (this.strength + restaurant.total_customer_n);
	second *= this.base_dist.get(current_word);

	return first + second;
    }

    public double log_like() {
	double _log_like = 0.0;

	double d;
	for (Map.Entry<Bigram, Restaurant> b_r : this.bigram_restaurants.entrySet()) {
	    d = b_r.getValue().log_like(this.discount, this.strength);
	    //System.out.println("log_like " + d + ", " + "table " + b_r.getValue().total_table_n + ", " + "customer " + b_r.getValue().total_customer_n);
	    _log_like += d;
	    //_log_like += b_r.getValue().log_like(this.discount, this.strength);
	}
	for (Map.Entry<String, Restaurant> s_r : this.unigram_restaurants.entrySet()) {
	    d = s_r.getValue().log_like(this.discount, this.strength);
	    //System.out.println("log_like " + d + ", " + "table " + s_r.getValue().total_table_n + ", " + "customer " + s_r.getValue().total_customer_n);
	    _log_like += d;
	    //p_log_like += s_r.getValue().log_like(this.discount, this.strength);
	}
	_log_like += this.zerogram_restaurant.log_like(this.discount, this.strength);

	int c_n;
	for (String word : this.zerogram_restaurant.tables.keySet()) {
	    c_n = this.zerogram_restaurant.customer_n_for_dish(word);
	    _log_like += c_n * Math.log(this.base_dist.get(word));
	}

	return _log_like;
    }
}
