package npylm;

import java.util.*;


public class Restaurant {

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
	if (!this.tables.containsKey(dish)) {
	    List<Integer> _tables = new LinkedList<Integer>();
	    this.tables.put(dish, _tables);
	    this.total_table_n += 1;
	}
	this.tables.get(dish).add(customer_n);
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
	    
    /*
      For gibbs sampling.
      return true if new customer sits in new table
    */
    /*
    public boolean add_customer(String customers_dish) {
    }
    */
    /*
      For gibbs sampling.
      return true if left talbe is empty.
    */
    /*
    public boolean remove_customer(String customers_dish) {
    }
    */
}
