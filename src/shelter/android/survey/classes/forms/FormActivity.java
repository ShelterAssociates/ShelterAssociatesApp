package shelter.android.survey.classes.forms;
import shelter.android.survey.classes.utils.*;
import shelter.android.survey.classes.widgets.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import shelter.android.survey.classes.widgets.FormCheckBox;
import shelter.android.survey.classes.widgets.FormEditText;
import shelter.android.survey.classes.widgets.FormMultiCheckBox;
import shelter.android.survey.classes.widgets.FormNumericEditText;
import shelter.android.survey.classes.widgets.OrderedSpinner;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import shelter.*;

/**
 * 
 * Adapted from Jeremy Neal Brown's 'FormGenerator' code:
 * 
 * "FormActivity allows you to create dynamic form layouts based upon a json schema file. 
 * This class should be sub-classed."
 * 
 * This code is used to dynamically render the survey/slum selection and upload process in
 * the Shelter Android Application.
 * It is also used as the basis for the more elaborate 'SurveyFormActivity', which interacts
 * with an SQLite database and features a sidebar to assist navigation between sections.
 * 
 */
public abstract class FormActivity extends InternetUtils
{
	/**
	 * Initializing all constants that will be needed for JSON object types, 
	 * T - EditText Text
	 * I - EditText Integer
	 * S - Single Choice - Spinner
	 * M - Multiple Choice - Checkbox 
	 * 
	 */

	public static String SCHEMA_KEY_MYMAP		= "map";
	public static String SCHEMA_KEY_TYPE		= "type";
	public static String SCHEMA_KEY_BOOL 		= "boolean";
	public static String SCHEMA_KEY_REQUIRED	= "required";
	public static String SCHEMA_KEY_INT  		= "I";
	public static String SCHEMA_KEY_SINGLE		= "S";
	public static String SCHEMA_KEY_STRING 		= "T";
	public static String SCHEMA_KEY_MULTI 		= "M";
	public static String SCHEMA_KEY_PRIORITY	= "weight";
	public static String SCHEMA_KEY_TOGGLES		= "toggles";
	public static String SCHEMA_KEY_DEFAULT		= "default";
	public static String SCHEMA_KEY_MODIFIERS	= "modifiers";
	public static String SCHEMA_KEY_OPTIONS		= "choices";
	public static String SCHEMA_KEY_META		= "meta";
	public static String SCHEMA_KEY_SURVEYS		= "surveys";	
	public static String SCHEMA_KEY_HINT		= "hint";
	public static String SCHEMA_KEY_ID          = "id";
	public static String PHOTO_FOLDER 			= "ShelterPhotos";
	public static String PHOTO_PATH				= Environment.getExternalStorageDirectory().getAbsolutePath();
	public static final LayoutParams defaultLayoutParams = new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    public static String m_id = "";
	// -- data
	protected Map<String, FormWidget> _map;
	protected ArrayList<FormWidget> _widgets;

	// -- widgets
	protected LinearLayout _container;
	protected LinearLayout _layout;
	protected ScrollView   _viewport;
	
	// -----------------------------------------------
	//
	// parse data and build view
	//
	// -----------------------------------------------

	/**
	 * parses a supplied schema of raw json data and creates widgets
	 * @param data - the raw json data as a String
	 * @return 
	 */
	public LinearLayout generateForm( String data )
	{
		_widgets = new ArrayList<FormWidget>();
		_map = new HashMap<String, FormWidget>();

		try
		{
			String name;
			FormWidget widget;
			JSONObject property;
			JSONObject schema = new JSONObject( data ); 
			JSONArray names = schema.names();
			for( int i= 0; i < names.length(); i++ ) 
			{
				name = names.getString( i );

				if( name.equals( SCHEMA_KEY_META )  ) continue;

				property = schema.getJSONObject( name );	

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
		} catch( JSONException e ) {
			Log.i( "Log", e.getMessage() );
		}

		// -- sort widgets on priority
		Collections.sort( _widgets, new PriorityComparison() );	

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

		_viewport.addView( _layout );
		_container.addView( _viewport );

		


		return _container;
	}

	void resetErrors()
	{
		for( int i = 0; i < _widgets.size(); i++)
		{
			FormWidget widget = _widgets.get(i);
			widget.error(false);
		}
	}

	// -----------------------------------------------
	//
	// toggles
	//
	// -----------------------------------------------

	/**
	 * creates the map a map of values for visibility and references to the widgets the value affects
	 */
	protected HashMap<String, ArrayList<String>> processToggles( JSONObject property )
	{
		try{
			ArrayList<String> toggled;
			HashMap<String, ArrayList<String>> toggleMap = new HashMap<String, ArrayList<String>>();

			JSONObject toggleList = property.getJSONObject( FormActivity.SCHEMA_KEY_TOGGLES );
			JSONArray toggleNames = toggleList.names();

			for( int j = 0; j < toggleNames.length(); j++ )
			{
				String toggleName = toggleNames.getString(j);
				JSONArray toggleValues = toggleList.getJSONArray( toggleName );
				toggled = new ArrayList<String>();
				toggleMap.put( toggleName, toggled );
				for( int k = 0; k < toggleValues.length(); k++ ) {
					toggled.add( toggleValues.getString(k) );
				}
			}

			return toggleMap;

		} catch( JSONException e ){
			return null;
		}
	}

	/**
	 * returns a boolean indicating that the supplied json object contains a property for toggles
	 */
	protected boolean hasToggles( JSONObject obj ){
		try{
			obj.getJSONObject( FormActivity.SCHEMA_KEY_TOGGLES );
			return true;
		} catch ( JSONException e ){
			return false;
		}
	}

	void setScroll(int y)
	{
		_viewport.scrollTo(0, y);
	}

	public String get(int widget_id)
	{
		return _widgets.get(widget_id).getValue();		
	}


	public String getName(int widget_id)
	{
		return _widgets.get(widget_id).getStringValue();
	}



	/**
	 * initializes the visibility of widgets that are togglable 
	 */
	protected void initToggles()
	{
		int i;
		FormWidget widget;

		for( i = 0; i < _widgets.size(); i++ )  {
			widget = _widgets.get(i);
			updateToggles( widget );
		}
	}

	/**
	 * updates any widgets that need to be toggled on or off
	 * @param widget
	 */
	protected void updateToggles( FormWidget widget ) 
	{
		int i;
		String name;
		ArrayList<String> toggles;
		ArrayList<FormWidget> ignore = new ArrayList<FormWidget>();

		toggles = widget.getToggledOn();
		for( i = 0; i < toggles.size(); i++ ) 
		{
			name = toggles.get(i);
			if( _map.get(name) != null ) 
			{
				FormWidget toggle = _map.get(name);
				ignore.add( toggle );
				toggle.setVisibility( View.VISIBLE );
			}
		}

		toggles = widget.getToggledOff();
		for( i = 0; i < toggles.size(); i++ ) 
		{
			name = toggles.get(i);
			if( _map.get(name) != null ) 
			{
				FormWidget toggle = _map.get(name);
				if( ignore.contains(toggle) ) continue;
				toggle.setVisibility( View.GONE );
			}
		}
	}

	/**
	 * simple callbacks for widgets to use when their values have changed
	 */
	public class FormWidgetToggleHandler
	{
		public void toggle( FormWidget widget ) {
			updateToggles( widget );
		}
	}

	// -----------------------------------------------
	//
	// utils
	//
	// -----------------------------------------------

	protected String getDefault( JSONObject obj ){
		try{
			return obj.getString( FormActivity.SCHEMA_KEY_DEFAULT );
		} catch ( JSONException e ){
			return null;
		}
	}

	/**
	 * helper class for sorting widgets based on priority
	 */
	class PriorityComparison implements Comparator<FormWidget>
	{
		public int compare( FormWidget item1, FormWidget item2 ) {
			return item1.getPriority() > item2.getPriority() ? 1 : -1;
		}
	}

	/**
	 * factory method for actually instantiating widgets
	 */
	protected FormWidget getWidget( String name, JSONObject property ) 
	{
		try
		{
			String type = property.getString( FormActivity.SCHEMA_KEY_TYPE );
			String id = property.getString(FormActivity.SCHEMA_KEY_ID);
			Boolean bTakePhoto = false;
			if (property.has("takePhoto"))
			{
				bTakePhoto = property.getBoolean("takePhoto");
			}
			if( type.equals( FormActivity.SCHEMA_KEY_STRING) ){
				return new FormEditText( this, name, id , type, bTakePhoto );
			}

			if( type.equals( FormActivity.SCHEMA_KEY_BOOL ) ){
				return new FormCheckBox( this, name, id, type, bTakePhoto );
			}

			if( type.equals( FormActivity.SCHEMA_KEY_INT) ){
				return new FormNumericEditText( this, name, id, type, bTakePhoto );
			}


			if( type.equals( FormActivity.SCHEMA_KEY_MULTI) )
			{
				if (property.has(FormActivity.SCHEMA_KEY_OPTIONS ) )
				{
					JSONObject options = property.getJSONObject( FormActivity.SCHEMA_KEY_OPTIONS );
					return new FormMultiCheckBox( this, name, options, id, type, bTakePhoto ) ;
				}
			}

			if( type.equals(  FormActivity.SCHEMA_KEY_SINGLE ) )
			{	
				if( property.has( FormActivity.SCHEMA_KEY_OPTIONS ) ) 
				{
					JSONObject options = property.getJSONObject( FormActivity.SCHEMA_KEY_OPTIONS );
					return new OrderedSpinner(  this, name, options, id, type, bTakePhoto );
				}
			}
		} catch( JSONException e ) {
			return null;
		}
		return null;
	}

	public static String parseFileToString( InputStream stream )
	{
		try
		{
			int size = stream.available();

			byte[] bytes = new byte[size];
			stream.read(bytes);
			stream.close();

			return new String( bytes );

		} catch ( IOException e ) {
			Log.i("Log", "IOException: " + e.getMessage() );
		}
		return null;
	}
	
	public static String parseAssetToString( Context context, String filename )
	{
		try
		{
			InputStream stream = context.getAssets().open( filename );
			return parseFileToString(stream);

		} catch ( IOException e ) {
			Log.i("Log", "IOException: " + e.getMessage() );
		}
		return null;
	}
}


