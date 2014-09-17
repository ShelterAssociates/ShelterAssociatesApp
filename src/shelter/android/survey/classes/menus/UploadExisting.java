package shelter.android.survey.classes.menus;
import shelter.android.survey.classes.forms.*;
import shelter.android.survey.classes.widgets.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
import android.os.Environment;
import android.os.Looper;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


public class UploadExisting extends FormActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Phase 1 - Generate the multi-checkboxes of completed surveys
		final DatabaseHandler db = new DatabaseHandler(this);
		ScrollView sv = new ScrollView(this);
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

		String template = FormActivity.parseAssetToString(this, "completed_slums_template.json" );
		JSONObject outputJson = new JSONObject();
		String pk = new String();
		String slumName = new String();
		String surveyName = new String();
		String householdCode = new String();
		String surveyDescription = new String();
		try {
			JSONObject obj = new JSONObject(inputJsonString);
			JSONObject slums = obj.getJSONObject("slums");
			JSONObject surveys = obj.getJSONObject("surveys");
			surveys = surveys.getJSONObject("choices");
			for (int i = 0; i < complete.size(); i++)
			{
				Log.i("Log", "Getting complete surveys " + i);
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
				//Create the directory if not available
				String root = Environment.getExternalStorageDirectory().toString();
				final File myDir = new File(root + "/ShelterPhotos");
				myDir.mkdirs();
				
				
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
									question.add(questions.get(j).get(1)); // QID
									question.add(questions.get(j).get(2)); // Fact
									question.add(survey.get(0)); // Survey
									question.add(survey.get(1)); // Slum
									question.add(survey.get(2)); // Household
									//Change questions.get(j).get(3) to questions.get(j).get(4)
									question.add(questions.get(j).get(4)); // sub_code
									question.add(questions.get(j).get(0)); // fact pk
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
								
								//Code added by SC:
								//Code to send photos to server.
								ArrayList<List<String>> arrImageList = db.getImageList(questionsToUpload.get(i).get(6));
								JSONArray images = new JSONArray();
								JSONObject image = new JSONObject();
								for (int j=0; j<arrImageList.size(); j++)
								{
									image.put("Latitude", arrImageList.get(j).get(0));
									image.put("Longitude", arrImageList.get(j).get(1));
									//Fetch image file and convert to bitmap and then to encode using base64.
									InputStream inputStream=null;
									try {
										inputStream= new FileInputStream(FormActivity.PHOTO_PATH+"/"+FormActivity.PHOTO_FOLDER+"/"+arrImageList.get(j).get(2));//You can get an inputStream using any IO API
										
									
									byte[] bytes;
									byte[] buffer = new byte[8192];
									int bytesRead;
									ByteArrayOutputStream output = new ByteArrayOutputStream();
									try {
									    while ((bytesRead = inputStream.read(buffer)) != -1) {
									    output.write(buffer, 0, bytesRead);
									}
									} catch (IOException e) {
									e.printStackTrace();
									}
									inputStream.close();
									bytes = output.toByteArray();											
//									 ByteArrayOutputStream baoPhoto = new ByteArrayOutputStream();
//									    GZIPOutputStream zos = new GZIPOutputStream(baoPhoto);
//									    zos.write(bytes);
//									    zos.close(); 
//									    bytes = baoPhoto.toByteArray();								
									String image_str = Base64.encodeToString(bytes, Base64.DEFAULT);
									image.put("Image", image_str);
									images.put(image);
									image = new JSONObject();
									
									} catch (Exception e) {
										// TODO: handle exception
									}
								} 
								fact.put("image_list", images);
								images = new JSONArray();
								facts.put(fact);
							}
							json.put("facts", facts);
							// Set about uploading via HTTPS
							facts=null;
							questionsToUpload = null;
							DataOutputStream wr = new DataOutputStream(https.getOutputStream ());
							
							wr.writeBytes(json.toString());
							wr.flush();
							wr.close();
							json=null;
							httpResponseCode = https.getResponseCode();
							// Read the response
							GZIPInputStream surveySystemResponse = (GZIPInputStream) https.getContent();
							InputStreamReader reader = new InputStreamReader(surveySystemResponse);
							BufferedReader in = new BufferedReader(reader);
//							BufferedReader in = new BufferedReader(
//		                               new InputStreamReader(https.getInputStream()));
							surveyResponseCode = in.readLine();
							https.disconnect();

							// If the HTTP response is 200 (i.e. the server was reached), check the surveyResponseCode
							if(httpResponseCode == 200){
								if (surveyResponseCode.indexOf("1") != -1)
								{
									Toast.makeText(getApplicationContext(), "Selected survey(s) uploaded successfully" , Toast.LENGTH_LONG).show();
//									// Locally delete the successfully uploaded surveys.
//									for (int i=0; i<desiredSurveysArray.length; i++) 
//									{
//										// Get questions by survey primary key
//										String surveyPrimaryKey = desiredSurveysArray[i];
//										db.removeSurvey(surveyPrimaryKey);
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
		layout.addView(bt);

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
