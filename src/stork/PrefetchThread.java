package stork;

import stork.ad.Ad;
import stork.cache.Cache;
import stork.main.StorkClientActivity;
import android.util.Log;
import ch.boye.httpclientandroidlib.client.HttpClient;

public class PrefetchThread extends Thread {
	//	public static Map<URI, Future<List<String>[]>> current_requests =
	//			new ConcurrentHashMap<URI, Future<List<String>[]>>();
	//	
	//	public static class Future<O> {
	//		O object = null;
	//		boolean canceled = false;
	//		public synchronized O get() {
	//			while (object == null && !canceled) try {
	//				wait();
	//			} catch (Exception e) { /* ignore */ }
	//			return object;
	//		}
	//		public synchronized void put(O o) {
	//			object = o;
	//			notifyAll();
	//		}
	//		public synchronized void cancel() {
	//			canceled = true;
	//			notifyAll();
	//		}
	//	}

	// String s;
	Watch w;
	public static String TAG = PrefetchThread.class.getSimpleName();
	public static volatile HttpClient httpclient;

	public PrefetchThread() {
		httpclient = Server.createHttpClient();
		Log.v(TAG,
				"Prefetching and caching Thread started");

	}

	public void run() {
		while (true) try {
			// Queue is used for prefetching the directories since it stores the URL's to 
			// be requested next and cache is the structure which stores the serverName, path 
			// and the actual directory structure
			TreeView tv = StorkClientActivity.queue.take();
			//Future<List<String>[]> future = null;

			w = new Watch(true);

			Ad ad  = null;
			//future = new Future<List<String>[]>();
			//current_requests.put(url, future);

			ad = Server.getListings(httpclient, tv);

			//future.put(listings_child);
			//current_requests.remove(url);

			if (ad == null) {
				Log.v(TAG,"Unable to access children");
			} 
			else {
				Cache.addToCache(tv.getURI(), ad);
			}

			Thread.sleep(100);
		} catch (InterruptedException e) {
			Log.d("PrefetchThread", "interrupted");
		} catch (Exception e) {
			Log.v("PFT Exception", e.toString());
		}
	}

}