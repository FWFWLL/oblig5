public final class Subsequence {
    private String sequence;
    private int occurences = 1;

    Subsequence(String subsequence) {sequence = subsequence;}

    public String getSequence() {return sequence;}
    public int getOccurences() {return occurences;}

    public Subsequence add(Subsequence subsequence) {
        occurences += subsequence.getOccurences();
        return this;
    }

    public String toString() {
        return "(" + sequence + ", " + occurences + ")";
    }
}