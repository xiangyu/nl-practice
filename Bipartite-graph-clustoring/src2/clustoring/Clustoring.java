package clustoring;

import java.util.*;
import clustoring.Training;


public class Clustoring {
    public static void main(String[] args) {
	List<List<Double>> weights = new ArrayList<List<Double>>(10);
	List<Double> temp;

	for (int i = 0; i < 10; ++i) {
	    temp = new ArrayList<Double>(20);
	    weights.set(i, temp);
	}
    }
}
