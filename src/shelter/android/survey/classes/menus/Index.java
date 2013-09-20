package shelter.android.survey.classes.menus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import shelter.android.survey.classes.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class Index extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.index_layout);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void goToSurveys(View view) {
		try {
			FileInputStream file = openFileInput("all_surveys.json");
		} catch (FileNotFoundException e) {
			Toast.makeText(getApplicationContext(), "Download the surveys first." , Toast.LENGTH_LONG).show();
			return;
		}
	    Intent intent = new Intent(this, SurveySelect.class);
	    startActivity(intent);
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	private void alertConnectionUnavailable() 
	{
		AlertDialog.Builder altDialog= new AlertDialog.Builder(this);
		altDialog.setMessage("Internet connection unavailable");
		altDialog.setNeutralButton("OK", null);
		altDialog.show();
	}
	
	public void goToUpload(View view) 
	{
		if (isNetworkAvailable()==true)
		{
		    Intent intent = new Intent(this, UploadExisting.class);
		    startActivity(intent);
		}
		else
		{
			alertConnectionUnavailable();
		}
	}
	
	public void goToDownload(View view) throws Exception
	{
		if (isNetworkAvailable()==true)
		{

			URL url = new URL("https://survey.shelter-associates.org/android/download/");
			// Ignore unverified certificate
			trustAllHosts();
			HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
			https.setHostnameVerifier(DO_NOT_VERIFY);
			
			InputStream is = (InputStream) https.getContent();
			
			String FILENAME = "all_surveys.json";
			FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = is.read(bytes)) != -1) {
				fos.write(bytes, 0, read);
			}
	 		fos.close();
	 		https.disconnect();
	 		Toast.makeText(getApplicationContext(), "Download successful." , Toast.LENGTH_LONG).show();
		}
		else
		{
			alertConnectionUnavailable();
		}
	}
	
	// always verify the host - don't check for certificate
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	/**
	 * Trust every server - don't check for any certificate
	 */
	private static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}
			
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
