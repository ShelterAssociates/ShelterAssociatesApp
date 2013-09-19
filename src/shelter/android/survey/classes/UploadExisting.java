package shelter.android.survey.classes;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import shelter.android.survey.classes.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.TextView;
import android.widget.Toast;


public class UploadExisting extends FormActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Phase 1 - Generate the multi-checkboxes of completed surveys
        final DatabaseHandler db = new DatabaseHandler(this);
        // Get a list of length-2 arrays:
        // [0] = slum ID
        // [1] = slum name (needs to be looked up from input JSON)
        String inputJsonString = parseDownloadToString( "all_surveys.json" );
        ArrayList<ArrayList<String>> complete = db.getCompletedSurveys(); //
        if (complete.isEmpty())
        {
        	this.onBackPressed();
        	Toast.makeText(getApplicationContext(), "No surveys to upload!" , Toast.LENGTH_LONG).show();
        	return;
         }
        JSONObject obj = JSONParser.getJSONFromString(inputJsonString);
		String template = FormActivity.parseAssetToString(this, "completed_slums_template.json" );
		JSONObject outputJson = new JSONObject();
		String pk = new String();
		String slumName = new String();
		String surveyName = new String();
		String householdCode = new String();
		String surveyDescription = new String();
		try {
        	JSONObject slums = obj.getJSONObject("slums");
        	JSONObject surveys = obj.getJSONObject("surveys");
        	surveys = surveys.getJSONObject("choices");
        	for (int i = 0; i < complete.size(); i++)
        	{
        		pk = complete.get(i).get(0);
        		surveyName = surveys.getString(complete.get(i).get(1));
        		slumName = slums.getString(complete.get(i).get(2));
        		householdCode = complete.get(i).get(3);
        		surveyDescription = surveyName + " - " + slumName;
        		if (!householdCode.equals("0")) {
        			surveyDescription += "(Household " + householdCode + ")"; 
        		}
        		outputJson.put(pk, surveyDescription);
        	}
        } catch (JSONException e) {
    		Log.e("JSON Parser", "Error parsing data " + e.toString());
    	}
        template = template.replace("COMPLETED_SLUMS", outputJson.toString());
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
        
        Button bt = new Button(this);
		bt.setText("Upload Checked Surveys");
		bt.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		bt.setOnClickListener(new OnClickListener() {           
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
							// url = new URL("http://android.blake01.webfactional.com/test/");
							url = new URL("https://survey.shelter-associates.org/android/upload/");
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
		                JSONArray facts = new JSONArray();
		                try {
		                    // Put authentication details into JSON
							json.put("username", username.getText().toString() );
		                    json.put("password", password.getText().toString());
		                    // Which surveys are we uploading?
		                    String[] desiredSurveysArray = desired_surveys.trim().split(" ");
		                    // Put all the questions for these surveys into an array list of array lists.
		                    ArrayList<ArrayList<String>> questionsToUpload = new ArrayList<ArrayList<String>>();
		                    for (int i=0; i<desiredSurveysArray.length; i++) 
		                    {
		                    	// Get questions by survey primary key
		                    	String surveyPrimaryKey = desiredSurveysArray[i];
		                    	ArrayList<String> survey = db.getSurvey(surveyPrimaryKey);
		                    	ArrayList<List<String>> questions = db.getQuestions(surveyPrimaryKey);
		                    	for (int j=0; j<questions.size(); j++)
		                    	{
		                    		ArrayList<String> question = new ArrayList<String>();
		                    		question.add(questions.get(j).get(0)); // QID
		                    		question.add(questions.get(j).get(1)); // Fact
		                    		question.add(survey.get(0)); // Survey
		                    		question.add(survey.get(1)); // Slum
		                    		question.add(survey.get(2)); // Household
		                    		question.add(questions.get(j).get(3)); // sub_code
		                    		// Add this question to the pile
		                    		questionsToUpload.add(question); 
		                    	}
		                    }
		                    
		                    // Now compile this data into JSON.
		                    // The database lookups and JSON compilation could probably be done in the same loop,
		                    // but I find this more verbose way easier to read and understand.
		                    for (int i=0; i<questionsToUpload.size(); i++)
		                    {
		                    	JSONObject fact = new JSONObject();
			                    fact.put("code", questionsToUpload.get(i).get(0));
			                    fact.put("data", questionsToUpload.get(i).get(1));
			                    // Strip the last character from the survey code.
			                    // The app makes a distinction between slum- and household-level surveys;
			                    // however, the server-side code does not.
		                    	String surveyWithSubcode = questionsToUpload.get(i).get(2);
			                    fact.put("survey", surveyWithSubcode.substring(0, surveyWithSubcode.length()-1));
			                    fact.put("slum", questionsToUpload.get(i).get(3));
			                    fact.put("household",questionsToUpload.get(i).get(4));
			                    fact.put("sub_code", questionsToUpload.get(i).get(5));
			                    facts.put(fact);
		                    }
		                    json.put("facts", facts);
		                    
		                    // Set about uploading via HTTPS
		                    DataOutputStream wr = new DataOutputStream(https.getOutputStream ());
		                    wr.writeBytes(json.toString());
		                    wr.flush();
		                    wr.close();
		                    httpResponseCode = https.getResponseCode();
		                    // Read the response
		                    GZIPInputStream surveySystemResponse = (GZIPInputStream) https.getContent();
		                    InputStreamReader reader = new InputStreamReader(surveySystemResponse);
		                    BufferedReader in = new BufferedReader(reader);
		                    surveyResponseCode = in.readLine();
		                    https.disconnect();
		                 
		                    // If the HTTP response is 200 (i.e. the server was reached), check the surveyResponseCode
		                    if(httpResponseCode == 200){

		                        if (surveyResponseCode.indexOf("1") != -1)
		                        {
		                        	Toast.makeText(getApplicationContext(), "Selected survey(s) uploaded successfully" , Toast.LENGTH_LONG).show();
		                        	// Locally delete the successfully uploaded surveys.
				                    for (int i=0; i<desiredSurveysArray.length; i++) 
				                    {
				                    	// Get questions by survey primary key
				                    	String surveyPrimaryKey = desiredSurveysArray[i];
				                    	db.removeSurvey(surveyPrimaryKey);
				                    }
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
		layout.addView(bt);
		
		// Now render it all
		setContentView(layout);
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
	
	private void restartActivity() 
	{
    	Intent intent = new Intent(this, Index.class);
	    startActivity(intent);
	}

}
