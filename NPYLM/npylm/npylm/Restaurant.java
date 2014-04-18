package npylm;

import java.util.*;
import org.apache.commons.math3.special.Gamma;


public class Restaurant {

    private static Random rand_gen = new Random();

    public int total_customer_n;
    public int total_table_n;
    public Map<String, List<Integer>> tables;

    public Restaurant() {
	this.total_customer_n = 0;
	this.total_table_n    = 0;
	this.tables = new HashMap<String, List<Integer>>(1000);
    }

    public void add_new_table(String dish, int customer_n) {
	if (!this.tables.containsKey(dish)) {
	    List<Integer> _tables = new LinkedList<Integer>();
	    this.tables.put(dish, _tables);
	    this.total_table_n += 1;
	}
	this.tables.get(dish).add(customer_n);
	this.total_customer_n += customer_n;
    }

    /*
      add new customer. If same dish is already searved, we make her sit there.
      Else we make her sit new table.
    */
    public void add_new_customer(String customers_dish, int customer_n) {
	if (!this.tables.containsKey(customers_dish)) {
	    List<Integer> _tables = new LinkedList<Integer>();
	    this.tables.put(customers_dish, _tables);
	    this.total_table_n += 1;
	}
	this.tables.get(customers_dish).add(customer_n);
	this.total_customer_n += customer_n;
    }
    public void add_new_customer(String customers_dish) {
	this.add_new_customer(customers_dish, 1);
    }

    public int customer_n_for_dish(String dish) {
	if (!this.tables.containsKey(dish)) {
	    return 0;
	}
	List<Integer> dish_tables = this.tables.get(dish);
	int total = 0;
	for (Integer c_n : dish_tables) {
	    total += c_n;
	}
	return total;
    }
	    

    //For gibbs sampling. Return true if new customer sits in new table
    public boolean add(String customers_dish, double discount, double new_prob) {
	List<Integer> tables_for_dish = this.tables.get(customers_dish);
	List<Double> probs = new ArrayList<Double>();
	for (Integer c_n : tables_for_dish) {
	    probs.add(c_n - discount);
	}
	probs.add(new_prob);

	int add_index = this.sample(probs);
	if (add_index == probs.size() - 1) {
	    tables_for_dish.add(1);
	    this.total_table_n += 1;
	    this.total_customer_n += 1;
	    return true;
	}

	ListIterator<Integer> it = tables_for_dish.listIterator();
	for (int i = 0; i < add_index; ++i) {
	    it.next();
	}
	it.set(it.next() + 1);
	this.total_customer_n += 1;
	return false;
    }

    //For gibbs sampling. Return true if left talbe is empty.
    public boolean remove(String customers_dish) {
	List<Integer> tables_for_dish = this.tables.get(customers_dish);
	List<Double> probs = new ArrayList<Double>();
	for (Integer c_n : tables_for_dish) {
	    probs.add(new Double(c_n));
	}

	int remove_index = this.sample(probs);
	ListIterator<Integer> it = tables_for_dish.listIterator();
	for (int i = 0; i < remove_index; ++i) {
	    it.next();
	}
	if (it.next() == 1) {
	    it.remove();
	    this.total_table_n -= 1;
	    this.total_customer_n -= 1;
	    return true;
	}

	it.set(it.next() - 1);
	this.total_customer_n -= 1;
	return false;
    }

    public double log_like(double discount, double strength) {
	double new_term, occ_term;

	new_term = this.log_factorial(strength, discount, this.total_table_n) -
	    this.log_factorial(strength, this.total_customer_n, 1);

	occ_term = 0.0;
	for (Map.Entry<String, List<Integer>> s_l : this.tables.entrySet()) {
	    for (Integer c_n : s_l.getValue()) {
		occ_term += this.log_factorial(1 - discount, 1.0, c_n - 1);
	    }
	}

	return new_term + occ_term;
    }

    private double log_factorial(double a, double b, int c) {
	if (c <= 0) {
	    return 1.0;
	}
	
	return c * Math.log(b) + Gamma.logGamma(a / b + c) - Gamma.logGamma(a / b);
    }

    private int sample(List<Double> probs) {
	double total = 0.0;
	for (Double p : probs) {
	    total += p;
	}

	double rand = this.rand_gen.nextDouble() * total;
	int i = 0;
	double  accm = 0.0;
	for (Double p : probs) {
	    if (p < rand) {
		return i;
	    } else {
		accm += p;
		i += 1;
	    }
	}

	return probs.size() - 1;
}
