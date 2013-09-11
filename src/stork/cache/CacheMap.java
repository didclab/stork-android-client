package stork.cache;

import java.util.*;
import java.lang.ref.*;

public class CacheMap<K,V> {
	private static final long serialVersionUID = -2105136638645261962L;
	private Map<K,SoftReference<W>> map;

	private class W {
		V v; K k;
		W(K k1, V v1) { v = v1; k = k1; }
	}
	
	public CacheMap() {
		map = new WeakHashMap<K,SoftReference<W>>();
	}
	
	public V put(K key, V value) {
		SoftReference<W> nr = (value == null) ? null :
			new SoftReference<W>(new W(key, value));
		SoftReference<W> vr = map.put(key, nr);
		return (vr == null) ? null :
		 (vr.get() == null) ? null : vr.get().v;
	}
	
	public V get(K key) {
		SoftReference<W> vr = map.get(key);
		return (vr == null) ? null :
		 (vr.get() == null) ? null : vr.get().v;
	}
	
	public void clear() {
		map.clear();
	}
}