package shelter.android.survey.classes.widgets;
import shelter.android.survey.classes.forms.*;

import shelter.android.survey.classes.R;
import android.content.Context;
import android.graphics.Paint.Style;
import android.text.InputType;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class FormEditText extends FormWidget
{
	protected TextView _label;
	protected EditText _input;
	protected String   _id;
	protected String   _type;
	
	public FormEditText( Context context, String property, String id, String type)
	{
		super( context, property );
		
		_id = id;
		_type = type;
		_label = new TextView( context );
		_label.setText( getDisplayText() );
		_label.setTextAppearance(context, R.style.CustomTitleStyle );
		
		_label.setLayoutParams( FormActivity.defaultLayoutParams );
	
		_input = new EditText( context );
		_input.setLayoutParams( FormActivity.defaultLayoutParams );
		_input.setImeOptions( EditorInfo.IME_ACTION_DONE );
		_input.setTextAppearance(context, R.style.CustomTextStyle);
		
		_layout.addView( _label );
		_layout.addView( _input );
		
	}
	
	
	
	@Override
	public String getValue(){
		return _input.getText().toString();
	}
	
	
	public void error(Boolean val)
	{
		if (val)
				{
			_label.setError("Enter Value");
				}
		else
		{
			_label.setError(null);
		}
		
	}
	
	public String getType()
	{
		return _type;
	}
	
	
	public String getId()
	{
		return _id;
	}
	
	@Override
	public void setValue( String value ) {
		_input.setText( value );
	}
	
	@Override 
	public void setHint( String value ){
		_input.setHint( value );
	}
}
