package shelter.android.survey.classes.forms;

import shelter.android.survey.classes.menus.*;
import shelter.android.survey.classes.widgets.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

/**
 * SurveyFormActivity is an extension of FormActivity.
 * It allows for surveys with sections to be created, including surveys
 * in which sections may have branchable subsections.
 * Customised for use with the Shelter Associates Online Survey System,
 * it also interacts with an SQLite database. 
 * The DatabaseHandler class is, therefore, a dependency.
 * 
 */
public abstract class SurveyFormActivity extends FormActivity
{
	// -- data
	int subIndex = 0;
	Button bt, bt2;
	Boolean button = false;
	Boolean partOfSub = false;
	public DatabaseHandler db = new DatabaseHandler(this);
	ArrayList<String> subSects = new ArrayList<String>();

	// -----------------------------------------------
	//
	// parse data and build view
	//
	// -----------------------------------------------

	/**
	 * generateForm(String[] params, final DatabaseHandler db, ArrayList<String> subSects)
	 * takes a String Array of parameters, pm, a DatabaseHandler object, db, and an ArrayList
	 * of existing subSections. From the params parameter, it uses [0], which is a raw JSON file, 
	 * to generate each widget which is displayed to the user on a LinearLayout.
	 */
	public LinearLayout generateForm(String[] params, final DatabaseHandler db, ArrayList<String> subSects)
	{
		_widgets = new ArrayList<FormWidget>();
		_map = new HashMap<String, FormWidget>();
		this.subSects = subSects;
		
		final String data = params[0];
		final String slum = params[1];
		final String slumName = params[2];
		String sectionName = params[3];
		final String key = params[4];
		final String survey = params[5];
		final String householdId = params[6];
		
		try
		{
			
			
			String name;
			FormWidget widget;
			JSONObject property = null;
			JSONObject schema = new JSONObject( data );


			JSONArray sections = schema.getJSONArray("sections");
			if (sectionName.equals("")) {
				JSONObject section_1 = sections.getJSONObject(0);
				sectionName = section_1.getString("name");
			}
			
			JSONObject section = new JSONObject();
			JSONObject temp;
			JSONArray questions = new JSONArray();
			for(int i = 0; i < sections.length(); i++)
			{
				temp = sections.getJSONObject(i);

				if(temp.getString("name").equals(sectionName))
				{
					section = temp;
					questions = section.getJSONArray("desired_facts");
				}

				else if(sectionName.contains(temp.getString("name")))
				{
					section = temp;
					questions = section.getJSONArray("sub_section");
					partOfSub = true;

				}		

			}

			JSONObject question;


			for( int i= 0; i < questions.length(); i++ ) 
			{
				question = questions.getJSONObject(i);
				name = question.getString("name");
				property = question;

				if( name.equals( SCHEMA_KEY_META )  ) continue;

				boolean toggles  = hasToggles( property );
				String defaultValue   = getDefault( property );
				int priority = property.getInt( FormActivity.SCHEMA_KEY_PRIORITY );

				if (property.getString(FormActivity.SCHEMA_KEY_REQUIRED).equals("True"))
				{
					name = name + " *";
				}

				widget = getWidget( name, property );
				if( widget == null) continue;
				widget.setPriority( priority );
				widget.setValue( defaultValue );

				if(partOfSub)
				{
					Pattern pattern = Pattern.compile("([0-9]+$)");
					Matcher matcher = pattern.matcher(sectionName);
					matcher.find();
					
					try{
						widget.setSub(Integer.valueOf(matcher.group(0)));
					}
					catch(IllegalStateException e)
					{
					}
				}


				if( toggles ){
					widget.setToggles( processToggles( property ) );
					widget.setToggleHandler( new FormActivity.FormWidgetToggleHandler() );
				}

				if( property.has(FormActivity.SCHEMA_KEY_HINT)) widget.setHint( property.getString( FormActivity.SCHEMA_KEY_HINT ) );

				if (property.getString(FormActivity.SCHEMA_KEY_REQUIRED).equals("True"))
				{
					widget.setRequired(true);
				}

				_widgets.add( widget );

				_map.put( name, widget );
			}

			if(!section.getString("sub_section").equals(null) && !partOfSub)
			{
				
				try{
					subIndex = subSects.size();
				}
				catch(NullPointerException e)
				{
					subSects = new ArrayList<String>();
				}
				final ArrayList<String> subSects2 = subSects;

				final String finalName = section.getString("name");

				//final int sectionNo = Integer.valueOf(section.getString("id"));
				name = section.getString("name");
				bt = new Button(this);
				bt.setText("Add another");
				bt.setMinHeight(50);
				button = true;
				bt.setOnClickListener(new OnClickListener()
				{
					public void onClick(View v)
					{
						subIndex ++;
						ArrayList<String> subSections = subSects2;
						subSections.add(finalName);	
						Log.i("Log", "Passing " + finalName + " in Intent as section");
						Intent intent = new Intent(v.getContext(), MainSurvey.class);
						intent.putExtra("subSections" , subSections);
						intent.putExtra("slumName", slumName);
						intent.putExtra("slum", slum);
						intent.putExtra("surveyId", survey);
						intent.putExtra("section", finalName);
						intent.putExtra("householdId", householdId);
						saveAsDraft(db, key);
						startActivity(intent);
						return;
					}	


				}
						);
				
				bt2 = new Button(this);
				bt2.setText("Remove last");
				final Intent intent = new Intent(this, MainSurvey.class);
				//intent.putExtra("subSections" , subSections);
				intent.putExtra("slumName", slumName);
				intent.putExtra("slum", slum);
				intent.putExtra("surveyId", survey);
				intent.putExtra("section", finalName);
				intent.putExtra("householdId", householdId);
				
				bt2.setOnClickListener(new OnClickListener()
				{
					public void onClick(View v)
					{
						
						ArrayList<String> subSections = subSects2;
						if(subSections.size() == 0)
						{
							Toast.makeText(getApplicationContext(), "There are no subsections to remove!" , Toast.LENGTH_SHORT).show();
						}
						else if(!db.hasQs(key, String.valueOf(subSections.size())))
						{
							subIndex --;
							db.removeSub(key,subSections.size());
							subSections.remove(subSections.size()-1);
							Toast.makeText(getApplicationContext(), "Removed last section" , Toast.LENGTH_SHORT).show();
							intent.putExtra("subSections" , subSections);
							saveAsDraft(db, key);
							startActivity(intent);
						}
						else{
								
								AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());
								alertDialogBuilder.setTitle("Are you sure?");
								alertDialogBuilder.setMessage("Are you sure you want to remove the last section?")
								.setCancelable(false)
								.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
									public void onClick(DialogInterface dialog, int id){
										
										ArrayList<String> subSections = subSects2;
										subIndex --;
										db.removeSub(key,subSections.size());
										subSections.remove(subSections.size()-1);
										intent.putExtra("subSections" , subSections);
										startActivity(intent);
										Toast.makeText(getApplicationContext(), "Removed last section" , Toast.LENGTH_SHORT).show();
									}
								})
								.setNegativeButton("No", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id){
										dialog.cancel();
									}
								});
								AlertDialog alertDialog = alertDialogBuilder.create();
								alertDialog.show();
						}
						
						return;
					}	


				}
						);
			}


		} catch( JSONException e ) {
			Log.i( "Log", e.getMessage() );
		}

		// -- sort widgets on priority
		Collections.sort( _widgets, new PriorityComparison() );	
		//Log.i("Log", bt.getText().toString());

		// -- create the layout
		_container = new LinearLayout( this );
		_container.setOrientation( LinearLayout.VERTICAL );
		_container.setLayoutParams( FormActivity.defaultLayoutParams );

		_viewport  = new ScrollView( this );
		_viewport.setLayoutParams( FormActivity.defaultLayoutParams );

		_layout = new LinearLayout( this );
		_layout.setOrientation( LinearLayout.VERTICAL );
		_layout.setLayoutParams( FormActivity.defaultLayoutParams );

		initToggles();


		for( int i = 0; i < _widgets.size(); i++ ) {
			_layout.addView( ( View ) _widgets.get(i).getView() );
		}

		if(button)
		{
			_layout.addView(bt);
			_layout.addView(bt2);
			
		}

		_viewport.addView( _layout );
		_container.addView( _viewport );

		return _container;
	}

	public ArrayList<String> getSections(String data, ArrayList<String> subSections)
	{
		ArrayList<String> result =  new ArrayList<String>();
		try{

			JSONObject schema = new JSONObject(data);
			JSONArray sections = schema.getJSONArray("sections");
			for (int i = 0; i<sections.length(); i++)
			{
				JSONObject section = sections.getJSONObject(i);
				String sectionName = section.getString("name");
				result.add(sectionName);
				try {
					if (subSections.contains(sectionName))
					{
						for(int j = 0; j<(subSections.size());j++)
						{
							if(subSections.get(j).equals(sectionName))
							{
								result.add("	" + sectionName + " - " + (String.valueOf(j+1)));
							}
						}
					}
				}
				catch(NullPointerException e)
				{
					System.out.println(e + " at getSections()");
				}

				//Log.i("Log", result[i]);
			}
		}
		catch(JSONException e)
		{

		}
		return result;

	}
	
	/**
	 * populate(db, survey) takes the database and the survey number, which
	 * is used as a key, and fills the form with previously saved answers from
	 * the database if they are available.
	 */
	protected void populate( DatabaseHandler db, String survey )
	{
		try
		{
			FormWidget widget;
			for( int i = 0; i< _widgets.size(); i++) 
			{
				widget = _widgets.get(i);
				widget.setValue(db.getValue(widget.getId(), widget.getSub(), survey));
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			System.out.println(e);
		}
	}

	/**
	 * Save(db, survey) validates each value in the form in respect to it's
	 * specifications. If validation fails, toast and warning signs are placed
	 * accordingly, and the form is saved as a draft, and returns false.
	 * 
	 * The method first creates a Map of all required and unrequired questions from the 
	 * JSON input file with their required input type. It then uses a helper method from 
	 * the DatabaseHelper class, getQs(surveyId), to load a Map all of the desired facts 
	 * which are currently stored in the database for that survey with their answers and 
	 * the subSection they belong to. It then validates the answers according to the required
	 * input type, and if they do not pass validation, or the required desired fact has not yet 
	 * been saved in the database, the desired fact Id is recorded. At the end of the method,
	 * if some desired facts have not passed validation, the section it belongs to is looked up
	 * from the sectionMap ArrayList and returned in a toast to the user. 
	 * 
	 */



	protected Boolean save(DatabaseHandler db, String surveyId, String survey)
	{
		Map<String, String> jsonRequired = new HashMap<String, String>();
		Map<String, String> jsonNotRequired = new HashMap<String, String>();
		Map<String, ArrayList<String>> dbQuestions = new HashMap<String, ArrayList<String>>();
		//Map<String, Integer> subSectionMap = new HashMap<String, Integer>();
		saveAsDraft(db, surveyId);
		this.db = db;
		Map<String, String> sectionMap = new HashMap<String, String>();
		ArrayList<String> unfinishedSections = new ArrayList<String>();
		Map<String, String> unAnswered = new HashMap<String, String>();
		
		/*	
		 * Setting up Map of questions from JSON input file,
		 * which will then be used to compare against questions
		 * present in the database to ensure that all required 
		 * questions have been answered.
		 * 
		 */
		
		String data = survey;
		try{
			JSONObject schema = new JSONObject(data);
			JSONArray sections = schema.getJSONArray("sections");
			for(int i = 0; i<sections.length(); i++)
			{
				JSONObject section = sections.getJSONObject(i);
				JSONArray questions = section.getJSONArray("desired_facts");
				for(int j = 0; j<questions.length(); j++)
				{
					JSONObject question = questions.getJSONObject(j);
					if(question.getString("required").equals("True")){
						jsonRequired.put(question.getString("id"), question.getString("type"));
						
							sectionMap.put(question.getString("id"), section.getString("name"));
						
					}
					else{
						jsonNotRequired.put(question.getString("id"), question.getString("type"));
					}
				}
				
				try{
				
					/*
					 * Processing for subSection questions.
					 * Should only reach code if there is an 
					 * existing subsection, in which case it 
					 * will add the required subSection questions
					 * to the map of required questions.
					 */
					
					
					
				if(!section.optJSONArray("sub_section").equals(null) && subSects.size() > 0)
				{
					questions = section.optJSONArray("sub_section");
					for(int j = 0; j<questions.length(); j++)
					{
						JSONObject question = questions.getJSONObject(j);
						if(question.getString("required").equals("True")){
							jsonRequired.put(question.getString("id"), question.getString("type"));
							
						}
						else{
							jsonNotRequired.put(question.getString("id"), question.getString("type"));
						}
					}
				}
				}
				catch(NullPointerException e)
				{
					
				}
				
				
			}
			
			
		}
		catch(JSONException e)
		{

		}

		dbQuestions = db.getQuestionMap(surveyId);

		/*
		 * Validation testing. For each entry in jsonRequired,
		 * dbQuestions is searched. For each entry that isn't 
		 * matched in dbQuestions, an entry is made to unAnswered
		 * with the Desired Fact id. If there is a match, then the
		 * value is validated against the requirement from the JSON 
		 * input file. If it doesn't validate, it's also added to the
		 * list of unAnswered questions.
		 * 
		 * 
		 */
		
		for(Map.Entry<String, String> entry : jsonRequired.entrySet())
		{
			if (dbQuestions.containsKey(entry.getKey()))
			{
				//Log.i("Log", "Entry key - " + entry.getKey() + " dbQuestionskeys " + dbQuestions.keySet().toString());
				//Log.i("Log", "Answer for q - " + entry.getKey() +" is " +  dbQuestions.get(entry.getKey()).get(0));


				if (dbQuestions.get(entry.getKey()).get(0).equals(""))
				{
					unAnswered.put(entry.getKey(), dbQuestions.get(entry.getKey()).get(1));
					//Log.i("Log", "Answer for q - " + entry.getKey() +" is " +  dbQuestions.get(entry.getKey()).get(0));
				}
				if (entry.getValue().equals("I"))
				{
					try{
						int k = Integer.valueOf(dbQuestions.get(entry.getKey()).get(0));
					}
					catch(NumberFormatException e)
					{
						unAnswered.put(entry.getKey(), dbQuestions.get(entry.getKey()).get(1));
					}
				}
			}

			else
			{
				/*  If question is not stored in DB, then dbQuestions.get(entry.getKey()).get(1)) will be
			    	null. In that case, it can not be a part of a subSection, so we can set the subSection
					entry of the map to 0.
				*/
				
				try{
				unAnswered.put(entry.getKey(), dbQuestions.get(entry.getKey()).get(1));
				//Log.i("Log", entry.getKey());
				}
				catch(NullPointerException e)
				{
					unAnswered.put(entry.getKey(), "0");
				}
			}
		}

		for(Map.Entry<String, String> entry : jsonNotRequired.entrySet())
		{
			/*
			 * If database has record of unrequired question, if it
			 * is empty it can validate, if it has an invalid value
			 * it cannot and will be added to unAnswered.
			 */

			if(dbQuestions.containsKey(entry.getKey()) && !unAnswered.containsKey(dbQuestions.get(entry.getKey())))
			{
				if(entry.getValue().equals("I") && !dbQuestions.get(entry.getKey()).get(0).equals(""))
				{
					try{
						int k = Integer.valueOf(dbQuestions.get(entry.getKey()).get(0));
					}
					catch(NumberFormatException e)
					{
						unAnswered.put(entry.getKey(), dbQuestions.get(entry.getKey()).get(1));
					}
				}
			}
		}

		/*
		 	If the unAnswered ArrayList is not empty, then
		 	the validation has failed and there are incomplete
		 	answers. We then check which section they are part 
		 	of, and return the list of sections to the user in 
		 	a toast.
		 */



		if (!(unAnswered.isEmpty()))
			
		{
			for (Map.Entry entry: unAnswered.entrySet())
					{
				
				//entry.getKey();
				//Log.i("Log", "Unfinished question - " + entry.getKey());
		
			//Log.i("Log", "Unanswered - " + unAnswered.toString());
			//Log.i("Log", "Section Map - " + sectionMap.toString());
			//Log.i("Log", "" + entry.getValue());
			
				
				/*	We only want a section with an unAnswered question 
				 *  to be displayed once to the user, so there is first
				 *  a check to see if the unfinishedSection has already 
				 *  been entered. If not, it is entered.
				 *  
				 *  If entry.getValue() is not 1, the entry is part of
				 *  a subSection, therefore needs to be displayed differently.
				 *  It then finds the name of the section by looking at the 
				 *  subSection id and finding the index in the subSects ArrayList,
				 *  and using the name to identify the subSection that is not complete.				 * 
				 * 
				 */
				
				if(!unfinishedSections.contains(sectionMap.get(entry.getKey())) && entry.getValue().equals("0"))
						{
							unfinishedSections.add(sectionMap.get(entry.getKey()));
						}
				
				else if(!entry.getValue().equals("0") && !unfinishedSections.contains((subSects.get(Integer.valueOf((String)entry.getValue())-1) + " Subsections" )))
				{
					unfinishedSections.add(subSects.get(Integer.valueOf((String)entry.getValue())-1) + " Subsections" );
				}
				}
			
			
			
			
			Toast.makeText(getApplicationContext(), "Save failed! Sections not complete!" + 
					unfinishedSections.toString(), Toast.LENGTH_LONG).show();
			
			return false;
		}

		else{
			db.setComplete(surveyId, 1);
			Toast.makeText(getApplicationContext(), "Save Success!", Toast.LENGTH_LONG).show();
			return true;
		}
		
	}

	/**	saveAsDraft(DatabaseHandler db, String survey)
	 *
	 * saveAsDraft takes the current Database and Survey and
	 * saves the Desired Fact id, Value and SubSection id in the
	 * database. Does no validation, therefore can be thought of as
	 * a temporary save. First checks if the Desired Fact is currently in the
	 * database, if it is, update the row, if not, add the whole row to the
	 * database.
	 * 
	 */

	public void saveAsDraft(DatabaseHandler db, String survey)
	{
		FormWidget widget = _widgets.get(0);
		this.db = db;

		for( int i = 0; i < _widgets.size(); i++)
		{
			widget = _widgets.get(i);
			if(!db.isInDb(widget.getId(), widget.getSub(), survey))
			{
				db.addQuestion(widget.getId(), widget.getValue(), widget.getSub(), survey);
			}

			else if (db.isInDb(widget.getId(), widget.getSub(), survey) && !widget.getValue().equals(""))
			{
				db.setValue(widget.getId(), widget.getValue(), widget.getSub(), survey);
			}
		}
		db.setComplete(survey, 0);
	}
	
	@Override
	protected FormWidget getWidget( String name, JSONObject property ) 
	{
		try
		{
			String type = property.getString( FormActivity.SCHEMA_KEY_TYPE );
			String id = property.getString(FormActivity.SCHEMA_KEY_ID);

			if( type.equals( FormActivity.SCHEMA_KEY_STRING) ){
				return new FormEditText( this, name, id , type );
			}

			if( type.equals( FormActivity.SCHEMA_KEY_BOOL ) ){
				return new FormCheckBox( this, name, id, type );
			}

			if( type.equals( FormActivity.SCHEMA_KEY_INT) ){
				return new FormNumericEditText( this, name, id, type );
			}


			if( type.equals( FormActivity.SCHEMA_KEY_MULTI) )
			{
				if (property.has(FormActivity.SCHEMA_KEY_OPTIONS ) )
				{
					JSONObject options = property.getJSONObject( FormActivity.SCHEMA_KEY_OPTIONS );
					return new FormMultiCheckBox( this, name, options, id, type ) ;
				}
			}

			if( type.equals(  FormActivity.SCHEMA_KEY_SINGLE ) )
			{	
				if( property.has( FormActivity.SCHEMA_KEY_OPTIONS ) ) 
				{
					JSONObject options = property.getJSONObject( FormActivity.SCHEMA_KEY_OPTIONS );
					return new FormSpinner(  this, name, options, id, type );
				}
			}
		} catch( JSONException e ) {
			return null;
		}
		return null;
	}
}
