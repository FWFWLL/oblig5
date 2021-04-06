import java.util.HashMap;
import java.util.ArrayList;

public final class HashMapContainer {
    private ArrayList<HashMap<String, Subsequence>> hashMaps = new ArrayList<HashMap<String, Subsequence>>();

    public ArrayList<HashMap<String, Subsequence>> get() {return hashMaps;}
    public int size() {return hashMaps.size();}

    public void add(HashMap<String, Subsequence> hashMap) {hashMaps.add(hashMap);}
}