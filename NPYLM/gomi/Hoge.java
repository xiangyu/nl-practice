import java.util.*;


public class Hoge {
    public static void main(String[] args) {
	String[] a1 = {"hoge", "hoge", "hoge"};
	String[] a2 = {"hoge", "hoge", "hoge"};
	//List<String> a1 = Arrays.asList("hoge", "hoge", "hoge");
	//List<String> a2 = Arrays.asList("hoge", "hoge", "hoge");

	Map<String[], Integer> counter = new HashMap<String[], Integer>();
	System.out.println("AAA");
	if (counter.containsKey(a1)) {
	    System.out.println("B");
	    counter.put(a1, counter.get(a1) + 1);
	} else {
	    System.out.println("C");
	    counter.put(a1, 1);
	}
	System.out.println("D");
	if (counter.containsKey(a2)) {
	    counter.put(a2, counter.get(a2) + 1);
	} else {
	    counter.put(a2, 1);
	}
	System.out.println("Hoge");
	for (Map.Entry<String[], Integer> e : counter.entrySet()) {
	    System.out.println(e.getValue());
	}

	if (a1 == a2) {
	    System.out.println("V");
	} else {
	    System.out.println("U");
	}
    }
}

