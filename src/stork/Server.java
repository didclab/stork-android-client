package stork;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.*;
import ch.boye.httpclientandroidlib.client.methods.*;
import ch.boye.httpclientandroidlib.conn.*;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.entity.mime.*;
import ch.boye.httpclientandroidlib.entity.mime.content.*;
import ch.boye.httpclientandroidlib.impl.client.*;
import ch.boye.httpclientandroidlib.impl.conn.*;
import ch.boye.httpclientandroidlib.params.*;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import stork.ad.*;
import android.annotation.SuppressLint;
import android.util.Log;

/**
 * Class used to contact with rest api(s). TODO port to async classes for
 * multi-threading env
 * 
 * ftp://23.20.170.141
 */
public class Server {
	public static String TAG = Server.class.getSimpleName();
	static volatile int request_count = 1;
	static URI stork_uri = URI.create("http://didclab-ws4.cse.buffalo.edu:9000");
	public static volatile HttpClient httpclient;
	public static List<String> credentialKeys = new ArrayList<String>();
	//private static ClientConnectionManager conman =
		//	new PoolingClientConnectionManager();

	static  {
		httpclient = createHttpClient();
		credentialKeys.add("");
	}
	
	public static HttpClient createHttpClient() {
		HttpParams params = new BasicHttpParams();
		params.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);
		
		PoolingClientConnectionManager cxMgr = new PoolingClientConnectionManager( SchemeRegistryFactory.createDefault());
		cxMgr.setMaxTotal(30);//earlier it was set to 100 which worked fine
		cxMgr.setDefaultMaxPerRoute(20);
		return new DecompressingHttpClient(new DefaultHttpClient(cxMgr, params));
		//return new DefaultHttpClient();
	}

	// Helper methods for converting ads to HttpClient parameters.
	public static String adToQueryString(Ad ad) {
		String s = null;
		Map<String, String> map = new HashMap<String, String>();

		for (Map.Entry<Object, AdObject> e : ad.entrySet()) {
			if (s != null) s = s+"&";
			else s = "?";
			s = s+(e.getKey()+"="+URLEncoder.encode(e.getValue().asString()));
		} return s == null ? "" : s;
	}

	public static MultipartEntity adToMultipart(Ad ad) {
		MultipartEntity me = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		for (Map.Entry<Object, AdObject> e : ad.entrySet()) try {
			System.out.println(e);
			me.addPart((String) e.getKey(), new StringBody(e.getValue().toString()));
		} catch (UnsupportedEncodingException e1) {
			// Oh well.
		} return me;
	}
	
	// Methods to communicate with the REST service.
	public static Ad sendRequest(String path) {
		return sendRequest(path, null);
	} public static Ad sendRequest(String path, Ad ad) {
		return sendRequest(null, path, ad, null);
	} public static Ad sendRequest(String path, Ad ad, String method) {
		return sendRequest(null, path, ad, method);
	} public static Ad sendRequest(HttpClient hc, String path, Ad ad, String method) {
		return Ad.parse(sendRequestRaw(hc, path, ad, method));
	} public static String sendRequestRaw(HttpClient hc, String path, Ad ad, String method) {
		URI uri = stork_uri.resolve(path);
		return sendHTTPRequest(hc, uri, ad, method);
	} @SuppressWarnings("deprecation")
	public static String sendHTTPRequest(HttpClient hc, URI uri, Ad ad, String method) {
		if (hc == null) hc = httpclient;
		if (ad == null) ad = new Ad();
		if (method == null) method = "GET";
		HttpRequestBase req;
		method = method.toUpperCase();
		HttpResponse resp;
		
		try {
			// Create HTTP request.
			if (method.equals("GET")) {
				String s = adToQueryString(ad);
				Log.v("response from adToQuery", s);
				System.out.println(s);
				System.out.println(uri.resolve("."+adToQueryString(ad)));
				req = new HttpGet(uri.resolve(adToQueryString(ad)));
			} else if (method.equals("POST")) {
				/*
				req = new HttpPost(uri);
				req.addHeader("Content-Type", "multipart/form-data");
				((HttpPost) req).setEntity(adToMultipart(ad));
				*/
				req = new HttpPost(uri);
				req.addHeader("Content-Type", "application/x-www-form-urlencoded");
				((HttpPost) req).setEntity(new StringEntity(adToQueryString(ad).substring(1)));
			} else {
				throw new RuntimeException("Invalid method: " + method);
			}

			// Send request.
			Log.v("URI = " + method, req.getURI().toString());
			resp = hc.execute(req);
			Log.v("Response", resp.toString());
			// Check that response was positive.
			if (resp.getStatusLine().getStatusCode() / 100 != 2) {
				String s = EntityUtils.toString(resp.getEntity());
				throw new RuntimeException("error: "+s);
			}
	
			// Get the response entity.
			String s = EntityUtils.toString(resp.getEntity());
			resp.getEntity().consumeContent();
			return s;
		} catch (RuntimeException e) {
			Log.v(TAG + " sendRequest", e.toString());
			throw e;
		} catch (Exception e) {
			Log.v(TAG + " sendRequest", e.toString());
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Sends a Job Progress request to the Stork server
	 * 
	 * @param Why even add Javadocs if no one even updates them.
	 */
	@SuppressLint("UseSparseArrays")  // Shut up Eclipse, let me do what I want.
	public static Map<Integer, Ad> getQueue(String id) {
		try {
			Map<Integer, Ad> map = new HashMap<Integer, Ad>();
			Ad ad = sendRequest("/api/stork_q");
			for (Entry<Object, AdObject> e : ad.entrySet()) {
				Ad a = e.getValue().asAd();
				map.put(a.getInt("job_id"), a);
			} return map;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sends a Job Cancel request to the Stork server
	 */
	public static String sendJobCancelRequest(Long id) {
		try {
			return sendRequest("/api/stork_rm", new Ad("job_id", id), "POST").toString();
		} catch (Exception e) {
			Log.v("Server - Job Cancel Request", e.toString());
			return null;
		}
	}

	/**
	 * Get sub-dir list for a root directory.
	 */
	public static Ad getListings(TreeView tv) {
		return getListings(null, tv);
	} public static Ad getListings(HttpClient hc, TreeView tv) {
		return getListings(null, tv, null);
	} public static Ad getListings(HttpClient hc, TreeView tv, Ad opts) {
		if (opts == null)
			opts = new Ad();
		opts.put("uri", tv.getURI().toASCIIString());
		opts.put("cred", tv.getCred());
		opts.put("depth", 0);
		return sendRequest("/api/stork_ls", opts);
	}
}
