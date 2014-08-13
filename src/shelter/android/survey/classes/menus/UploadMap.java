package shelter.android.survey.classes.menus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import shelter.android.survey.classes.R;
import shelter.android.survey.classes.forms.FormActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class UploadMap extends FormActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final DatabaseHandler db = new DatabaseHandler(this);
		ScrollView sv = new ScrollView(this);
		
		String inputJsonString = parseDownloadToString( "all_surveys.json" );
		ArrayList<List<String>> data=new ArrayList<List<String>>();
		data=db.getAllData(db.TABLE_PLOTTEDSHAPE_GROUP);				
		/***************** Condition checking Maps for upload ***************************************/
		if (data.isEmpty())
		{
			this.onBackPressed();
			Toast.makeText(getApplicationContext(), "No Online Maps to upload!" , Toast.LENGTH_LONG).show();
			return;
		}
		
		String template = FormActivity.parseAssetToString(this, "completed_onlinemap_slums_template.json" );
		JSONObject outputJson = new JSONObject();
		String pk = new String();		
		String surveyDescription = new String();
		try {
			JSONObject obj = new JSONObject(inputJsonString);
			JSONObject slums = obj.getJSONObject("slums");
					
			for (int i = 0; i < data.size(); i++)
			{		
					pk=data.get(i).get(1);					
					surveyDescription= slums.getString(data.get(i).get(1));
					outputJson.put(pk, surveyDescription);		
			}
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}
		
		template = template.replace("COMPLETED_ONLINEMAP_SLUMS", outputJson.toString());
		LinearLayout layout = generateForm(template);
		

		// Phase 2 - Ask for login credentials
		TextView text2 = new TextView(this);
		text2.setText("\n2: Enter your survey.shelter-associates.org login details\n");

		TextView usernameLabel = new TextView(this);
		usernameLabel.setText("Username");

		TextView passwordLabel = new TextView(this);
		passwordLabel.setText("Password");

		final EditText username = new EditText(this);

		final EditText password = new EditText(this);
		password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		layout.addView(text2);
		layout.addView(usernameLabel);
		layout.addView(username);
		layout.addView(passwordLabel);
		layout.addView(password);

		// Phase 3 - Perform the upload
		Button bt_upload = new Button(this);
		bt_upload.setText("Upload Checked Surveys");
		bt_upload.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		bt_upload.setOnClickListener(new OnClickListener() {           
			@Override
			public void onClick(View v) {
				// Get the primary keys of the desired surveys.
				// This has to be done from the main thread.
				final String desired_surveys = get(0);			
				
				// (Check some surveys were picked)
				if (desired_surveys.length()==0)
				{
					Toast.makeText(getApplicationContext(), "You didn't select any surveys!" , Toast.LENGTH_LONG).show();
					return;
				}							
				
				Thread t = new Thread() {

					public void run() {
						Looper.prepare(); //For Preparing Message Pool for the child Thread
						URL url = null;
						try {
							url = new URL("https://survey.shelter-associates.org/android/upload/slum_map/");
						} catch (MalformedURLException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
						// Ignore unverified certificate
						trustAllHosts();
						HttpsURLConnection https = null;
						try {
							https = (HttpsURLConnection) url.openConnection();
						} catch (IOException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
						//https.setHostnameVerifier(DO_NOT_VERIFY);
						https.setDoOutput(true);
						https.setDoInput(true);
						https.setInstanceFollowRedirects(false); 
						try {
							https.setRequestMethod("POST");
						} catch (ProtocolException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} 
						https.setRequestProperty("Content-Type", "application/json"); 
						int httpResponseCode;
						String surveyResponseCode = new String();
						JSONObject json = new JSONObject();
						
						JSONArray slum = new JSONArray();
						
						try {							
							
							JSONObject slumobj=new JSONObject();
							// Put authentication details into JSON
							json.put("username", username.getText().toString() );
							json.put("password", password.getText().toString());
							// Which surveys are we uploading?
							String[] desiredSurveysArray = desired_surveys.trim().split(" ");
							for (int i=0; i<desiredSurveysArray.length; i++) 
							{
								JSONArray slumId = new JSONArray();
								ArrayList<List<String>> tbl=new ArrayList<List<String>>();
								
								//getting data from sqlite database : passing 4 parameter are ( table name, condition field, condition, order by)
								tbl=db.getAllDataList(db.TABLE_PLOTTEDSHAPE_GROUP, db.KEY_SLUMID,desiredSurveysArray[i].toString(),"");
								
								for (int j = 0; j < tbl.size(); j++) {
									JSONObject fact = new JSONObject();
									
									fact.put("pk", tbl.get(j).get(0));									
									fact.put("drawn_compo", tbl.get(j).get(2));
									fact.put("name", tbl.get(j).get(3));									
									fact.put("date", tbl.get(j).get(4));
									fact.put("lat_long", tbl.get(j).get(5));
																	
									slumId.put(fact);									
									
								}
								slumobj = new JSONObject();
								
								slumobj.put("map_info",slumId);
								slumobj.put("slum_id",desiredSurveysArray[i].toString());
								slum.put(slumobj);
							}
							
							json.put("slum", slum);
							// Set about uploading via HTTPS
							DataOutputStream wr = new DataOutputStream(https.getOutputStream ());
							
							wr.writeBytes(json.toString());
							wr.flush();
							wr.close();
							json=null;							
							
							httpResponseCode = https.getResponseCode();
						
							BufferedReader in = new BufferedReader(
		                               new InputStreamReader(https.getInputStream()));
							surveyResponseCode = in.readLine();
							https.disconnect();

							// If the HTTP response is 200 (i.e. the server was reached), check the surveyResponseCode
							if(httpResponseCode == 200){
								if (surveyResponseCode.indexOf("1") != -1)
								{
									Toast.makeText(getApplicationContext(), "Selected survey(s) uploaded successfully" , Toast.LENGTH_LONG).show();
									
//									//String val=desired_surveys.trim().replace(" ", ",");									
//									for (int i=0; i<desiredSurveysArray.length; i++) 
//									{
//									//	db.deletePlottedShapeGroup(desiredSurveysArray[i].toString());
//									}									
									
									restartActivity();
								}
								else if (surveyResponseCode.indexOf("2") != -1)
								{
									Toast.makeText(getApplicationContext(), "Invalid login details given" , Toast.LENGTH_LONG).show();
								}
								else if (surveyResponseCode.indexOf("3") != -1)
								{
									Toast.makeText(getApplicationContext(), "WARNING: some data wasn't accepted by the server. Double-check the survey data and try again." , Toast.LENGTH_LONG).show();
								}
								else
								{
									Toast.makeText(getApplicationContext(), "There was a network problem and the sever couldn't be reached. Check your wifi connection and try again." , Toast.LENGTH_LONG).show();
								}
							}

						} catch(Exception e) {
							e.printStackTrace();
						}

						Looper.loop(); //Loop in the message queue
					}
				};
				t.start(); 
			}
		});

		TextView text3 = new TextView(this);
		text3.setText("\n3: Press the magic button!\n");

		layout.addView(text3);
		layout.addView(bt_upload);

		// Now render it all
		sv.addView(layout);
		setContentView(sv);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public String parseDownloadToString( String filename )
	{
		try
		{
			FileInputStream stream = openFileInput(filename);
			return parseFileToString(stream);

		} catch ( IOException e ) {
			Log.i("Log", "IOException: " + e.getMessage() );
		}
		return null;
	}

	/**
	 * Trust every server - don't check for any certificate
	 */
	private void restartActivity() 
	{
		Intent intent = new Intent(this, Index.class);
		startActivity(intent);
	}

}
