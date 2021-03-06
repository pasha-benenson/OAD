package com.example.demoapp;

import java.io.*;
import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
//import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
//import org.json.JSONException;
import org.json.JSONObject;

//import net.oauth.jsontoken;
//import org.json.*;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Base64;


public class OAuthHelper {
	public static String RefreshToken="";
	public static String AccessToken="";
	public static String OIDCClaims="";
//	public static final String PREFS_NAME = "OAuth_demo_PREF_NAME";
	private static Activity activity;
	private static final String BaseURL = "https://sso.pbens.com:9031";
	private static final String UserInfo = "/idp/userinfo.openid";
	private static final String AuthEndPoint="/as/authorization.oauth2"; 
	private static final String TokenEndPoint="/as/token.oauth2";

	private static final String clientId = "mobile_client2";
	private static final String clientSecret = "lkjlkj";
	private static final String scope="openid%20profile%20address%20email%20phone";//openid scope is required for OpenIdConnect tokens to be returned
	private static final String AuthEndPointParams = "?client_id=" + clientId + "&response_type=code&PartnerIdpId=PBENS:SAML2&scope=" + scope;
	//	 AuthEndPointParams = "?client_id=mobile_client&response_type=code&pfidpadapterid=Form1&scope=openid%20profile%20address%20email%20phone";
    private static final String AuthCodeFlowUrl = BaseURL + AuthEndPoint + AuthEndPointParams;

    private static final String WebServiceURL = "http://pbens.com/api/index.php";
	static Time expires=new Time();
	
	
	public OAuthHelper(Activity ac) {
		activity=ac;
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy); 
		
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
				// TODO Auto-generated method stub

			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
				// TODO Auto-generated method stub

			}
		} };

		try {
			SSLContext sc = SSLContext.getInstance("SSL");

			sc.init(null, trustAllCerts, new java.security.SecureRandom());

			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (KeyManagementException kme) {
			kme.printStackTrace();
		} catch (NoSuchAlgorithmException nsae) {
			nsae.printStackTrace();
		}
	

	}
	public  String setRefreshToken(String Username, String Password){
		return callRefreshToken("",Username, Password);
	}
	public  String setRefreshToken(String data){
		return callRefreshToken(data,"","");
	}
	/*
	 * Get RefreshToken either out of Code or UserNamePassword
	 * and setRefreshToken, AccessToken, IDToken, etc..
	 * 
	 */
	private  String callRefreshToken(String data, String Username, String Password){
		
		
		URL hurl;
		try {
			hurl = new URL(BaseURL + TokenEndPoint);
	
			HostnameVerifier v = new HostnameVerifier() {
							@Override
				public boolean verify(String arg0, SSLSession arg1) {
					// TODO Auto-generated method stub
					return true;
				}
			};
	
           
            
		
			HttpsURLConnection https = (HttpsURLConnection) hurl.openConnection();
			https.setHostnameVerifier(v);
			https.setReadTimeout(10000);
			https.setConnectTimeout(15000);
			https.setRequestMethod("POST");
			https.setDoInput(true);
			https.setDoOutput(true);

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			if (Username==""){
				String code=data.substring( data.indexOf("?code=")+6);
				System.out.println("code = " + code);

			
			params.add(new BasicNameValuePair("code", code));
			params.add(new BasicNameValuePair("grant_type", "authorization_code"));
			params.add(new BasicNameValuePair("client_id", clientId));
			params.add(new BasicNameValuePair("client_secret", "lkjlkj"));
			
			}
			else {
				
				
				params.add(new BasicNameValuePair("grant_type", "password"));
				params.add(new BasicNameValuePair("client_id", clientId));
				params.add(new BasicNameValuePair("client_secret", "lkjlkj"));
				params.add(new BasicNameValuePair("username", Username));
				params.add(new BasicNameValuePair("password", Password));
				params.add(new BasicNameValuePair("validator_id", "ldap1"));
			
			}
			OutputStream os = https.getOutputStream();
			BufferedWriter writer = new BufferedWriter(
			        new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(params));
			writer.close();
			os.close();
		    System.out.println(getQuery(params));
			https.connect();
		      int status = https.getResponseCode();

		    System.out.println("status code= " + status);
		        
		    BufferedReader br = new BufferedReader(new InputStreamReader(https.getInputStream()));
		    StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			String rdata=sb.toString();	
			System.out.println("response data: "+ rdata);
			JSONObject json = new JSONObject(rdata);
			System.out.println(json);
			RefreshToken=json.getString("refresh_token");
			
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
			Editor edit = preferences.edit();
            edit.putString("RefreshToken", RefreshToken);
            
            edit.commit();

			
			AccessToken=json.getString("access_token");
			String expires_in=json.getString("expires_in");
			Time t = new Time();
			
			expires=new Time();
			expires.set(t.toMillis(false)+Integer.parseInt(expires_in)*1000);
			
			System.out.println("Refresh Token:" + RefreshToken);
			System.out.println("Access Token" + AccessToken);
			System.out.print("time now "); System.out.println(t);
			System.out.print("expires "); System.out.println(expires);
			
			// Get ID_Token if it is there
			
			String id_token=json.getString("id_token");
//			
			String encoded_claims=id_token.split("\\.")[1];
			String claims = new String(Base64.decode(encoded_claims,Base64.DEFAULT), "UTF-8");
			System.out.println("claims: " + claims);
			
			OIDCClaims=fromJSONtoTable(claims);
			OIDCClaims=OIDCClaims.replace("https:\\/\\/\n","https://");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return RefreshToken;
	}
	/*
	 * Read refresh token out of storage (Shared Memory Space)
	 * */
	public static  String readRefreshToken(){
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
            return preferences.getString("RefreshToken","");            		
	}	
	/*
	 * get RefreshToken ( either from variable, or Shared Memory or by calling Authorization Codeflow )
	 * */
	
	public   String getRefreshToken(boolean authenticate){
		System.out.println("getRefreshToken "+ RefreshToken );

		if (RefreshToken==""){
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		    RefreshToken = preferences.getString("RefreshToken","");
		    System.out.println("got token from storage "+ RefreshToken );
		    if (RefreshToken=="" && authenticate){
		    	
		    	authenticate();
		    }
		
		}
		
            return RefreshToken;
            
		
	}
	/*
	 * Helper function for creating POST parameters
	 * */
	private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
	{
	    StringBuilder result = new StringBuilder();
	    boolean first = true;

	    for (NameValuePair pair : params)
	    {
	        if (first)
	            first = false;
	        else
	            result.append("&");

	        result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
	        result.append("=");
	        result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
	    }

	    return result.toString();
	}
	
	/*
	 * call api on pbens.com that validates Access Token against PF
	 * */
	public  String callapi(String queryparameter){
		URL hurl;
		try {
			hurl = new URL(WebServiceURL + queryparameter);
	
		
			
           
            
		
			HttpURLConnection http =  (HttpURLConnection) hurl.openConnection();
			
			http.setReadTimeout(10000);
			http.setConnectTimeout(15000);
			http.setRequestMethod("POST");
			http.setDoInput(true);
			http.setDoOutput(true);
            http.setRequestProperty("Authorization", "Bearer " + getAccessToken());

			http.connect();
		      int status = http.getResponseCode();

		    System.out.println("status code= " + status);
		        
		    BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
		    StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			String rdata=sb.toString();	
			System.out.println("response data: "+ rdata);
			JSONObject json = new JSONObject(rdata);
			System.out.println(json);

		return json.getString("data");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
		
	}

	/*
	 * Call Web Service
	 * */
	public  String callREST(String surl){
		URL hurl;
		BufferedReader br;
		try {
			hurl = new URL(surl);
	
		
			HostnameVerifier v = new HostnameVerifier() {
				@Override
					public boolean verify(String arg0, SSLSession arg1) {
						// TODO Auto-generated method stub
						return true;
					}
				};




			
			if(surl.contains("https://")) {
				HttpsURLConnection https = (HttpsURLConnection) hurl.openConnection();
				https.setHostnameVerifier(v);
				https.setReadTimeout(10000);
				https.setConnectTimeout(15000);
				https.setRequestMethod("GET");
				https.setDoInput(true);
				https.setDoOutput(true);
				
	            https.setRequestProperty("Authorization", "Bearer " + getAccessToken());
	
				https.connect();
			      int status = https.getResponseCode();
	
			    System.out.println("status code= " + status);
			        
			    br = new BufferedReader(new InputStreamReader(https.getInputStream()));
			}
			else {				
			HttpURLConnection https = (HttpURLConnection) hurl.openConnection();
			
			https.setReadTimeout(10000);
			https.setConnectTimeout(15000);
			https.setRequestMethod("GET");
			https.setDoInput(true);
			https.setDoOutput(true);
			
            https.setRequestProperty("Authorization", "Bearer " + getAccessToken());

			https.connect();
		      int status = https.getResponseCode();

		    System.out.println("status code= " + status);
		        
		    br = new BufferedReader(new InputStreamReader(https.getInputStream()));

				
			}
			
		    StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			String rdata=sb.toString();	
			System.out.println("response data: "+ rdata);
			JSONObject json = new JSONObject(rdata);
			System.out.println(json);

		return json.getString("data");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
		
	}
    /*
     * Get Access Token, get access token change activate to true to automatically start Authorization code flow
     */
	private  String getAccessToken() {
		boolean activate=false;
		Time t = new Time();
		t.setToNow();
		System.out.println("getAccessToken "+ AccessToken + expires.toString());
		if (AccessToken!="" && expires.after(t))
			return AccessToken;
		else{
			RefreshToken=getRefreshToken(activate);
			// use refresh token to get access token
			URL hurl;
			try {
				hurl = new URL("https://sso.pbens.com:9031/as/token.oauth2");
		
				HostnameVerifier v = new HostnameVerifier() {
								@Override
					public boolean verify(String arg0, SSLSession arg1) {
						// TODO Auto-generated method stub
						return true;
					}
				};
	           
	            
			
				HttpsURLConnection https = (HttpsURLConnection) hurl.openConnection();
				https.setHostnameVerifier(v);
				https.setReadTimeout(10000);
				https.setConnectTimeout(15000);
				https.setRequestMethod("POST");
				https.setDoInput(true);
				https.setDoOutput(true);

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("refresh_token", RefreshToken));
				params.add(new BasicNameValuePair("grant_type", "refresh_token"));
				params.add(new BasicNameValuePair("client_id", clientId));
				params.add(new BasicNameValuePair("client_secret", clientSecret));

				OutputStream os = https.getOutputStream();
				BufferedWriter writer = new BufferedWriter(
				        new OutputStreamWriter(os, "UTF-8"));
				writer.write(getQuery(params));
				writer.close();
				os.close();
				
				 System.out.println(getQuery(params));
				 
				https.connect();
			      int status = https.getResponseCode();

			    System.out.println("status code= " + status);
			        
			    BufferedReader br = new BufferedReader(new InputStreamReader(https.getInputStream()));
			    StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();
				String rdata=sb.toString();	
				System.out.println("response data: "+ rdata);
				JSONObject json = new JSONObject(rdata);
				System.out.println(json);

				
				AccessToken=json.getString("access_token");
				String expires_in=json.getString("expires_in");
				
				
				
				expires=new Time();
				expires.set(t.toMillis(false)+Integer.parseInt(expires_in)*1000);
				

				System.out.println("Refresh Token:" + RefreshToken);
				System.out.println("Access Token" + AccessToken);
				System.out.print("time now "); System.out.println(t);
				System.out.print("expires "); System.out.println(expires);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return AccessToken;
		}
	}
	/*
	 * Start Authorization Code Flow 
	 * */
	public void authenticate(){
		System.out.println("about to  authenticate" );
		Time time=new Time();
		time.setToNow();
		
    	//adding nonce to make sure browser does not cash old response
		Uri uriUrl = Uri.parse(AuthCodeFlowUrl + "&nonce=" + time.toString());

    	Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
		activity.startActivity(launchBrowser);
	}
	/*
	 * Clear all values, the app forgets everything and acts as though it was just downloaded
	 * */
	public static void clear(){
		RefreshToken="";
		AccessToken="";
		OIDCClaims="";
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		Editor edit = preferences.edit();
        edit.putString("RefreshToken", RefreshToken);
        
        edit.commit();	

	}
	/*
	 * Quick and Dirty convert json into a table looking string for easy viewing
	 * */
	public static String fromJSONtoTable(String sjson){
		
		return sjson.replace(",", ",\n");
	}
	/*
	 * Call getuserinfo endpoint to get identinty claims for OpenIDConnect  ( notice AccessToken and openid scope is required for this operation)
	 * 
	 */
	public  String getUserInfo(){
		URL hurl;
		try {
			
			hurl = new URL(BaseURL + UserInfo);	
			HostnameVerifier v = new HostnameVerifier() {
							@Override
				public boolean verify(String arg0, SSLSession arg1) {
					// TODO Auto-generated method stub
					return true;
				}
			};
           
            
		
			HttpsURLConnection https = (HttpsURLConnection) hurl.openConnection();
			https.setHostnameVerifier(v);
			https.setReadTimeout(10000);
			https.setConnectTimeout(15000);
			https.setRequestMethod("GET");
			https.setDoInput(true);
			https.setDoOutput(true);
			
			
			
            https.setRequestProperty("Authorization", "Bearer " + getAccessToken());

			https.connect();
		      int status = https.getResponseCode();

		    System.out.println("status code= " + status);
		        
		    BufferedReader br = new BufferedReader(new InputStreamReader(https.getInputStream()));
		    StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			String rdata=sb.toString();	
			/*System.out.println("response data: "+ rdata);
			JSONObject json = new JSONObject(rdata);
			System.out.println(json);*/

		return rdata;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
		
	}	
}


