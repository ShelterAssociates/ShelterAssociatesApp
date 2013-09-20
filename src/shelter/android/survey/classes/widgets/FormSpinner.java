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

public class FormSpinner extends FormWidget
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

	// -----------------------------------------------
	//
	// FormSpinner._order
	// 
	// Arraylist<String> _order takes each number value
	// from the JSONObject options, then sorts and uses
	// the sorted array to add choices to the layout
	// in the proper order specified in the survey.
	//		Required as JSONObjects do not maintain 
	//		order while being manipulated.
	//
	// -----------------------------------------------

	public FormSpinner( Context context, String property, JSONObject options, String id, String type ) 
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
		

		for (int i = 0; i < propertyNames.length(); i++)
		{
			try {
				_orderMap.put(propertyNames.getString(i), options.getString(propertyNames.getString(i)));
				_order.add(propertyNames.getString(i));
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
		}

		Collections.sort(_order);
		_propmap = new HashMap<String, String>();
		//_adapter = new ArrayAdapter<String>( context, android.R.layout.simple_spinner_item );
		_adapter = new ArrayAdapter<String>( context, R.layout.spinner_item );
		_adapter.setDropDownViewResource( R.layout.spinner_item );
		_spinner.setAdapter( _adapter );
		_spinner.setSelection( 0 );

		try{
			for( int i = 0; i < options.length(); i++ ) 
			{
				name =  _order.get(i);
				p = options.getString( name );
				_adapter.add( p );
				_propmap.put( p, name );
			}
		} catch( JSONException e){

		}

		_layout.addView( _label );
		_layout.addView( _spinner );
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
