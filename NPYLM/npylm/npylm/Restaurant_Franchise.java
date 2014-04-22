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
	int turn = 0;
	int gibbs_n = 0;
	Bigram bigram = new Bigram();
	String current_word;
	for (List<String> sentence : sentences) {
	    turn++;
	    int upper = sentence.size() - 2;
	    for (int i = 0; i < upper; ++i) {
		bigram.first  = sentence.get(i);
		bigram.second = sentence.get(i + 1);
		current_word  = sentence.get(i + 2);
		this.remove(bigram, current_word);
		this.add(bigram, current_word);
	    }
	    if (turn > 200) {
		System.out.println(this.log_like());
		turn = 0;
	    }
	    gibbs_n += upper;
	    if (gibbs_n > gibbs_sup) {
		return gibbs_sup - gibbs_n;
	    }
	}
	return gibbs_sup - gibbs_n;
    }

    private void add(Bigram context, String current_word) {
	boolean is_recurve;
	Restaurant restaurant;
	double new_prob;

	restaurant = this.bigram_restaurants.get(context);
	new_prob = (this.strength + this.discount * restaurant.total_table_n) *
	    this.prob(context.second, current_word);
	is_recurve = restaurant.add(current_word, this.discount, new_prob);
	if (!is_recurve) {
	    return;
	}

	restaurant = this.unigram_restaurants.get(context.second);
	new_prob = (this.strength + this.discount * restaurant.total_table_n) *
	    this.prob(current_word);
	is_recurve = restaurant.add(current_word, this.discount, new_prob);
	if (!is_recurve) {
	    return;
	}

	restaurant = this.zerogram_restaurant;
	new_prob = (this.strength + this.discount * restaurant.total_table_n) *
	    this.base_dist.get(current_word);
	restaurant.add(current_word, this.discount, new_prob);
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

    public double prob(Bigram context, String current_word) {
	Restaurant restaurant = this.bigram_restaurants.get(context);
	double first  = this.prob_first(restaurant, current_word);
	double second = this.prob_second(restaurant);

	return first + second * this.prob(context.second, current_word);
    }

    public double prob(String context, String current_word) {
	Restaurant restaurant = this.unigram_restaurants.get(context);
	double first  = this.prob_first(restaurant, current_word);
	double second = this.prob_second(restaurant);

	return first + second * this.prob(current_word);
    }

    public double prob(String current_word) {
	Restaurant restaurant = this.zerogram_restaurant;
	double first  = this.prob_first(restaurant, current_word);
	double second = this.prob_second(restaurant);

	return first + second * base_dist.get(current_word);
    }

    private double prob_first(Restaurant restaurant, String current_word) {
	int dish_table_n;
	if (!restaurant.tables.containsKey(current_word)) {
	    dish_table_n = 0;
	} else {
	    dish_table_n = restaurant.tables.get(current_word).size();
	}
	double first = (restaurant.customer_n_for_dish(current_word) - this.discount * dish_table_n) / (this.strength + restaurant.total_customer_n);

	return first;
    }

    private double prob_second(Restaurant restaurant) {
	return (this.strength + this.discount * restaurant.total_table_n) / (this.strength + restaurant.total_customer_n);
    }

    public double log_like() {
	double _log_like = 0.0;

	for (Map.Entry<Bigram, Restaurant> b_r : this.bigram_restaurants.entrySet()) {
	    _log_like += b_r.getValue().log_like(this.discount, this.strength);
	}
	for (Map.Entry<String, Restaurant> s_r : this.unigram_restaurants.entrySet()) {
	    _log_like += s_r.getValue().log_like(this.discount, this.strength);
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
