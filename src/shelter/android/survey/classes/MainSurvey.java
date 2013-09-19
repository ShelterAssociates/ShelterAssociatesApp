package shelter.android.survey.classes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import shelter.android.survey.classes.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;


//Test

/* Version 1.5 - David Stark - 8/8/13
 * 
 * - Survey data now saving and populating
 * from database dependent on PK taken from
 * Surveys table on opening of particular slum.
 * 
 * - If survey record does not exist, new row is
 * created, if it does exist, old row is opened and 
 * populated from matching key in Facts table.
 * 
 * Version 1.6 - David Stark - 8/8/13
 * 
 * - All validation now completes correctly,
 * showing warning signals next to improperly 
 * completed widgets. Does not save while there
 * is any active warning. 
 * 
 */

public class MainSurvey extends SurveyFormActivity
{
	public static final int OPTION_SAVE = 0;
	public static final int OPTION_LOAD = 1;
	public static final int OPTION_CANCEL = 2;
	public static final int OPTION_SAVEDRAFT = 4;
	public static final int OPTION_SCROLL = 5;
	private ListView mDrawerList;
	FrameLayout mContentFrame;
	//private String[] sections;
	private ArrayList<String> subSections = new ArrayList<String>();
	JSONObject obj = new JSONObject();
	ArrayList<String> sections = new ArrayList<String>();
	String slum;
	String key;
	String survey;
	String slumName;
	String section;
	String householdId;
	JSONObject json_survey;

	PrintWriter writer;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );
		// Parse intent
		slum = (String)getIntent().getExtras().get("slum");
		slumName = (String)getIntent().getExtras().get("slumName");
		subSections = getIntent().getExtras().getStringArrayList("subSections");
		householdId = (String)getIntent().getExtras().get("householdId");
		survey = (String)getIntent().getExtras().get("surveyId");
		// Render The main survey area
		section = (String) getIntent().getExtras().get("section");
		setTitle(slumName + " - " + section);
		setContentView(R.layout.main);
		String json_data = parseDownloadToString( "all_surveys.json" );
		obj = JSONParser.getJSONFromString( json_data );
		key = init(slum, survey, householdId);
		
		
		if(section.equals(""))
		{
			subSections = getSubSections();
		}
		
		
		
		try {
			json_survey = obj.getJSONObject(survey);
			String[] params = {json_survey.toString(), slum, slumName, section, key, survey, householdId} ;
			LinearLayout layout = generateForm(params, db, subSections);
			mContentFrame = (FrameLayout)findViewById(R.id.content_frame);
			mContentFrame.addView(layout);
			// Render the sidebar
			mDrawerList = (ListView)findViewById(R.id.left_drawer);
			sections = getSections(json_survey.toString(), subSections);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, sections);
			mDrawerList.setAdapter(adapter);
			mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
			mDrawerList.setDividerHeight(15);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Display existing facts
		DatabaseHandler db = new DatabaseHandler(this);
		populate(db, key);
	}
//
	private class DrawerItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			saveAsDraft(db, key);
			selectItem(position);
			
		}
	}

	private void selectItem(int position)
	{
		//sections.get(position);
		section = sections.get(position);
		Intent intent = new Intent(this, MainSurvey.class);
		intent.putExtra("slum", slum);
		intent.putExtra("slumName", slumName);
		intent.putExtra("surveyId", survey);
		intent.putExtra("section", section);
		intent.putExtra("subSections", subSections);
		intent.putExtra("householdId",  householdId);
		mDrawerList.setItemChecked(position, true);
	    startActivity(intent);
	}

	public String init(String slum, String survey, String householdId)
	{
		if (db.isInSurvey(slum, survey, householdId) == null)
		{
			db.addSurvey(slum, survey, householdId);
			return db.isInSurvey(slum, survey, householdId);
		}

		return db.isInSurvey(slum, survey, householdId);
	}
	
	public ArrayList<String> getSubSections()
	{
		ArrayList<String> subSections = new ArrayList<String>();
		Map<Integer, Integer> dbSubSections = db.getSubSections(key);
		for (Map.Entry<Integer, Integer> entry : dbSubSections.entrySet())
		{
		try
		{
		JSONObject schema = obj.getJSONObject(survey);
		JSONArray sections = schema.getJSONArray("sections");
			for(int i = 0; i<sections.length();i++)
			{
				JSONObject section = sections.getJSONObject(i);
				if(section.has("sub_section")){
				JSONArray questions = section.getJSONArray("sub_section");
					for(int j=0;j<questions.length();j++)
					{
						JSONObject question = questions.getJSONObject(j);
						if(question.getString("id").equals(String.valueOf(entry.getValue())))
						{
							subSections.add(section.getString("name"));
						}
					}
				}
			}
		
		}
		catch(JSONException e)
		{
			
		}
		}
		
		
		return subSections;
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) 
	{
		menu.add( 0, OPTION_SAVE, 0, "Save" );
		menu.add( 0, OPTION_SAVEDRAFT, 0, "Save as Draft");
		menu.add( 0, OPTION_LOAD, 0, "Load" );
		menu.add( 0, OPTION_CANCEL, 0, "Cancel" );
		return true;
	}

	@Override
	public boolean onMenuItemSelected( int id, MenuItem item )
	{

		switch( item.getItemId() )
		{
		case OPTION_SAVE:
			Boolean saved = false;
			saved = save(db, key, json_survey.toString());
			if (saved)
			{
				Intent intent = new Intent(this, Index.class);
				startActivity(intent);
			}
			break;

		case OPTION_LOAD:
			populate(db, key);
			break;

		case OPTION_SAVEDRAFT:
			saveAsDraft(db, key);
			resetErrors();
			Toast.makeText(getApplicationContext(), "Draft Save Success!" , Toast.LENGTH_SHORT).show();
			break;

		case OPTION_CANCEL:
			break;
		}

		return super.onMenuItemSelected( id, item );
	}

	@Override
	public void onBackPressed()
	{
		new AlertDialog.Builder(this).setTitle("Do you want to save?")
		.setMessage("Do you wish to save a draft?").setNegativeButton("No", new OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1)
			{
				Intent intent  = new Intent(getBaseContext(), Index.class);
				startActivity(intent);
			}
		})
		.setPositiveButton("Yes", new OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1)
			{
				saveAsDraft(db, key);
				Intent intent  = new Intent(getBaseContext(), Index.class);
				startActivity(intent);

			}

		}
				).setNeutralButton("Cancel", null).create().show();
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
}