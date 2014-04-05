import java.util.*;


public class Hoge2 {
    public static void main(String[] args) {
	Map<String, Integer> counter = new HashMap<String, Integer>();

	String[] s1 = {"a", "a", "b", "c", "a", "b"};

	for (int i = 0; i < 6; ++i) {
	    if (counter.containsKey(s1[i])) {
		counter.put(s1[i], counter.get(s1[i]) + 1);
	    } else {
		counter.put(s1[i], 1);
	    }
	}

	String s = "";
	s = s + "a";
	if (counter.containsKey(s)) {
	    counter.put(s, counter.get(s) + 1);
	} else {
	    counter.put(s, 1);
	}

	for (Map.Entry<String, Integer> e : counter.entrySet()) {
	    System.out.println(e.getKey() + " " + e.getValue());
	}
    }
}

