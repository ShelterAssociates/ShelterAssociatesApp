package shelter.android.survey.classes;

import shelter.android.survey.classes.R;
import android.content.Context;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class FormNumericEditText extends FormWidget
{
	protected TextView _label;
	protected EditText _input;
	protected int _priority;
	protected String _id;
	protected String _type;
		
	public FormNumericEditText(Context context, String property, String id, String type) 
	{
		super( context, property );
		
		_type = type;
		_id = id;
		_label = new TextView( context );
		_label.setText( getDisplayText() );
		
		_input = new EditText( context );
		_input.setInputType( InputType.TYPE_CLASS_PHONE );
		_input.setImeOptions( EditorInfo.IME_ACTION_DONE );
		_input.setLayoutParams( FormActivity.defaultLayoutParams );
		_label.setTextAppearance(context, R.style.CustomTitleStyle );
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
