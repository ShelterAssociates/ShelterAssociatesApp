package shelter.android.survey.classes.widgets;
import shelter.android.survey.classes.forms.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import shelter.android.survey.classes.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class OrderedSpinner extends FormWidget
{
	protected JSONObject _options;
	protected TextView _label;
	protected Spinner _spinner;
	protected Map<String, String> _propmap;
	protected ArrayAdapter<String> _adapter;
	protected ArrayList<String> _order;
	protected String _type;
	protected String _id;
	protected Map<String, String> _orderMap = new HashMap<String, String>();
	protected ArrayList<String> _slumOrdered = new ArrayList<String>();

	/*
	 * Twin version of FormSpinner, duplicate except
	 * from the ordering of options in the spinner,
	 * Ordered alphabetically in this version to make
	 * SelectSlum more user-friendly.
	*/

	public OrderedSpinner( Context context, String property, JSONObject options, String id, String type ) 
	{
		super( context, property );

		_type = type;
		_options = options;
		_id = id;
		_label = new TextView( context );
		_label.setText( getDisplayText() );
		_label.setLayoutParams( FormActivity.defaultLayoutParams );
		_label.setTextAppearance(context, R.style.CustomTextStyle );
		_spinner = new Spinner( context );
		_spinner.setLayoutParams( FormActivity.defaultLayoutParams );

		String p;
		String name;
		JSONArray propertyNames = options.names();
		_order = new ArrayList<String>();
		
		_orderMap = new HashMap<String, String>();
		_orderMap.put(" Make a Selection", "0");
		_slumOrdered.add(" Make a Selection");
		
		for (int i = 0; i < propertyNames.length(); i++)
		{
			try {
				_orderMap.put(options.getString(propertyNames.getString(i)) ,propertyNames.getString(i));
				_slumOrdered.add( options.getString(propertyNames.getString(i)));
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
		}

		Collections.sort(_slumOrdered);
		_propmap = new HashMap<String, String>();
		_adapter = new ArrayAdapter<String>( context, R.layout.spinner_item );
		_adapter.setDropDownViewResource( R.layout.spinner_item );
		_spinner.setAdapter( _adapter );
		_spinner.setSelection( 0 );
		

			for( int i = 0; i < options.length()+1; i++ ) 
			{
				name =  _slumOrdered.get(i);
				p = _orderMap.get( name );
				_adapter.add( name );
				_propmap.put( name, p);
			}

		_layout.addView( _label );
		_layout.addView( _spinner );
		_spinner.setSelection(0);
	}

	public String getType()
	{
		return _type;
	}

	@Override

	// -----------------------------------------------
	//
	// getValue() -
	//
	//	Returns value held in spinner, by matching
	//	the selected string to int in _propmap
	//
	// -----------------------------------------------

	public String getValue() {
		
		if(_propmap.get( _adapter.getItem( _spinner.getSelectedItemPosition() ) ).equals("0"))
		{
			return "";
		}

		return _propmap.get( _adapter.getItem( _spinner.getSelectedItemPosition() ) );

	}

	public void error(Boolean val)
	{
		if (val == true)
		{
			_label.setError("Enter Value");
		}
		else
		{
			_label.setError(null);
		}

	}
	
	public String getStringValue()
	{
		return _adapter.getItem( _spinner.getSelectedItemPosition() );
	}

	// -----------------------------------------------
	//
	// setValue() -
	//
	// Takes value from database and matches the value
	// to a selection, and sets the selection accordingly.
	//
	// -----------------------------------------------

	@Override
	public void setValue(String value)
	{
		try{
			String name;
			JSONArray names = _options.names();

			for( int i = 0; i < names.length(); i++ )
			{
				name = names.getString(i);

				if( name.equals(value) )
				{
					String item = _options.getString(name);
					_spinner.setSelection( _adapter.getPosition(item) );
				}
			}
		} catch( JSONException e ){
			Log.i("Lykaion", e.getMessage() );
		}
	}

	public String getId()
	{
		return _id;
	}

	@Override 
	public void setToggleHandler( FormActivity.FormWidgetToggleHandler handler )
	{
		super.setToggleHandler(handler);
		_spinner.setOnItemSelectedListener( new SelectionHandler( this ) );
	}

	class SelectionHandler implements AdapterView.OnItemSelectedListener 
	{
		protected FormWidget _widget;

		public SelectionHandler( FormWidget widget ){
			_widget = widget;
		}

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			if( _handler != null ){
				_handler.toggle( _widget );
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}

	}
}
