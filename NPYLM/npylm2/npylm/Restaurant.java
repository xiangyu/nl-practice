package npylm;

import java.util.*;


public class Restaurant {
    public static double[] param_d = {0.2, 0.2, 0.2};
    public static double[] param_theta = {0.2, 0.2, 0.2};

    public int total_customer_n;
    public List<Table> tables;

    public Restaurant() {
	this.total_customer_n = 0;
	this.tables = new LinkedList<Table>();
    }

    public void add_new_table(String dish, int customer_n) {
	Table table = new Table(customer_n, dish);
	tables.add(table);
	this.total_customer_n += customer_n;
	System.out.println(this.total_customer_n);
    }
    /*
      add new customer. If same dish is already searved, we make her sit there.
      Else we make her sit new table.
    */
    public void add_new_customer(String customers_dish) {
	this.total_customer_n += 1;
	for (Table table : this.tables) {
	    if (table.dish.equals(customers_dish)) {
		table.customer_n++;
		return;
	    }
	}
	Table table = new Table(1, customers_dish);
	tables.add(table);
    }


    // return talbe's customer number for all dish which is served.
    public Map<String, List<Integer>> get_customer_list() {
	Map<String, List<Integer>> customer_list = new HashMap<String, List<Integer>>();

	for (Table table : this.tables) {
	    String dish = table.dish;
	    if (customer_list.containsKey(dish)) {
		customer_list.get(dish).add(table.customer_n);
	    } else {
		List<Integer> list = new ArrayList<Integer>(100);
		list.add(table.customer_n);
		customer_list.put(dish, list);
	    }
	}

	return customer_list;
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
