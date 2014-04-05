import java.util.*;


public class Hoge3 {
    public static void main(String[] args) {
	Integer a = 2;
	Integer b = 2;
	Map<Integer, String> m = new HashMap<Integer, String>();

	String s = "a";
	String t = "b";

	m.put(a, s);
	m.put(b, t);

	a += 2;
	System.out.println(m.get(a));
    }
}

