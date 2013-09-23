package shelter.android.survey.classes.menus;
import shelter.android.survey.classes.forms.*;
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

public class SurveySelect extends FormActivity 
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        // Prepare the 'Continue' button
		Button bt = new Button(this);
		bt.setText("Continue");
		bt.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 50));
		bt.setOnClickListener(new OnClickListener() {           
			@Override
			public void onClick(View v) {
				if(getName(0).equals(" Make a Selection"))
				{
					Toast.makeText(getApplicationContext(), "Please select a survey!" , Toast.LENGTH_SHORT).show();
					
				}
				else{
				Intent intent = new Intent(v.getContext(), SlumSelect.class);
				intent.putExtra("surveyName", getName(0));
				intent.putExtra("surveyId", get(0));
			    startActivity(intent);
				}
			}
		});
		// Parse JSON
		try {
			FileInputStream file = openFileInput("all_surveys.json");
	        String jsonString = FormActivity.parseFileToString( file );
	        JSONObject obj = new JSONObject(jsonString);
        	JSONObject surveys = obj.getJSONObject(SCHEMA_KEY_SURVEYS);
        	LinearLayout layout = generateForm("{ \"Select a Survey\" : " + surveys.toString() + " }");
    		TextView spacer = new TextView(this);
    		spacer.setHeight(15);
    		layout.addView(spacer);
        	layout.addView(bt);
        	setContentView(layout);
        }  catch (JSONException e) {
    		Log.e("JSON Parser", "Error parsing data " + e.toString());
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        
    }
}