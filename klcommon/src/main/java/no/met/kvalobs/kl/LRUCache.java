package no.met.kvalobs.kl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by borgem on 03.11.16.
 */
public class LRUCache<K,V> extends LinkedHashMap<K,V> {
    int size;
    public LRUCache(int size ){
        super( (int)size/3, 0.75f, true); // Initiate as an LRU cache
        this.size = size;
    }

    @Override
    protected boolean 	removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > size;
    }

}
