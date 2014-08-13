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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class FormMultiCheckBox extends FormWidget{

	protected JSONObject _options;
	protected CheckBox		_checkbox;
	protected ArrayList<CheckBox> _boxes = new ArrayList<CheckBox>();
	protected Map<String, String> _propmap = new HashMap<String, String>();
	protected Map<String, String> _opmap = new HashMap<String, String>();
	protected ArrayList<Integer> _order;
	protected TextView _label;
	protected String _id;
	
	String name;
	String p;
	String result;
	String _type;

	// -----------------------------------------------
	//
	// FormMultiCheckBox._order
	// 
	// Arraylist<String> _order takes each number value
	// from the JSONObject options, then sorts and uses
	// the sorted array to add checkboxes to the layout
	// in the proper order specified in the survey.
	//		Required as JSONObjects do not maintain 
	//		order while being manipulated.
	//
	// -----------------------------------------------

	public FormMultiCheckBox( Context context, String property, JSONObject options, String id, String type, Boolean bTakePhoto)
	{
		super( context, property );
		_order = new ArrayList<Integer>();
		_options = options;
		_type = type;
		JSONArray propertyNames = options.names();
		try{
			for (int i = 0; i < propertyNames.length(); i++)
			{
				_order.add(Integer.valueOf(propertyNames.getString(i)));
			}
		}
		catch (JSONException e)
		{
			System.out.println("Something went wrong here.");
		}
		_id = id;


		Collections.sort(_order);


		_label = new TextView( context );
		_label.setText( getDisplayText() );
		_layout.addView(_label);
		_label.setTextAppearance(context, R.style.CustomTitleStyle );

		try{

			for(int i= 0; i < _options.length(); i++)
			{

				name = String.valueOf(_order.get(i));
				p = options.getString( name );
				_propmap.put(p, name);
				_opmap.put(name, p);
				_opmap.put(name, p);
				_checkbox = new CheckBox ( context );
				_checkbox.setText(p);
				_boxes.add(_checkbox);
				_layout.addView(_checkbox);
				_checkbox.setTextAppearance(context,R.style.CustomCheckBox);

			}
			
			//Added by SC : to add take photo button if applicable
			AddPhotoLayout(context, bTakePhoto);
		}
		catch(JSONException e){
			e.printStackTrace();
		}

	}

	// -----------------------------------------------
	//
	// getValue()
	// 
	// Iterates through the _boxes ArrayList,
	// if the box is checked, the id of the box
	// is appended to the result. If none are checked,
	// returns 0.
	//
	// -----------------------------------------------

	public String getValue() {

		result = "";
		for (int i = 0; i < _boxes.size(); i++)
		{
			if ((_boxes.get(i).isChecked()))
			{
				result = result +_propmap.get(_boxes.get(i).getText()) + " ";
			}
		}
		return result;

	}


	public void error(Boolean val)
	{
		if (val = true)
		{
			//_label.setError("Enter Value");
		}
		else
		{
			_label.setError(null);
		}

	}


	public void reset()
	{
		for(int i = 0; i<_boxes.size(); i++)
		{
			//_boxes.get(i).setChecked(false);
		}
	}

	public String getType()
	{
		return _type;
	}

	public void makeHighLight()
	{
		_label.setError("Enter Value");
	}

	// -----------------------------------------------
	//
	// setValue()
	// 
	// Takes value in the form "1 2 3... n", uses regular
	// expression to strip spaces and put in an array in 
	// the form [1,2,3,...n]. Then iterates through the array
	// and get()s the appropriate box object then sets it as checked.
	//
	// -----------------------------------------------

	public void setValue( String value ) {

		if (value != null)
		{
			String[] array = value.trim().split("[^0-9]+");
			for(int i = 0; i < array.length; i++)
			{				
				for (int j = 0; j<_boxes.size();j++)

					try{
						if(_boxes.get(j).getText().equals(_opmap.get(array[i])))
						{
							_boxes.get(j).setChecked(true);
						}
					}
				catch(NumberFormatException e)
				{
					break;
				}
			}
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
		_checkbox.setOnCheckedChangeListener( new ChangeHandler(this) );
	}

	class ChangeHandler implements CompoundButton.OnCheckedChangeListener 
	{
		protected FormWidget _widget;

		public ChangeHandler( FormWidget widget ) {
			_widget = widget;
		}
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
		{
			if( _handler != null ){
				_handler.toggle( _widget );
			}
		}

	}

}

