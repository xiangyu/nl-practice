package npylm;

public class Bigram {
    public String first;
    public String second;

    Bigram(String first, String second) {
	this.first  = first;
	this.second = second;
    }

    @Override
    public boolean equals(Object o) {
	return this.first.equals(((Bigram)o).first) && this.second.equals(((Bigram)o).second);
    }

    @Override
    public int hashCode() {
	return this.first.hashCode() + this.second.hashCode();
    }
}
