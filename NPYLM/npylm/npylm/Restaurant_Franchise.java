package npylm;

import java.util.*;


public class Restaurant_Franchise {
    public Restaurant zerogram_restaurant;
    public Map<String, Restaurant> unigram_restaurants;
    public Map<Bigram, Restaurant> bigram_restaurants;

    public Restaurant_Franchise(Map<Bigram, Map<String, Integer>> counter) {
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
		int customer_n = unigram_restaurant.cutomer_n_for_dish(dish);
		this.zerogram_restaurant.add_new_customer(dish, customer_n);
	    }
	}
    }

    void gibbs_sampling(List<List<String>> sentences, int gibbs_n) {
    }
}

