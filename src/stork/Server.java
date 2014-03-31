package stork;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stork.ad.Ad;
import stork.ad.AdObject;
import stork.main.StorkClientActivity;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpVersion;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.client.methods.HttpRequestBase;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.entity.mime.HttpMultipartMode;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntity;
import ch.boye.httpclientandroidlib.entity.mime.content.ContentBody;
import ch.boye.httpclientandroidlib.entity.mime.content.FileBody;
import ch.boye.httpclientandroidlib.entity.mime.content.StringBody;
import ch.boye.httpclientandroidlib.impl.client.DecompressingHttpClient;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.impl.conn.PoolingClientConnectionManager;
import ch.boye.httpclientandroidlib.impl.conn.SchemeRegistryFactory;
import ch.boye.httpclientandroidlib.params.BasicHttpParams;
import ch.boye.httpclientandroidlib.params.CoreConnectionPNames;
import ch.boye.httpclientandroidlib.params.CoreProtocolPNames;
import ch.boye.httpclientandroidlib.params.HttpParams;
import ch.boye.httpclientandroidlib.util.EntityUtils;

public class Server {
	public static final String TAG = Server.class.getSimpleName();
	private static final URI stork_uri = URI.create("https://storkcloud.org");
	private static volatile HttpClient httpclient;
	private static List<String> credentialKeys = new ArrayList<String>();
	private static Ad cookie = new Ad();
	private static final String mWalledGardenUrl = "http://clients3.google.com/generate_204"; //for overcoming the firewall.
	private static final int WALLED_GARDEN_SOCKET_TIMEOUT_MS = 10000;
	
	public static boolean overWrite = false;
	public static boolean xferOptimization = false;
	public static boolean fileIntegrity = false;
	public static boolean EdataChannel = false;
	public static boolean CdataChannel = false;
	
	public URI getStorkUri(){
		return stork_uri;
	}
	
	public static List<String> getCredentials(){
		return credentialKeys;
	}
	public static void setCookie(Ad ad){
		if(ad != null){
			cookie = ad;
		}
	}
	public static Ad getCookie(){
		return cookie;
	}
	static{
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
	}

	// Helper methods for converting ads to HttpClient parameters.
	public static String adToQueryString(Ad ad) {
		String s = "";
		Log.v("Ad", ad.toString());
		Map<String, String> map = new HashMap<String, String>(); 

		// Flatten ad up to one level.
		for (String k : ad.keySet()) {
			AdObject o = ad.getObject(k);
			if (o.isAd()) for (String k2 : o.asAd().keySet()) {
				map.put(k+"."+k2, o.asAd().get(k));
			} else {
				map.put(k, ad.get(k));
			}
		}	
		// Make query string.
		for (String k : map.keySet()) {
			if (map.get(k) == null) continue;
			if (!s.isEmpty()) s += "&";
			s += k+"="+URLEncoder.encode(map.get(k));
		}
		
		return s;
	}

	public static MultipartEntity adToMultipart(Ad ad) {
		MultipartEntity me = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		for (String s : ad.keySet()) try {
			me.addPart(s, new StringBody(ad.get(s)));
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
		if (method == null) method = "POST";
		HttpRequestBase req;
		method = method.toUpperCase();
		
		
		try {
			if(ad.containsKey("src")){
				ad = ad.put("user", cookie)
				.put("options.optimizer", xferOptimization)
				.put("options.overwrite", overWrite)
				.put("options.verify", fileIntegrity)
				.put("options.encrypt", EdataChannel)
				.put("options.compress", CdataChannel);
				resetTransferOptions();
			}
			if(ad.containsKey("action")){
				if(!ad.get("action").equals("login")){
					ad = ad.put("user", cookie);
				}
			}
			else{
				System.out.println("ad.containsValue = login ?"+ad.containsValue("login"));
				ad = ad.put("user", cookie);
			}
			
			Log.v("Request Ad = ", ad.toString(true));
			// Create HTTP request.
			if (method.equals("GET")) {
				String s = adToQueryString(ad);
				if (s != null && !s.isEmpty()) {
					if (uri.getQuery() != null)
						s = uri.getQuery()+"&"+s;
					uri = uri.resolve("?"+s);
				}
				req = new HttpGet(uri);
			} else if (method.equals("POST")) {
				/*
				req = new HttpPost(uri);
				req.addHeader("Content-Type", "multipart/form-data");
				((HttpPost) req).setEntity(adToMultipart(ad));
				*/
				System.out.println("uri = "+uri);
				req = new HttpPost(uri);
				req.addHeader("Content-Type", "application/json");
				((HttpPost) req).setEntity(new StringEntity(ad.toJSON()));
			} else {
				throw new RuntimeException("Invalid method: " + method);
			}

			/* Send request.
			String s = new SendRequest().execute(req, hc).get(3, TimeUnit.SECONDS);
			return s;*/
			HttpResponse resp;
			Log.v("URI = ", req.toString());
		
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
			e.printStackTrace();
			Log.v(TAG + " sendRequest", e.toString());
			StorkClientActivity.showToast(e.getMessage());
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			Log.v(TAG + " sendRequest", e.toString());
			if(e.getMessage().contains("not resolve host"))
				StorkClientActivity.showToast("Please check your internet connection");
			throw new RuntimeException(e);
		}
		
	}
	
	//Uploading files from the client to a remote server.
	private static class SendRequest extends AsyncTask<Object, Void, String>{

		@Override
		protected String doInBackground(Object... objs) {
			try {
			HttpRequestBase req = (HttpRequestBase) objs[0];
			HttpClient hc = (HttpClient) objs[1];
			HttpResponse resp;
			Log.v("URI = ", req.toString());
		
			resp = hc.execute(req);
			
			Log.v("Response", resp.toString());
			
			// Check that response was positive.
			if (resp.getStatusLine().getStatusCode() / 100 != 2) {
				String s = EntityUtils.toString(resp.getEntity());
				throw new RuntimeException("error: "+s);
			}
			// Get the response entity.
			String s = EntityUtils.toString(resp.getEntity());
			return s;
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String v){
			super.onPostExecute(v);
		}
		
	}
	
	public static void upload(HttpClient hc, String filepath)
    {
      try
      {
        hc.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        HttpPost httppost = new HttpPost("youe url");
        File file = new File(filepath);
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file, " -mime type here!-image/jpeg");
        mpEntity.addPart("userfile", cbFile);
        httppost.setEntity(mpEntity);

        HttpResponse response = hc.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        //Server response...
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
   }
	
	private static void resetTransferOptions() {
		xferOptimization = false;
		overWrite = false;
		fileIntegrity = false;
		EdataChannel = false;
		CdataChannel = false;
	}

	/**
	 * Sends a Job Progress request to the Stork server
	 * 
	 * @param Why even add Javadocs if no one even updates them.
	 */
	@SuppressLint("UseSparseArrays")  // Shut up Eclipse, let me do what I want.
	public static Map<Integer, Ad> getQueue() {
		try {
			Map<Integer, Ad> map = new HashMap<Integer, Ad>();
			Ad ad = sendRequest("/api/stork/q?status=all");
			for (Ad a : ad.getAds()) {
				map.put(a.getInt("job_id"), a);
			} return map;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sends a Job Cancel request to the Stork server
	 */
	public static String sendJobCancelRequest(Long id) {
		try {
			return sendRequest("/api/stork/rm", new Ad("job_id", id), "POST").toString();
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
		
		// Fixes the problem with the URI not containing a trailing slash.
		String uri = tv.getURI().toASCIIString();
		if (!uri.endsWith("/"))
			uri += "/";
		
		opts.put("uri", uri);
		opts.put("cred", tv.getCred());
		opts.put("depth", 1);
		return sendRequest("/api/stork/ls", opts);
	}
	public static boolean isWalledGardenConnection() {
	    HttpURLConnection urlConnection = null;
	    
	    try {
	        URL url = new URL(mWalledGardenUrl); // "http://clients3.google.com/generate_204"
	        urlConnection = (HttpURLConnection) url.openConnection();
	        urlConnection.setInstanceFollowRedirects(false);
	        urlConnection.setConnectTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
	        urlConnection.setReadTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
	        urlConnection.setUseCaches(false);
	        urlConnection.getInputStream();
	        // We got a valid response, but not from the real google
	        Log.v("Response code", urlConnection.getResponseCode()+"");
	        return urlConnection.getResponseCode() == 204;
	    } catch (IOException e) {
	            Log.v("Walled garden check - probably not a portal: exception ", e.toString());
	        return false;
	    } finally {
	        if (urlConnection != null) {
	        	Log.v("urlConnection", "disconnected");
	        	urlConnection.disconnect();
	        }
	    }
	}
}
