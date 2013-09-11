package stork.cache;

import java.net.URI;
import stork.ad.Ad;

public final class Cache {
	private static CacheMap <URI, Ad> map = new CacheMap<URI, Ad>();

	public static final Cache instance = new Cache();

	private Cache() {
		if (instance != null) {
			throw new IllegalStateException("Already instantiated");
		}
	}

	public static Ad getFromCache(URI s) { 
		return map.get(s);
	}

	public static synchronized void addToCache(URI key, Ad value) {
		map.put(key, value);
	}
	
	public static synchronized void clear() {
		map.clear();
	}

	public static synchronized Cache getInstance() {
		return instance;
	}

	public static CacheMap<URI, Ad> getMap() {
		return map;
	}

	public static void setLinked_hash_map(CacheMap<URI, Ad> m) {
		Cache.map = m;
	}
}