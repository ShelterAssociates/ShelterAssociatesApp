package shelter.android.survey.classes.menus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import shelter.android.survey.classes.R;
import shelter.android.survey.classes.utils.InternetUtils;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class Index extends InternetUtils {

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

	public void goToUpload(View view) 
	{
		if (isNetworkAvailable()==true)
		{
			Intent intent = new Intent(this, UploadExisting.class);
			intent.putExtra("survey_group", "PUNE");
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
			Thread t  = new Thread()
			{

				public void run()
				{
					try{
						Looper.prepare();
						URL url = new URL("https://survey.shelter-associates.org/android/download/");
						//URL url = new URL("http://192.168.1.9/android/download/");
						
						// Ignore unverified certificate
						trustAllHosts();
						HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
						//HttpURLConnection https = (HttpURLConnection) url.openConnection();
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
						Toast.makeText(getCurrentContext(), "Download successful." , Toast.LENGTH_LONG).show();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					Looper.loop();
				}
			};

			t.start();
		}
		else
		{
			alertConnectionUnavailable();
		}
	}
	
	public void goToOnlineMapUpload(View view)
	{
		if (isNetworkAvailable()==true)
		{
			Intent intent = new Intent(Index.this, UploadMap.class);
			intent.putExtra("survey_group", "PUNE");
			startActivity(intent);
		}
		else
		{
			alertConnectionUnavailable();
		}
	}
	
	public void goToOnlinePCMCUpload(View view)
	{
		if (isNetworkAvailable()==true)
		{
			Intent intent = new Intent(Index.this, UploadExisting.class);
			intent.putExtra("survey_group", "PCMC");
			startActivity(intent);
		}
		else
		{
			alertConnectionUnavailable();
		}
	}
	
	
	public void goToOnlinePCMCMapUpload(View view)
	{
		if (isNetworkAvailable()==true)
		{
			Intent intent = new Intent(Index.this, UploadMap.class);
			intent.putExtra("survey_group", "PCMC");
			startActivity(intent);
		}
		else
		{
			alertConnectionUnavailable();
		}
	}
	
	@Override
	public void onBackPressed()
	{
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
	}
	
	public Context getCurrentContext()
	{
		return this;
	}

}
