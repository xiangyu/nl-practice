import java.io.*;
import java.util.*;


public class Naive_Bayes {
    public static void main(String[] argv) throws IOException {
	Trainer t = new Trainer();
	Map<String, Map<String, Double>> model =  t.train(argv[0]);

	String[] dcmt = {"IT", "HTML", "HTTP", "bank", "IT"};
	Classifier c = new Classifier(model);
	System.out.println(c.d_classify(Arrays.asList(dcmt)));
    }
}
