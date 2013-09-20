package shelter.android.survey.classes.widgets;

import shelter.android.survey.classes.forms.*;
import android.content.Context;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class FormCheckBox extends FormWidget
{

	protected int 			_priority;
	protected CheckBox		_checkbox;
	protected String 		_id;
	protected String 		_type;
	
	public FormCheckBox( Context context, String property, String id, String type ) 
	{
		super( context, property );

		_checkbox = new CheckBox( context );
		_checkbox.setText( this.getDisplayText() );
		_id = id;
		_type = type;
		
		_layout.addView( _checkbox );
	}
	
	public String getType()
	{
		return _type;
	}

	@Override
	public String getValue() {
		return String.valueOf( _checkbox.isChecked() ? "1" : "0" );
	}


	public void setValue( String value ) {
		_checkbox.setChecked( value.equals("1") );
	}
	
	public void error(Boolean val)
	{
		if (val)
				{
			_checkbox.setError("Enter Value");
				}
		else
		{
			_checkbox.setError(null);
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
