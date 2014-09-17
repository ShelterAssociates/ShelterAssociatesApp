package shelter.android.survey.classes.menus;
import shelter.android.survey.classes.forms.*;
import shelter.android.survey.classes.widgets.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class SlumSelect extends FormActivity 
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        final String surveyId = (String) getIntent().getExtras().get("surveyId");
        final String surveyName = (String) getIntent().getExtras().get("surveyName");
        setTitle(surveyName);
        // Prepare the 'Continue' button
		Button bt = new Button(this);
		bt.setText("Continue");
		bt.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 50));
		bt.setOnClickListener(new OnClickListener() {           
			@Override
			public void onClick(View v) {
				
				if(getName(0).equals(" Make a Selection"))
				{
					Toast.makeText(getApplicationContext(), "Please select a slum!" , Toast.LENGTH_SHORT).show();

				}
				
				else
				{
				Intent intent = new Intent(v.getContext(), MainSurvey.class);
				intent.putExtra("slum", get(0)); // slum should be the chosen spinner value
				intent.putExtra("slumName", getName(0));
				intent.putExtra("surveyId", surveyId);
				intent.putExtra("section", "");
				try {
					String householdId = get(1);
					if (! householdId.matches("\\d\\d\\d\\d")) {
						Toast.makeText(getApplicationContext(), "The household ID should be four digits, e.g. 0004" , Toast.LENGTH_LONG).show();
						return;
					} else {
						intent.putExtra("householdId", householdId);
					}
				} catch (Exception e) {
					intent.putExtra("householdId", "0");
				}
			    startActivity(intent);
				}
			}
		});
		
		
		 // Prepare the 'MapView' button
		Button mapbt = new Button(this);
		mapbt.setText("View Map");
		mapbt.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 50));
		mapbt.setOnClickListener(new OnClickListener() {           
			@Override
			public void onClick(View v) {
				
				if(getName(0).equals(" Make a Selection"))
				{
					Toast.makeText(getApplicationContext(), "Please select a slum!" , Toast.LENGTH_SHORT).show();
				}				
				else
				{				
					Intent intent = new Intent(SlumSelect.this,EditMap.class);
					intent.putExtra("slumID", get(0)); // slum should be the chosen spinner value	
					intent.putExtra("slumName", getName(0));
					startActivity(intent);
				}
			}
		});
		
		// Parse JSON
		FileInputStream file;
		try {
			file = openFileInput("all_surveys.json");
			String jsonString = FormActivity.parseFileToString( file );
			JSONObject obj = new JSONObject(jsonString);
			JSONObject survey = obj.getJSONObject(surveyId);
        	JSONObject meta = survey.getJSONObject(SCHEMA_KEY_META);
        	JSONObject slums = meta.getJSONObject("slums"); 
        	String questionString = "{ \"Select a Slum\" : " + slums.toString();
        	if (surveyId.indexOf("h") != -1) {
        		questionString += FormActivity.parseAssetToString(this, "household_select_template.json");
        	} 
    		LinearLayout layout = generateForm(questionString + " }"); 
    		TextView spacer = new TextView(this);
    		spacer.setHeight(15);
    		layout.addView(spacer);
        	layout.addView(bt);
        	
        	TextView spacer1 = new TextView(this);
        	spacer1.setHeight(15);
    		layout.addView(spacer1);
        	layout.addView(mapbt);
        	setContentView(layout);
        	
        }  catch (JSONException e) {
    		Log.e("JSON Parser", "Error parsing data " + e.toString());
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
          
    }
}